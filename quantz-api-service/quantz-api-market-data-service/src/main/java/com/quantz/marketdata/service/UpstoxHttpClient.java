package com.quantz.marketdata.service;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Interface for Upstox HTTP client operations
 */
public interface UpstoxHttpClient {
    
    /**
     * Send HTTP GET request to Upstox API
     */
    <T> ResponseEntity<T> get(String path, Class<T> responseType);
    
    /**
     * Send HTTP GET request with query parameters to Upstox API
     */
    <T> ResponseEntity<T> get(String path, Map<String, Object> queryParams, Class<T> responseType);
    
    /**
     * Send HTTP POST request to Upstox API
     */
    <T> ResponseEntity<T> post(String path, Object requestBody, Class<T> responseType);
    
    /**
     * Send a custom RequestEntity to Upstox API
     */
    <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) throws RestClientException;
}