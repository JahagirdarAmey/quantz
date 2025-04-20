package com.quantz.backtest.service;

import com.quantz.model.BacktestRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

import static com.quantz.model.BacktestDetail.DataIntervalEnum.*;

/**
 * Calculator for estimating backtest completion times
 */
@Component
public class BacktestCompletionTimeCalculator {
    
    /**
     * Calculate the estimated completion time for a backtest
     * 
     * @param request the backtest request
     * @return the estimated completion time
     */
    public OffsetDateTime calculateEstimatedCompletionTime(BacktestRequest request) {
        int minutes = calculateEstimatedMinutes(request);
        return OffsetDateTime.now().plusMinutes(minutes);
    }
    
    /**
     * Calculate the estimated number of minutes for a backtest to complete
     * 
     * @param request the backtest request
     * @return the estimated number of minutes
     */
    private int calculateEstimatedMinutes(BacktestRequest request) {
        // Calculate the estimated completion time based on complexity factors:
        // - Date range length
        // - Number of instruments
        // - Data interval (more granular = longer)
        long daysBetween = request.getStartDate().until(request.getEndDate()).getDays();
        int instrumentCount = request.getInstruments().size();
        
        // Base calculation - adjust constants as needed based on actual performance
        double baseMinutes = 1.0;
        double dateRangeFactor = daysBetween / 30.0; // normalize to months
        double instrumentFactor = instrumentCount / 5.0; // normalize to typical number
        
        // Interval factor - more granular intervals take longer
        double intervalFactor = 1.0;
        if (request.getDataInterval() != null) {
            intervalFactor = switch (request.getDataInterval()) {
                case _1M -> 5.0;
                case _5M -> 4.0;
                case _15M -> 3.0;
                case _30M, _1H -> 2.0;
                default -> 1.0;
            };
        }
        
        // Combine factors with appropriate weighting
        return (int) Math.ceil(baseMinutes * (1 + dateRangeFactor + instrumentFactor) * intervalFactor);
    }
}