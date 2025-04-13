package com.quantz.backtest.service;

import com.quantz.backtest.exception.BadRequestException;
import com.quantz.backtest.exception.ResourceNotFoundException;
import com.quantz.backtest.exception.UnauthorizedException;
import com.quantz.backtest.mapper.*;
import com.quantz.backtest.model.*;
import com.quantz.backtest.entity.BacktestEntity;
import com.quantz.backtest.repository.BacktestRepository;
import com.quantz.backtest.validator.BacktestRequestValidator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for backtest operations
 */
@Service
@Validated
@AllArgsConstructor
@Slf4j
public class BacktestService {

    private final BacktestRepository backtestRepository;
    private final BacktestMapper backtestMapper;
    private final BacktestDetailMapper detailMapper;
    private final BacktestSummaryMapper summaryMapper;
    private final BacktestResponseMapper responseMapper;
    private final ListBacktestsResponseMapper listResponseMapper;
    private final BacktestRequestValidator validator;
    private final UserContextService userContextService;
    private final BacktestEventProducer eventProducer;
    private final BacktestCompletionTimeCalculator completionTimeCalculator;
    private final StrategyService strategyService;

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

        validator.validate(backtestRequest);

        UUID backtestId = UUID.randomUUID();
        UUID userId = userContextService.getCurrentUserId();

        BacktestEntity entity = backtestMapper.toEntity(backtestRequest, backtestId, userId);
        backtestRepository.save(entity);
        log.debug("Saved backtest entity with ID: {}", backtestId);

        eventProducer.publishBacktestCreatedEvent(backtestRequest, backtestId, userId);

        return responseMapper.toCreationResponse(
                backtestId,
                completionTimeCalculator.calculateEstimatedCompletionTime(backtestRequest)
        );
    }

    /**
     * Get a backtest by ID
     *
     * @param backtestId the ID of the backtest to retrieve
     * @return the backtest details
     * @throws ResourceNotFoundException if the backtest doesn't exist
     * @throws UnauthorizedException     if the current user doesn't own the backtest
     */
    @Transactional(readOnly = true)
    public BacktestDetail getBacktest(UUID backtestId) {
        log.info("Retrieving backtest with ID: {}", backtestId);

        UUID currentUserId = userContextService.getCurrentUserId();

        BacktestEntity backtest = backtestRepository.findById(backtestId.toString())
                .orElseThrow(() -> {
                    log.warn("Backtest with ID {} not found", backtestId);
                    return new ResourceNotFoundException("Backtest not found with ID: " + backtestId);
                });

        UUID backtestUserId = UUID.fromString(backtest.getUserId());
        if (!backtestUserId.equals(currentUserId)) {
            log.warn("User {} attempted to access backtest {} owned by {}",
                    currentUserId, backtestId, backtestUserId);
            throw new UnauthorizedException("You don't have permission to access this backtest");
        }

        BacktestDetail detail = detailMapper.toBacktestDetail(backtest);

        try {
            String strategyName = strategyService.getStrategyName(UUID.fromString(backtest.getStrategyId()));
            detail.setStrategyName(Optional.ofNullable(strategyName));
        } catch (Exception e) {
            log.warn("Could not retrieve strategy name for strategy ID: {}", backtest.getStrategyId(), e);
        }

        log.debug("Successfully retrieved backtest with ID: {}", backtestId);
        return detail;
    }

    /**
     * List backtests with optional filtering and pagination
     *
     * @param status optional status filter
     * @param limit  maximum number of results to return
     * @param offset pagination offset
     * @return paginated list of backtests with metadata
     */
    @Transactional(readOnly = true)
    public ListBacktests200Response listBacktests(String status, @Min(1) @Max(100) int limit, @Min(0) int offset) {
        log.info("Listing backtests with status filter: {}, limit: {}, offset: {}", status, limit, offset);

        UUID currentUserId = userContextService.getCurrentUserId();
        String userIdStr = currentUserId.toString();

        // Query backtests
        List<BacktestEntity> backtestEntities = backtestRepository.findBacktestsForUser(
                userIdStr, status, limit, offset);

        // Count total matching backtests
        int totalCount = backtestRepository.countBacktestsForUser(userIdStr, status);

        // Map entities to summaries
        List<BacktestSummary> summaries = summaryMapper.toBacktestSummaryList(backtestEntities);

        // Enrich with strategy names if possible
        enrichStrategyNames(summaries);

        // Create response
        ListBacktests200Response response = listResponseMapper.toListResponse(summaries, totalCount, limit, offset);

        log.debug("Found {} backtests out of {} total", summaries.size(), totalCount);
        return response;
    }

    /**
     * Enrich backtest summaries with strategy names
     */
    private void enrichStrategyNames(List<BacktestSummary> summaries) {
        summaries.forEach(summary -> {
            try {
                String strategyName = strategyService.getStrategyName(summary.getStrategyId());
                summary.setStrategyName(Optional.ofNullable(strategyName));
            } catch (Exception e) {
                log.warn("Could not retrieve strategy name for strategy ID: {}", summary.getStrategyId(), e);
            }
        });
    }

    /**
     * Delete a backtest by ID
     *
     * @param backtestId the ID of the backtest to delete
     * @throws ResourceNotFoundException if the backtest doesn't exist
     * @throws UnauthorizedException     if the current user doesn't own the backtest
     * @throws BadRequestException       if the backtest is in a state that can't be deleted
     */
    @Transactional
    public void deleteBacktest(UUID backtestId) {
        log.info("Deleting backtest with ID: {}", backtestId);

        UUID currentUserId = userContextService.getCurrentUserId();

        BacktestEntity backtest = backtestRepository.findById(backtestId.toString())
                .orElseThrow(() -> {
                    log.warn("Backtest with ID {} not found", backtestId);
                    return new ResourceNotFoundException("Backtest not found with ID: " + backtestId);
                });

        UUID backtestUserId = UUID.fromString(backtest.getUserId());
        if (!backtestUserId.equals(currentUserId)) {
            log.warn("User {} attempted to delete backtest {} owned by {}",
                    currentUserId, backtestId, backtestUserId);
            throw new UnauthorizedException("You don't have permission to delete this backtest");
        }

        String status = backtest.getStatus();
        if ("RUNNING".equals(status)) {
            log.warn("Cannot delete backtest {} with status RUNNING", backtestId);
            throw new BadRequestException("Cannot delete a backtest that is currently running");
        }

        backtestRepository.deleteById(backtestId.toString());

        eventProducer.publishBacktestDeletedEvent(backtestId, currentUserId);

        log.info("Successfully deleted backtest with ID: {}", backtestId);
    }
}