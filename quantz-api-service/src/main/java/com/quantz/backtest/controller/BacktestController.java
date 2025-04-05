package com.quantz.backtest.controller;

import com.quantz.backtest.api.BacktestApi;
import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestDetail;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.model.ListBacktests200Response;
import com.quantz.backtest.service.BacktestService;
import jakarta.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

public class BacktestController implements BacktestApi {

    private final BacktestService backtestService;

    @Autowired
    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    @Override
    public ResponseEntity<BacktestCreationResponse> createBacktest(BacktestRequest backtestRequest) {
        BacktestCreationResponse response = backtestService.createBacktest(backtestRequest);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<Void> deleteBacktest(UUID backtestId) {
        this.backtestService.deleteBacktest(backtestId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BacktestDetail> getBacktest(UUID backtestId) {
        BacktestDetail detail = backtestService.getBacktest(backtestId);
        return new ResponseEntity<>(detail, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListBacktests200Response> listBacktests(Optional<String> status, Optional<@Max(100) Integer> limit, Optional<Integer> offset) {
        // Extract values from Optional with defaults
        int actualLimit = limit.orElse(20);
        int actualOffset = offset.orElse(0);
        String statusFilter = status.orElse(null);

        // Call service with actual values, not Optionals
        ListBacktests200Response response = backtestService.listBacktests(statusFilter, actualLimit, actualOffset);
        return ResponseEntity.ok(response);
    }
}
