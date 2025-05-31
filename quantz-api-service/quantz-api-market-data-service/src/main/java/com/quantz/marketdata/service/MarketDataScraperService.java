package com.quantz.marketdata.service;

import com.quantz.marketdata.entity.Instrument;
import com.quantz.marketdata.entity.CandleData;
import com.quantz.marketdata.entity.ScrapingMetadata;

import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface for the Data Scraper Service
 */
public interface MarketDataScraperService {

    /**
     * Scheduled method to scrape data
     */
    void scheduledScraping();

    /**
     * Manually triggered method to scrape data
     */
    void manualScraping();


    List<Instrument> findInstruments(String exchange, String segment, String instrumentType, String search);

    Optional<Instrument> findInstrumentByKey(String instrumentKey);

    List<CandleData> findCandleData(String instrumentKey, String interval, LocalDateTime startTime, LocalDateTime endTime);

    List<ScrapingMetadata> getScrapingHistory();

    Optional<ScrapingMetadata> getLatestScrapingMetadata();
}