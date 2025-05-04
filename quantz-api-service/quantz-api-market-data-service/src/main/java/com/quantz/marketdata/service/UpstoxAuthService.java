package com.quantz.marketdata.service;

import com.quantz.marketdata.model.TokenResponse;
import org.springframework.http.HttpHeaders;

/**
 * Interface for authentication service with Upstox
 */
public interface UpstoxAuthService {

    /**
     * Generates the authorization URL for Upstox login
     */
    String getAuthorizationUrl();

    /**
     * Exchanges authorization code for access token
     */
    TokenResponse getAccessToken(String authorizationCode);

    /**
     * Refreshes access token using refresh token
     */
    TokenResponse refreshAccessToken(String refreshToken);

    /**
     * Gets the current valid access token or obtains a new one
     */
    String getCurrentAccessToken();

    /**
     * Checks if token is valid and not expired
     */
    boolean isTokenValid();

    /**
     * Creates an HTTP headers object with the access token
     */
    HttpHeaders createAuthHeaders();
}
