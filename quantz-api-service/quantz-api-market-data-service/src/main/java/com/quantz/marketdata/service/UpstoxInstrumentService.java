package com.quantz.marketdata.service;

import com.quantz.marketdata.model.UpstoxInstrument;

import java.util.List;

/**
 * Interface for Upstox instruments service
 */
public interface UpstoxInstrumentService {

    /**
     * Fetch all instrument definitions from Upstox
     */
    List<UpstoxInstrument> fetchAllInstruments();

    /**
     * Fetch instruments by exchange
     */
    List<UpstoxInstrument> fetchInstrumentsByExchange(String exchange);

    /**
     * Fetch instruments by segment (e.g., NSE_EQ, NSE_FO)
     */
    List<UpstoxInstrument> fetchInstrumentsBySegment(String segment);

    /**
     * Get instrument by instrument key
     */
    UpstoxInstrument getInstrumentByKey(String instrumentKey);

    /**
     * Save instruments to database
     */
    int saveInstruments(List<UpstoxInstrument> instruments);
}
