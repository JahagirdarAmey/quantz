package com.quantz.marketdata.service;

import com.quantz.marketdata.model.CandleData;
import com.quantz.marketdata.model.LtpQuoteData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface for Upstox market data service
 */
public interface UpstoxMarketDataService {
    
    /**
     * Fetch historical candle data for an instrument
     */
    List<CandleData> fetchHistoricalCandleData(
            String instrumentKey, String interval, LocalDate fromDate, LocalDate toDate);
    
    /**
     * Fetch intraday candle data for an instrument
     */
    List<CandleData> fetchIntradayCandleData(String instrumentKey, String interval);
    
    /**
     * Get LTP quotes for multiple instruments
     */
    Map<String, LtpQuoteData> getLtpQuotes(List<String> instrumentKeys);
    
    /**
     * Connect to market data websocket for real-time updates
     */
    void connectToMarketDataStream(List<String> instrumentKeys);
}