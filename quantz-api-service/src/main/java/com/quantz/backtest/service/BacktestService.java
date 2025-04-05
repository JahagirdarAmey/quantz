package com.quantz.backtest.service;

import com.quantz.backtest.model.BacktestCreationResponse;
import com.quantz.backtest.model.BacktestDetail;
import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.model.ListBacktests200Response;

import java.util.Optional;
import java.util.UUID;

public class BacktestService {
    public BacktestCreationResponse createBacktest(BacktestRequest backtestRequest) {
        return null;
    }

    public Void deleteBacktest(UUID backtestId) {
        return null;
    }

    public BacktestDetail getBacktest(UUID backtestId) {
        return null;
    }

    public ListBacktests200Response listBacktests(String status, Integer limit, Integer offset) {
        return null;
    }
}
