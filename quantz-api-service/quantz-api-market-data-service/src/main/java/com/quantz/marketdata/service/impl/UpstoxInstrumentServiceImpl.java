package com.quantz.marketdata.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantz.marketdata.config.UpstoxProperties;
import com.quantz.marketdata.entity.Instrument;
import com.quantz.marketdata.model.UpstoxInstrument;
import com.quantz.marketdata.repository.InstrumentRepository;
import com.quantz.marketdata.service.UpstoxHttpClient;
import com.quantz.marketdata.service.UpstoxInstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpstoxInstrumentServiceImpl implements UpstoxInstrumentService {

    private final UpstoxProperties upstoxProperties;
    private final InstrumentRepository instrumentRepository;
    private final UpstoxHttpClient upstoxHttpClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    // In-memory cache for instruments
    private final Map<String, UpstoxInstrument> instrumentCache = new ConcurrentHashMap<>();
    private LocalDateTime lastCacheRefresh = null;

    @Override
    @Cacheable(value = "instruments", unless = "#result == null || #result.isEmpty()")
    public List<UpstoxInstrument> fetchAllInstruments() {
        // Check if cache is valid
        if (!isCacheValid()) {
            refreshInstrumentCache();
        }

        // Return all instruments from cache
        return new ArrayList<>(instrumentCache.values());
    }

    @Override
    @Cacheable(value = "instrumentsByExchange", key = "#exchange", unless = "#result == null || #result.isEmpty()")
    public List<UpstoxInstrument> fetchInstrumentsByExchange(String exchange) {
        // Check if cache is valid
        if (!isCacheValid()) {
            refreshInstrumentCache();
        }

        // Filter instruments by exchange
        return instrumentCache.values().stream()
                .filter(instrument -> exchange.equals(instrument.getExchange()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "instrumentsBySegment", key = "#segment", unless = "#result == null || #result.isEmpty()")
    public List<UpstoxInstrument> fetchInstrumentsBySegment(String segment) {
        // Check if cache is valid
        if (!isCacheValid()) {
            refreshInstrumentCache();
        }

        // Filter instruments by segment
        return instrumentCache.values().stream()
                .filter(instrument -> segment.equals(instrument.getSegment()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "instrumentByKey", key = "#instrumentKey", unless = "#result == null")
    public UpstoxInstrument getInstrumentByKey(String instrumentKey) {
        // Check if cache is valid
        if (!isCacheValid()) {
            refreshInstrumentCache();
        }

        // Get instrument by key
        return instrumentCache.get(instrumentKey);
    }

    @Override
    @Transactional
    public int saveInstruments(List<UpstoxInstrument> instruments) {
        if (instruments == null || instruments.isEmpty()) {
            return 0;
        }

        // Update in-memory cache
        instruments.forEach(instrument ->
                instrumentCache.put(instrument.getInstrumentKey(), instrument));

        // Convert to entity objects
        List<Instrument> entityInstruments = instruments.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        // Save to database in batches to avoid overwhelming the database
        int totalSaved = saveBatches(entityInstruments, 500);

        log.info("Saved {} instruments to database", totalSaved);
        return totalSaved;
    }

    /**
     * Clear and refresh the instrument cache daily at 6 AM
     */
    @Scheduled(cron = "0 0 6 * * *")
    @CacheEvict(value = {"instruments", "instrumentsByExchange", "instrumentsBySegment", "instrumentByKey"}, allEntries = true)
    public void scheduledCacheRefresh() {
        log.info("Scheduled refresh of instrument cache");
        refreshInstrumentCache();
    }

    /**
     * Refresh the instrument cache with data from Upstox
     */
    private synchronized void refreshInstrumentCache() {
        log.info("Refreshing instrument cache");
        instrumentCache.clear();

        try {
            // Load NSE instruments
            List<UpstoxInstrument> nseInstruments = loadInstrumentsFromUrl(
                    upstoxProperties.getInstruments().getBodInstrumentsUrl());
            if (nseInstruments != null && !nseInstruments.isEmpty()) {
                nseInstruments.forEach(instrument ->
                        instrumentCache.put(instrument.getInstrumentKey(), instrument));
                log.info("Loaded {} NSE instruments", nseInstruments.size());
            }

            // Load NSE F&O instruments
            List<UpstoxInstrument> nfoInstruments = loadInstrumentsFromUrl(
                    upstoxProperties.getInstruments().getNfoInstrumentsUrl());
            if (nfoInstruments != null && !nfoInstruments.isEmpty()) {
                nfoInstruments.forEach(instrument ->
                        instrumentCache.put(instrument.getInstrumentKey(), instrument));
                log.info("Loaded {} NSE F&O instruments", nfoInstruments.size());
            }

            // Load BSE instruments
            List<UpstoxInstrument> bseInstruments = loadInstrumentsFromUrl(
                    upstoxProperties.getInstruments().getBseInstrumentsUrl());
            if (bseInstruments != null && !bseInstruments.isEmpty()) {
                bseInstruments.forEach(instrument ->
                        instrumentCache.put(instrument.getInstrumentKey(), instrument));
                log.info("Loaded {} BSE instruments", bseInstruments.size());
            }

            // Load BSE F&O instruments if URL is available
            if (upstoxProperties.getInstruments().getBfoInstrumentsUrl() != null) {
                List<UpstoxInstrument> bfoInstruments = loadInstrumentsFromUrl(
                        upstoxProperties.getInstruments().getBfoInstrumentsUrl());
                if (bfoInstruments != null && !bfoInstruments.isEmpty()) {
                    bfoInstruments.forEach(instrument ->
                            instrumentCache.put(instrument.getInstrumentKey(), instrument));
                    log.info("Loaded {} BSE F&O instruments", bfoInstruments.size());
                }
            }

            // Load MCX instruments if URL is available
            if (upstoxProperties.getInstruments().getMcxInstrumentsUrl() != null) {
                List<UpstoxInstrument> mcxInstruments = loadInstrumentsFromUrl(
                        upstoxProperties.getInstruments().getMcxInstrumentsUrl());
                if (mcxInstruments != null && !mcxInstruments.isEmpty()) {
                    mcxInstruments.forEach(instrument ->
                            instrumentCache.put(instrument.getInstrumentKey(), instrument));
                    log.info("Loaded {} MCX instruments", mcxInstruments.size());
                }
            }

            // Update cache timestamp
            lastCacheRefresh = LocalDateTime.now();

            log.info("Instrument cache refreshed, total instruments: {}", instrumentCache.size());
        } catch (Exception e) {
            log.error("Error refreshing instrument cache: {}", e.getMessage(), e);
        }
    }

    /**
     * Load instruments from a URL
     */
    private List<UpstoxInstrument> loadInstrumentsFromUrl(String url) {
        try {
            // Directly use RestTemplate instead of UpstoxHttpClient since these URLs don't require authentication
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<List<UpstoxInstrument>>() {
                        });
            } else {
                log.error("Failed to load instruments from {}: {}", url, response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error loading instruments from {}: {}", url, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Check if the cache is still valid
     */
    private boolean isCacheValid() {
        if (lastCacheRefresh == null || instrumentCache.isEmpty()) {
            return false;
        }

        // Check if cache has expired
        return LocalDateTime.now().isBefore(
                lastCacheRefresh.plusMinutes(upstoxProperties.getInstruments().getCacheExpiryMinutes()));
    }

    /**
     * Convert from model to entity
     */
    private Instrument convertToEntity(UpstoxInstrument model) {
        return Instrument.builder()
                .instrumentKey(model.getInstrumentKey())
                .exchange(model.getExchange())
                .segment(model.getSegment())
                .name(model.getName())
                .isin(model.getIsin())
                .instrumentType(model.getInstrumentType())
                .tradingSymbol(model.getTradingSymbol())
                .exchangeToken(model.getExchangeToken())
                .lotSize(model.getLotSize())
                .tickSize(model.getTickSize())
                .expiry(model.getExpiry())
                .strike(model.getStrike())
                .optionType(model.getOptionType())
                .build();
    }

    /**
     * Save instruments in batches to avoid OOM
     */
    private int saveBatches(List<Instrument> instruments, int batchSize) {
        int total = 0;

        for (int i = 0; i < instruments.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, instruments.size());
            List<Instrument> batch = instruments.subList(i, endIndex);

            List<Instrument> saved = instrumentRepository.saveAll(batch);
            total += saved.size();

            log.debug("Saved batch of {} instruments, progress: {}/{}",
                    saved.size(), total, instruments.size());
        }

        return total;
    }
}