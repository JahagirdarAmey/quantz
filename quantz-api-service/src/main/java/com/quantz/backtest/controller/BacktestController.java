package com.quantz.backtest.controller;

import com.quantz.backtest.api.BacktestApi;
import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestDetail;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.model.ListBacktests200Response;
import com.quantz.backtest.service.BacktestService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for backtest operations
 */
@RestController
@RequestMapping("/api/backtests")
@Validated
@Slf4j
@AllArgsConstructor
public class BacktestController implements BacktestApi {

    private final BacktestService backtestService;

    /**
     * Create a new backtest
     *
     * @param request the backtest request
     * @return the created backtest's ID and status
     */
    @PostMapping
    @Override
    public ResponseEntity<BacktestCreationResponse> createBacktest(@Valid @RequestBody BacktestRequest request) {
        log.debug("REST request to create backtest for strategy: {}", request.getStrategyId());
        BacktestCreationResponse response = backtestService.createBacktest(request);
        return ResponseEntity.ok(response);
    }


    /**
     * List backtests with optional filtering and pagination
     *
     * @param status optional status filter
     * @param limit maximum number of results to return (defaults to 20)
     * @param offset pagination offset (defaults to 0)
     * @return paginated list of backtests with metadata
     */
    @GetMapping
    @Override
    public ResponseEntity<ListBacktests200Response> listBacktests(
            @RequestParam Optional<String> status,
            @RequestParam Optional<@Max(100) Integer> limit,
            @RequestParam Optional<Integer> offset) {

        // Extract values from Optional with defaults
        int actualLimit = limit.orElse(20);
        int actualOffset = offset.orElse(0);
        String statusFilter = status.orElse(null);

        log.debug("REST request to list backtests with status: {}, limit: {}, offset: {}",
                statusFilter, actualLimit, actualOffset);

        // Call service with actual values, not Optionals
        ListBacktests200Response response = backtestService.listBacktests(statusFilter, actualLimit, actualOffset);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a backtest's details by ID
     *
     * @param backtestId the ID of the backtest to retrieve
     * @return the backtest details
     */
    @GetMapping("/{backtestId}")
    @Override
    public ResponseEntity<BacktestDetail> getBacktest(@PathVariable UUID backtestId) {
        log.debug("REST request to get backtest: {}", backtestId);
        BacktestDetail detail = backtestService.getBacktest(backtestId);
        return new ResponseEntity<>(detail, HttpStatus.OK);
    }

    /**
     * Delete a backtest
     *
     * @param backtestId the ID of the backtest to delete
     * @return no content on success
     */
    @DeleteMapping("/{backtestId}")
    @Override
    public ResponseEntity<Void> deleteBacktest(@PathVariable UUID backtestId) {
        log.debug("REST request to delete backtest: {}", backtestId);
        backtestService.deleteBacktest(backtestId);
        return ResponseEntity.noContent().build();
    }
}
