package com.quantz.backtest.repository;

import com.quantz.backtest.entity.BacktestEntity;

import java.util.List;

/**
 * Custom repository interface for additional Backtest query methods
 */
public interface BacktestRepositoryCustom {
    
    /**
     * Find backtests for a user with optional status filter and pagination
     * 
     * @param userId the user ID
     * @param status optional status filter
     * @param limit maximum number of results to return
     * @param offset pagination offset
     * @return list of matching backtest entities
     */
    List<BacktestEntity> findBacktestsForUser(String userId, String status, int limit, int offset);
    
    /**
     * Count backtests for a user with optional status filter
     * 
     * @param userId the user ID
     * @param status optional status filter
     * @return count of matching backtests
     */
    int countBacktestsForUser(String userId, String status);
}