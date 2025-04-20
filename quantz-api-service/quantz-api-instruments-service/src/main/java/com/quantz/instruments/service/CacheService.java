package com.quantz.instruments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service responsible for managing cache invalidation strategies
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    public void evictInstrumentDataCache(String instrumentId) {
        log.info("Evicting cache for instrument: {}", instrumentId);
        Objects.requireNonNull(cacheManager.getCache("instrumentData"))
                .evict("instrumentData:" + instrumentId + ":*");
    }

    public void evictInstrumentListCache() {
        log.info("Evicting all instrument listing caches");
        Objects.requireNonNull(cacheManager.getCache("instrumentListings")).clear();
    }
    

    @Scheduled(fixedRate = 60000) // Every minute
    public void evictPriceDataCache() {
        log.info("Scheduled eviction of price data cache");
        Objects.requireNonNull(cacheManager.getCache("priceData")).clear();
    }

    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    public void evictMetadataCache() {
        log.info("Scheduled eviction of instrument metadata cache");
        Objects.requireNonNull(cacheManager.getCache("instrumentMetadata")).clear();
    }
}