package com.quantz.backtest.controller;

import com.quantz.api.BacktestApi;
import com.quantz.model.BacktestCreationResponse;
import com.quantz.model.BacktestDetail;
import com.quantz.model.BacktestRequest;
import com.quantz.model.ListBacktests200Response;
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

    @Override
    public ResponseEntity<BacktestCreationResponse> _createBacktest(BacktestRequest backtestRequest) {
        log.debug("REST request to create backtest for strategy: {}", backtestRequest.getStrategyId());
        BacktestCreationResponse response = backtestService.createBacktest(backtestRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> _deleteBacktest(UUID backtestId) {
        log.debug("REST request to delete backtest: {}", backtestId);
        backtestService.deleteBacktest(backtestId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BacktestDetail> _getBacktest(UUID backtestId) {
        log.debug("REST request to get backtest: {}", backtestId);
        BacktestDetail detail = backtestService.getBacktest(backtestId);
        return new ResponseEntity<>(detail, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListBacktests200Response> _listBacktests(Optional<String> status, Optional<@Max(100) Integer> limit, Optional<Integer> offset) {
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
}
