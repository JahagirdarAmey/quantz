package com.quantz.backtest.service;

import com.quantz.backtest.exception.BadRequestException;
import com.quantz.backtest.mapper.BacktestMapper;
import com.quantz.backtest.mapper.BacktestResponseMapper;
import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.repository.BacktestRepository;
import com.quantz.backtest.validator.BacktestRequestValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Service for backtest operations
 */
@Service
@Validated
public class BacktestService {
    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

    private final BacktestRepository backtestRepository;
    private final BacktestMapper backtestMapper;
    private final BacktestResponseMapper responseMapper;
    private final BacktestRequestValidator validator;
    private final UserContextService userContextService;
    private final BacktestEventProducer eventProducer;
    private final BacktestCompletionTimeCalculator completionTimeCalculator;

    public BacktestService(
            BacktestRepository backtestRepository,
            BacktestMapper backtestMapper,
            BacktestResponseMapper responseMapper,
            BacktestRequestValidator validator,
            UserContextService userContextService,
            BacktestEventProducer eventProducer,
            BacktestCompletionTimeCalculator completionTimeCalculator) {
        this.backtestRepository = backtestRepository;
        this.backtestMapper = backtestMapper;
        this.responseMapper = responseMapper;
        this.validator = validator;
        this.userContextService = userContextService;
        this.eventProducer = eventProducer;
        this.completionTimeCalculator = completionTimeCalculator;
    }

    /**
     * Create a new backtest
     *
     * @param backtestRequest the request details
     * @return response with the created backtest ID and status
     * @throws BadRequestException if the request is invalid
     */
    @Transactional
    public BacktestCreationResponse createBacktest(@Valid BacktestRequest backtestRequest) throws BadRequestException {
        log.info("Creating backtest for strategy: {}", backtestRequest.getStrategyId());

        // Validate the request
        validator.validate(backtestRequest);

        // Generate a new UUID for the backtest
        UUID backtestId = UUID.randomUUID();

        // Get current user ID
        UUID userId = userContextService.getCurrentUserId();

        // Create and save the backtest entity
        BacktestEntity entity = backtestMapper.toEntity(backtestRequest, backtestId, userId);
        backtestRepository.save(entity);
        log.debug("Saved backtest entity with ID: {}", backtestId);

        // Publish the backtest created event
        eventProducer.publishBacktestCreatedEvent(backtestRequest, backtestId, userId);

        // Create and return the response
        return responseMapper.toCreationResponse(
                backtestId,
                completionTimeCalculator.calculateEstimatedCompletionTime(backtestRequest)
        );
    }
}