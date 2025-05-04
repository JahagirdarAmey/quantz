package com.quantz.marketdata.repository;

import com.quantz.marketdata.entity.ScrapingMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ScrapingMetadataRepository extends JpaRepository<ScrapingMetadata, Long> {

    @Query("SELECT sm FROM ScrapingMetadata sm ORDER BY sm.scrapeDate DESC")
    Optional<ScrapingMetadata> findLatestScraping();

    Optional<ScrapingMetadata> findByScrapeDate(LocalDate scrapeDate);

    @Query("SELECT sm FROM ScrapingMetadata sm WHERE sm.fullScrape = true ORDER BY sm.scrapeDate DESC")
    Optional<ScrapingMetadata> findLatestFullScraping();
}

