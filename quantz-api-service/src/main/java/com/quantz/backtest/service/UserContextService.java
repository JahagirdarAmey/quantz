package com.quantz.backtest.service;


import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to retrieve information about the currently authenticated user
 */
@Service
public class UserContextService {


    public UUID getCurrentUserId() {
        return UUID.randomUUID();
    }
}