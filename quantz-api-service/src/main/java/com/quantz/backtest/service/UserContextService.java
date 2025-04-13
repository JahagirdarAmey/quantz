package com.quantz.backtest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.quantz.backtest.exception.UnauthorizedException;

import java.util.UUID;

/**
 * Service to retrieve information about the currently authenticated user
 */
@Service
public class UserContextService {
    private static final Logger log = LoggerFactory.getLogger(UserContextService.class);

    /**
     * Get the UUID of the currently authenticated user
     * 
     * @return the user's UUID
     * @throws UnauthorizedException if no authenticated user is found
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found");
            throw new UnauthorizedException("No authenticated user found");
        }
        
        Object principal = authentication.getPrincipal();
        
        // Adapt this section based on your specific authentication implementation
        if (principal instanceof UserPrincipal) {
            // If you're using a custom UserPrincipal class
            return ((UserPrincipal) principal).getId();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // If you're using Spring's User class
            String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            return getUserIdFromUsername(username);
        } else if (principal instanceof String username) {
            // If the principal is a String (e.g., username)
            return getUserIdFromUsername(username);
        } else {
            log.error("Unsupported principal type: {}", principal.getClass().getName());
            throw new UnauthorizedException("Unable to determine user identity");
        }
    }
    
    /**
     * Get the username of the currently authenticated user
     * 
     * @return the username
     * @throws UnauthorizedException if no authenticated user is found
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found");
            throw new UnauthorizedException("No authenticated user found");
        }
        
        return authentication.getName();
    }
    
    /**
     * Get the user ID from a username by looking it up in the database
     * 
     * @param username the username
     * @return the user's UUID
     */
    private UUID getUserIdFromUsername(String username) {
        // In a real implementation, you would look up the user ID in your database
        // This is just a placeholder implementation
        try {
            // If your usernames are UUIDs, you can parse directly
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            // Otherwise, you'll need to query your user repository
            // Example: return userRepository.findByUsername(username).getId();
            
            // For demonstration purposes only - replace with actual implementation
            log.warn("Using mock implementation of getUserIdFromUsername - replace with actual DB lookup");
            return UUID.nameUUIDFromBytes(username.getBytes());
        }
    }
    
    /**
     * Example user principal class - customize according to your actual security implementation
     */
    public static class UserPrincipal {
        private final UUID id;
        private final String username;
        
        public UserPrincipal(UUID id, String username) {
            this.id = id;
            this.username = username;
        }
        
        public UUID getId() {
            return id;
        }
        
        public String getUsername() {
            return username;
        }
    }
}