package com.quantz.marketdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for tracking scraping operations
 */
@Entity
@Table(name = "scraping_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scrape_date", nullable = false)
    private LocalDate scrapeDate;

    @Column(name = "scraped_at", nullable = false)
    private LocalDateTime scrapedAt;

    @Column(name = "instruments_scraped")
    private Integer instrumentsScraped;

    @Column(name = "data_points_scraped")
    private Integer dataPointsScraped;

    @Column(name = "full_scrape", nullable = false)
    private Boolean fullScrape;

    @Column(length = 50)
    private String status;

    @Column(length = 2000)
    private String details;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
