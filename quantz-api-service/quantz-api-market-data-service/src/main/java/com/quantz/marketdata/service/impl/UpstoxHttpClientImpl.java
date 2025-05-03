package com.quantz.marketdata.service.impl;

import com.quantz.marketdata.config.UpstoxProperties;
import com.quantz.marketdata.service.UpstoxAuthService;
import com.quantz.marketdata.service.UpstoxHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpstoxHttpClientImpl implements UpstoxHttpClient {

    private final RestTemplate restTemplate;
    private final UpstoxAuthService authService;
    private final UpstoxProperties upstoxProperties;
    
    // Track request timing for rate limiting
    private long lastRequestTime = 0;
    private final Object rateLimitLock = new Object();

    @Override
    @Retryable(
        value = {HttpServerErrorException.class, HttpClientErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        String url = buildUrl(path);
        HttpHeaders headers = authService.createAuthHeaders();
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            applyRateLimit();
            log.debug("Sending GET request to: {}", url);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            updateLastRequestTime();
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during GET request to {}: {} - {}", 
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during GET request to {}: {} - {}", 
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during GET request to {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Error during GET request: " + e.getMessage(), e);
        }
    }

    @Override
    @Retryable(
        value = {HttpServerErrorException.class, HttpClientErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> ResponseEntity<T> get(String path, Map<String, Object> queryParams, Class<T> responseType) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl(path));
        
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (value != null) {
                    builder.queryParam(key, value);
                }
            });
        }
        
        String url = builder.build().toString();
        HttpHeaders headers = authService.createAuthHeaders();
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            applyRateLimit();
            log.debug("Sending GET request to: {}", url);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            updateLastRequestTime();
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during GET request to {}: {} - {}", 
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during GET request to {}: {} - {}", 
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during GET request to {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Error during GET request: " + e.getMessage(), e);
        }
    }

    @Override
    @Retryable(
        value = {HttpServerErrorException.class, HttpClientErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> ResponseEntity<T> post(String path, Object requestBody, Class<T> responseType) {
        String url = buildUrl(path);
        HttpHeaders headers = authService.createAuthHeaders();
        
        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            applyRateLimit();
            log.debug("Sending POST request to: {}", url);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            updateLastRequestTime();
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during POST request to {}: {} - {}", 
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during POST request to {}: {} - {}", 
                    url, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during POST request to {}: {}", url, e.getMessage(), e);
            throw new RestClientException("Error during POST request: " + e.getMessage(), e);
        }
    }

    @Override
    @Retryable(
        value = {HttpServerErrorException.class, HttpClientErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        try {
            applyRateLimit();
            
            // Log the request if debug is enabled
            if (log.isDebugEnabled()) {
                URI uri = requestEntity.getUrl();
                HttpMethod method = requestEntity.getMethod();
                log.debug("Sending {} request to: {}", method, uri);
            }
            
            ResponseEntity<T> response = restTemplate.exchange(requestEntity, responseType);
            updateLastRequestTime();
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during request: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during request: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during request: {}", e.getMessage(), e);
            throw new RestClientException("Error during request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build full URL from path
     */
    private String buildUrl(String path) {
        if (path.startsWith("http")) {
            return path;
        }
        
        String baseUrl = upstoxProperties.getBaseUrl();
        
        if (path.startsWith("/")) {
            return baseUrl + path;
        } else {
            return baseUrl + "/" + path;
        }
    }
    
    /**
     * Apply rate limiting to respect API limits
     */
    private void applyRateLimit() {
        synchronized (rateLimitLock) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastRequestTime;
            
            int maxRequestsPerSecond = upstoxProperties.getApi().getMaxRequestsPerSecond();
            long minIntervalMs = 1000 / Math.max(1, maxRequestsPerSecond);
            
            if (lastRequestTime > 0 && elapsedTime < minIntervalMs) {
                long sleepTime = minIntervalMs - elapsedTime;
                try {
                    log.trace("Rate limiting: sleeping for {}ms", sleepTime);
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Update the timestamp of the last request
     */
    private void updateLastRequestTime() {
        synchronized (rateLimitLock) {
            lastRequestTime = System.currentTimeMillis();
        }
    }
}