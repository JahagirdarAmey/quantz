package com.quantz.backtest.service;

import com.quantz.backtest.exception.BadRequestException;
import com.quantz.backtest.mapper.BacktestMapper;
import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.repository.BacktestRepository;
import com.quantz.event.model.BacktestCreatedEvent;
import com.quantz.event.publisher.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Validated
public class BacktestService {
    private static final Logger log = LoggerFactory.getLogger(BacktestService.class);

    private final BacktestRepository backtestRepository;
    private final EventPublisher eventPublisher;
    private final BacktestMapper backtestMapper;
    private final UserContextService userContextService;

    @Value("${backtest.event.topic}")
    private String backtestCreatedTopic;

    public BacktestService(
            BacktestRepository backtestRepository,
            EventPublisher eventPublisher,
            BacktestMapper backtestMapper,
            UserContextService userContextService) {
        this.backtestRepository = backtestRepository;
        this.eventPublisher = eventPublisher;
        this.backtestMapper = backtestMapper;
        this.userContextService = userContextService;
    }

    @Transactional
    public BacktestCreationResponse createBacktest(@Valid BacktestRequest backtestRequest) throws BadRequestException {
        log.info("Creating backtest for strategy: {}", backtestRequest.getStrategyId());

        validateBacktestRequest(backtestRequest);

        // Generate a new UUID for the backtest
        UUID backtestId = UUID.randomUUID();

        // Get current user ID
        UUID userId = userContextService.getCurrentUserId();

        // Create and save the backtest entity using the mapper
        BacktestEntity entity = backtestMapper.toEntity(backtestRequest, backtestId, userId);
        backtestRepository.save(entity);
        log.debug("Saved backtest entity with ID: {}", backtestId);

        // Create and publish the backtest created event using the mapper
        BacktestCreatedEvent event = backtestMapper.toEvent(backtestRequest, backtestId, userId);
        eventPublisher.publish(backtestCreatedTopic, event);
        log.info("Published backtest created event for backtest ID: {}", backtestId);

        // Create and return the response
        OffsetDateTime estimatedCompletionTime =
                OffsetDateTime.now().plusMinutes(calculateEstimatedMinutes(backtestRequest));

        return new BacktestCreationResponse()
                .backtestId(backtestId)
                .status(BacktestCreationResponse.StatusEnum.PENDING)
                .estimatedCompletionTime(estimatedCompletionTime);
    }

    private void validateBacktestRequest(BacktestRequest request) throws BadRequestException {
        // Validate date range
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }

        // Validate instruments
        if (request.getInstruments() == null || request.getInstruments().isEmpty()) {
            throw new BadRequestException("At least one instrument must be specified");
        }

        // Validate initial capital
        if (request.getInitialCapital() == null || request.getInitialCapital() <= 0) {
            throw new BadRequestException("Initial capital must be greater than zero");
        }

        // Additional validations as needed
        log.debug("Validated backtest request");
    }

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
        if (request.getDataInterval().isPresent()) {
            switch (request.getDataInterval().get()) {
                case _1M:
                intervalFactor = 5.0;
                    break;
                case _5M:
                    intervalFactor = 4.0;
                    break;
                case _15M:
                intervalFactor = 3.0;
                    break;
                case _30M:
                case _1H:
                intervalFactor = 2.0;
                    break;
                default:
                    intervalFactor = 1.0;
            }
        }

        // Combine factors with appropriate weighting
        return (int) Math.ceil(baseMinutes * (1 + dateRangeFactor + instrumentFactor) * intervalFactor);
    }
}
