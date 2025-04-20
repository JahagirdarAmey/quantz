package com.quantz.instruments.service;

import com.quantz.instruments.dto.InstrumentDataDto;
import com.quantz.instruments.dto.InstrumentListResponseDto;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service interface for instrument-related operations
 */
public interface InstrumentService {

    /**
     * Find instruments based on filtering criteria with pagination
     *
     * @param type Filter by instrument type (stock, etf, forex, crypto, futures, options)
     * @param exchange Filter by exchange
     * @param search Search term for finding instruments
     * @param limit Maximum number of results to return (default: 50, max: 200)
     * @param offset Number of results to skip (default: 0)
     * @return Paginated list of instruments matching criteria
     */
    InstrumentListResponseDto findInstruments(
            Optional<String> type,
            Optional<String> exchange,
            Optional<String> search,
            Optional<Integer> limit,
            Optional<Integer> offset);

    /**
     * Get historical data for a specific instrument
     *
     * @param instrumentId ID of the instrument
     * @param startDate Start date for historical data
     * @param endDate End date for historical data
     * @param interval Data interval (1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w, 1mo, default: 1d)
     * @return Historical price and volume data for the instrument
     */
    InstrumentDataDto getInstrumentData(
            String instrumentId,
            LocalDate startDate,
            LocalDate endDate,
            Optional<String> interval);
}