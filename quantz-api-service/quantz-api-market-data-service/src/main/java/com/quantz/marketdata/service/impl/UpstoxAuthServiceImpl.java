package com.quantz.marketdata.service.impl;

import com.quantz.marketdata.config.UpstoxProperties;
import com.quantz.marketdata.entity.OAuthToken;
import com.quantz.marketdata.model.TokenResponse;
import com.quantz.marketdata.repository.OAuthTokenRepository;
import com.quantz.marketdata.service.UpstoxAuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UpstoxAuthServiceImpl implements UpstoxAuthService {

    private final UpstoxProperties upstoxProperties;
    private final OAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    @Override
    public String getAuthorizationUrl() {
        return UriComponentsBuilder
                .fromUriString(upstoxProperties.getAuth().getAuthorizationEndpoint())
                .queryParam("response_type", "code")
                .queryParam("client_id", upstoxProperties.getAuth().getClientId())
                .queryParam("redirect_uri", upstoxProperties.getAuth().getRedirectUri())
                .build()
                .toUriString();
    }

    @Override
    public TokenResponse getAccessToken(String authorizationCode) {
        try {
            log.debug("Exchanging authorization code for access token...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("code", authorizationCode);
            formData.add("client_id", upstoxProperties.getAuth().getClientId());
            formData.add("client_secret", upstoxProperties.getAuth().getClientSecret());
            formData.add("redirect_uri", upstoxProperties.getAuth().getRedirectUri());
            formData.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    upstoxProperties.getAuth().getTokenEndpoint(),
                    request,
                    TokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TokenResponse tokenResponse = response.getBody();

                // Calculate expiry time
                LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
                tokenResponse.setExpiryTime(expiryTime);

                // Save token to database
                saveToken(tokenResponse);

                log.info("Successfully obtained access token, expires at: {}", expiryTime);
                return tokenResponse;
            } else {
                log.error("Failed to get access token: {}", response.getStatusCodeValue());
                throw new RestClientException("Failed to get access token, status: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("Error getting access token: {}", e.getMessage(), e);
            throw new RestClientException("Failed to get access token: " + e.getMessage(), e);
        }
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshToken) {
        try {
            log.debug("Refreshing access token...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("refresh_token", refreshToken);
            formData.add("client_id", upstoxProperties.getAuth().getClientId());
            formData.add("client_secret", upstoxProperties.getAuth().getClientSecret());
            formData.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    upstoxProperties.getAuth().getTokenEndpoint(),
                    request,
                    TokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TokenResponse tokenResponse = response.getBody();

                // Calculate expiry time
                LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
                tokenResponse.setExpiryTime(expiryTime);

                // If refresh token is null in the response, use the old one
                if (tokenResponse.getRefreshToken() == null) {
                    tokenResponse.setRefreshToken(refreshToken);
                }

                // Save token to database
                saveToken(tokenResponse);

                log.info("Successfully refreshed access token, expires at: {}", expiryTime);
                return tokenResponse;
            } else {
                log.error("Failed to refresh access token: {}", response.getStatusCodeValue());
                throw new RestClientException("Failed to refresh access token, status: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("Error refreshing access token: {}", e.getMessage(), e);
            throw new RestClientException("Failed to refresh access token: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCurrentAccessToken() {
        Optional<OAuthToken> latestToken = tokenRepository.findLatestToken();

        if (latestToken.isPresent()) {
            OAuthToken token = latestToken.get();

            // Check if token is expired or about to expire
            LocalDateTime bufferTime = LocalDateTime.now()
                    .plusSeconds(upstoxProperties.getAuth().getTokenExpiryBufferSeconds());

            if (token.getExpiresAt().isBefore(bufferTime)) {
                // Token is expired or about to expire, refresh it
                log.info("Token is expired or will expire soon, refreshing...");
                try {
                    TokenResponse refreshedToken = refreshAccessToken(token.getRefreshToken());
                    return refreshedToken.getAccessToken();
                } catch (Exception e) {
                    log.error("Failed to refresh token: {}", e.getMessage(), e);
                    throw new RestClientException("Failed to obtain valid access token", e);
                }
            }

            return token.getAccessToken();
        } else {
            log.error("No access token found in database");
            throw new RestClientException("No access token found. Please authenticate first.");
        }
    }

    @Override
    public boolean isTokenValid() {
        Optional<OAuthToken> latestToken = tokenRepository.findLatestToken();

        if (latestToken.isPresent()) {
            OAuthToken token = latestToken.get();

            // Check if token is expired or about to expire
            LocalDateTime bufferTime = LocalDateTime.now()
                    .plusSeconds(upstoxProperties.getAuth().getTokenExpiryBufferSeconds());

            if (token.getExpiresAt().isBefore(bufferTime)) {
                // Token is expired or about to expire, try to refresh it
                try {
                    log.info("Token is expired or will expire soon, attempting to refresh");
                    refreshAccessToken(token.getRefreshToken());
                    return true;
                } catch (Exception e) {
                    log.error("Failed to refresh token: {}", e.getMessage());
                    return false;
                }
            }

            return true;
        }

        log.warn("No token found in database");
        return false;
    }

    @Override
    public HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(getCurrentAccessToken());
        return headers;
    }

    /**
     * Save token information to database
     */
    private void saveToken(TokenResponse tokenResponse) {
        OAuthToken oAuthToken = OAuthToken.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresAt(tokenResponse.getExpiryTime())
                .build();

        tokenRepository.save(oAuthToken);
        log.debug("Saved token to database, expires at: {}", oAuthToken.getExpiresAt());
    }
}