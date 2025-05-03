package com.quantz.marketdata.service;

/**
 * Interface for the Data Scraper Service
 */
public interface DataScraperService {
    
    /**
     * Scheduled method to scrape data
     */
    void scheduledScraping();
    
    /**
     * Manually triggered method to scrape data
     */
    void manualScraping();
}