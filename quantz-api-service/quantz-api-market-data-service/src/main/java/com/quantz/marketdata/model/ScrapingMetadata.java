package com.quantz.marketdata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model to track scraping metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingMetadata {
    private Long id;
    private LocalDate scrapeDate;
    private LocalDateTime scrapedAt;
    private Integer instrumentsScraped;
    private Integer dataPointsScraped;
    private Boolean fullScrape;
    private String status;
    private String details;
}