package com.quantz.backtest.service;

import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.model.*;
import com.quantz.backtest.repository.BacktestRepository;
import com.quantz.event.model.BacktestCreatedEvent;
import com.quantz.event.publisher.EventPublisher;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BacktestService {

    private final BacktestRepository backtestRepository;
    private final EventPublisher eventPublisher;

    @Value("${quantz.topics.backtest-created:quantz.backtest.created}")
    private String backtestCreatedTopic;

    @Value("${quantz.topics.backtest-completed:quantz.backtest.completed}")
    private String backtestCompletedTopic;

    @Autowired
    public BacktestService(
            BacktestRepository backtestRepository,
            EventPublisher eventPublisher
    ) {
        this.backtestRepository = backtestRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BacktestCreationResponse createBacktest(@Valid BacktestRequest backtestRequest) throws BadRequestException {
        log.info("Creating backtest for strategy: {}", backtestRequest.getStrategyId());

        validateBacktestRequest(backtestRequest);

        // Generate a new UUID for the backtest
        UUID backtestId = UUID.randomUUID();

        // Create and save the backtest entity
        BacktestEntity entity = createBacktestEntity(backtestId, backtestRequest);
        backtestRepository.save(entity);
        log.debug("Saved backtest entity with ID: {}", backtestId);

        // Create and publish the backtest created event
        BacktestCreatedEvent event = mapToBacktestCreatedEvent(backtestId, backtestRequest);
        eventPublisher.publish(backtestCreatedTopic, event);
        log.info("Published backtest created event for backtest ID: {}", backtestId);

        // Create and return the response
        OffsetDateTime estimatedCompletionTime =
                OffsetDateTime.now().plusMinutes(calculateEstimatedMinutes(backtestRequest));

        return new BacktestCreationResponse()
                .backtestId(UUID.fromString(backtestId.toString()))
                .status(BacktestCreationResponse.StatusEnum.PENDING)
                .estimatedCompletionTime(estimatedCompletionTime);
    }

    public void deleteBacktest(UUID backtestId) {
    }

    public BacktestDetail getBacktest(UUID backtestId) {
        return null;
    }

    public ListBacktests200Response listBacktests(String status, Integer limit, Integer offset) {
        return null;
    }

    //------------------------------------------
    // Helper methods
    //------------------------------------------

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

    private BacktestEntity createBacktestEntity(UUID backtestId, BacktestRequest request) {
        BacktestEntity entity = new BacktestEntity();
        entity.setId(backtestId.toString());
        entity.setUserId(getCurrentUserId().toString());
        entity.setName(request.getName().isPresent() ? request.getName().get() : "Backtest " + backtestId.toString().substring(0, 8));
        entity.setStrategyId(String.valueOf(request.getStrategyId()));
        entity.setStatus("PENDING");
        entity.setCreatedAt(OffsetDateTime.now().toLocalDateTime());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setInstruments(String.join(",", request.getInstruments()));
        entity.setInitialCapital(request.getInitialCapital());
        entity.setCommission(request.getCommission().isPresent() ? request.getCommission().get() : 0.001);
        entity.setSlippage(request.getSlippage().isPresent() ? request.getSlippage().get() : 0.0005);
        entity.setDataInterval(request.getDataInterval().isPresent() ? String.valueOf(request.getDataInterval().get()) : "1d");

        if (request.getStrategyConfig() != null) {
            // Convert strategy config to JSON string or similar format
            entity.setStrategyConfig(convertToJsonString(request.getStrategyConfig()));
        }

        return entity;
    }

    private String convertToJsonString(Map<String, Object> strategyConfig) {
        return null;
    }

    private BacktestCreatedEvent mapToBacktestCreatedEvent(UUID backtestId, BacktestRequest request) {
        return new BacktestCreatedEvent(
                backtestId,
                getCurrentUserId(),
                request.getStrategyId(),
                request.getInstruments(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDataInterval().isPresent() ? String.valueOf(request.getDataInterval().get()) : "1d",
                request.getInitialCapital(),
                request.getStrategyConfig() != null ? request.getStrategyConfig() : Map.of()
        );
    }


    private long calculateEstimatedMinutes(BacktestRequest request) {
        // Simple heuristic for estimate:
        // 1. Calculate days in the range
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());

        // 2. Adjust based on data interval (daily is baseline)
        double intervalFactor = 1.0;
        if (request.getDataInterval().isPresent()) {
            if (request.getDataInterval().equals("1m")) {
                intervalFactor = 15.0;
            } else if (request.getDataInterval().equals("5m")) {
                intervalFactor = 10.0;
            } else if (request.getDataInterval().equals("15m")) {
                intervalFactor = 5.0;
            } else if (request.getDataInterval().equals("30m")) {
                intervalFactor = 3.0;
            } else if (request.getDataInterval().equals("1h")) {
                intervalFactor = 2.0;
            } else if (request.getDataInterval().equals("4h")) {
                intervalFactor = 1.5;
            } else if (request.getDataInterval().equals("1w")) {
                intervalFactor = 0.5;
            } else if (request.getDataInterval().equals("1mo")) {
                intervalFactor = 0.2;
            } else {
                intervalFactor = 1.0; // 1d
            }
        }

        // 3. Adjust based on number of instruments
        int numInstruments = request.getInstruments().size();

        // Base formula: 1 minute per 30 days per instrument, adjusted by interval
        return (long) Math.max(1, (days * numInstruments * intervalFactor) / 30);
    }

    private UUID getCurrentUserId() {
        // In a real application, this would come from the security context
        // For now, we'll use a placeholder
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
