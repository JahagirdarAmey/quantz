package com.quantz.backtest.validator;

import com.quantz.backtest.exception.BadRequestException;
import com.quantz.backtest.model.BacktestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Validator for BacktestRequest objects
 */
@Component
public class BacktestRequestValidator {
    private static final Logger log = LoggerFactory.getLogger(BacktestRequestValidator.class);

    /**
     * Validate a backtest request
     * 
     * @param request the request to validate
     * @throws BadRequestException if the request is invalid
     */
    public void validate(BacktestRequest request) throws BadRequestException {
        validateDateRange(request);
        validateInstruments(request);
        validateInitialCapital(request);
        
        log.debug("Validated backtest request");
    }
    
    private void validateDateRange(BacktestRequest request) throws BadRequestException {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
    }
    
    private void validateInstruments(BacktestRequest request) throws BadRequestException {
        if (request.getInstruments() == null || request.getInstruments().isEmpty()) {
            throw new BadRequestException("At least one instrument must be specified");
        }
    }
    
    private void validateInitialCapital(BacktestRequest request) throws BadRequestException {
        if (request.getInitialCapital() == null || request.getInitialCapital() <= 0) {
            throw new BadRequestException("Initial capital must be greater than zero");
        }
    }
}