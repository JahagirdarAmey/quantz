package com.quantz.marketdata.service.impl;

import com.quantz.marketdata.entity.Instrument;
import com.quantz.marketdata.entity.ScrapingMetadata;
import com.quantz.marketdata.model.CandleData;
import com.quantz.marketdata.model.UpstoxInstrument;
import com.quantz.marketdata.repository.CandleDataRepository;
import com.quantz.marketdata.repository.InstrumentRepository;
import com.quantz.marketdata.repository.ScrapingMetadataRepository;
import com.quantz.marketdata.service.DataScraperService;
import com.quantz.marketdata.service.UpstoxAuthService;
import com.quantz.marketdata.service.UpstoxInstrumentService;
import com.quantz.marketdata.service.UpstoxMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataScraperServiceImpl implements DataScraperService {

    private final UpstoxAuthService authService;
    private final UpstoxInstrumentService instrumentService;
    private final UpstoxMarketDataService marketDataService;
    
    private final InstrumentRepository instrumentRepository;
    private final CandleDataRepository candleDataRepository;
    private final ScrapingMetadataRepository metadataRepository;
    
    private static final String DEFAULT_INTERVAL = "1d"; // Daily candles
    private static final List<String> EQUITY_SEGMENTS = List.of("NSE_EQ", "BSE_EQ");
    
    // Run at 4:00 PM on weekdays (Monday to Friday)
    @Scheduled(cron = "${data-scraper.cron:0 0 16 * * MON-FRI}")
    @Override
    public void scheduledScraping() {
        log.info("Starting scheduled market data scraping at {}", LocalDateTime.now());
        scrapeData();
    }

    @Override
    @Transactional
    public void manualScraping() {
        log.info("Starting manual market data scraping at {}", LocalDateTime.now());
        scrapeData();
    }

    @Transactional
    private void scrapeData() {
        try {
            // Ensure we have a valid token
            if (!authService.isTokenValid()) {
                log.error("No valid authentication token available. Aborting scraping.");
                saveFailedMetadata("AUTH_FAILED", "No valid authentication token available");
                return;
            }
            
            // Step 1: Fetch and update instruments
            int instrumentCount = scrapeInstruments();
            log.info("Fetched and updated {} instruments", instrumentCount);
            
            // Step 2: Determine date range for historical data
            ScrapingMetadata lastScrape = metadataRepository.findLatestScraping()
                    .orElse(null);
            
            LocalDate startDate;
            boolean isFirstRun = false;
            
            if (lastScrape == null) {
                // First time running - scrape 10 years of historical data
                startDate = LocalDate.now().minusYears(10);
                isFirstRun = true;
                log.info("First time scraping - starting from {}", startDate);
            } else {
                // Incremental scraping - start from the day after last scrape
                startDate = lastScrape.getScrapeDate().plusDays(1);
                log.info("Incremental scraping - starting from {}", startDate);
            }
            
            LocalDate endDate = LocalDate.now();
            
            // Don't proceed if we're already up to date
            if (startDate.isAfter(endDate)) {
                log.info("Data is already up to date. No scraping needed.");
                saveSuccessMetadata(endDate, instrumentCount, 0, isFirstRun);
                return;
            }
            
            // Step 3: Scrape historical data
            int dataPoints = scrapeHistoricalData(startDate, endDate);
            log.info("Scraped a total of {} data points", dataPoints);
            
            // Step 4: Save metadata
            saveSuccessMetadata(endDate, instrumentCount, dataPoints, isFirstRun);
            
        } catch (Exception e) {
            log.error("Error during data scraping: {}", e.getMessage(), e);
            saveFailedMetadata("ERROR", e.getMessage());
            throw new RuntimeException("Failed to scrape market data", e);
        }
    }

    private int scrapeInstruments() {
        AtomicInteger count = new AtomicInteger(0);
        
        log.info("Fetching all instruments...");
        List<UpstoxInstrument> upstoxInstruments = instrumentService.fetchAllInstruments();
        
        if (upstoxInstruments == null || upstoxInstruments.isEmpty()) {
            log.warn("No instruments fetched from Upstox");
            return 0;
        }
        
        log.info("Total instruments fetched from Upstox: {}", upstoxInstruments.size());
        
        // Convert to entities and save to database
        List<Instrument> instruments = upstoxInstruments.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        // Save in batches to avoid overwhelming the database
        List<List<Instrument>> batches = splitIntoBatches(instruments, 500);
        
        for (List<Instrument> batch : batches) {
            List<Instrument> savedInstruments = instrumentRepository.saveAll(batch);
            count.addAndGet(savedInstruments.size());
            log.info("Saved batch of {} instruments", savedInstruments.size());
        }
        
        return count.get();
    }
    
    private int scrapeHistoricalData(LocalDate startDate, LocalDate endDate) {
        AtomicInteger totalDataPoints = new AtomicInteger(0);
        
        // For initial run, we'll prioritize NSE equities to avoid overwhelming the API
        List<Instrument> instruments;
        
        if (startDate.isBefore(LocalDate.now().minusYears(1))) {
            // If we're doing a long historical scrape, only get equities
            instruments = instrumentRepository.findBySegmentAndInstrumentType("NSE_EQ", "EQ");
            log.info("Long-term historical scrape: focusing on {} NSE equities", instruments.size());
        } else {
            // For recent data, get all segments
            instruments = instrumentRepository.findAll();
            log.info("Short-term historical scrape: fetching data for all {} instruments", instruments.size());
        }
        
        // Process equity instruments first, then others
        List<Instrument> equityInstruments = instruments.stream()
                .filter(i -> EQUITY_SEGMENTS.contains(i.getSegment()))
                .collect(Collectors.toList());
        
        log.info("Processing {} equity instruments", equityInstruments.size());
        
        // Process instruments in batches for better performance and resource management
        List<List<Instrument>> batches = splitIntoBatches(equityInstruments, 50);
        
        for (List<Instrument> batch : batches) {
            // Process batch in parallel
            AtomicInteger batchPoints = new AtomicInteger(0);
            
            batch.parallelStream().forEach(instrument -> {
                try {
                    int points = scrapeInstrumentData(instrument, DEFAULT_INTERVAL, startDate, endDate);
                    batchPoints.addAndGet(points);
                    
                    // Small delay to avoid overwhelming the API
                    Thread.sleep(200);
                } catch (Exception e) {
                    log.error("Error scraping data for instrument {}: {}", 
                            instrument.getTradingSymbol(), e.getMessage());
                }
            });
            
            totalDataPoints.addAndGet(batchPoints.get());
            log.info("Completed batch of {} instruments, added {} data points", 
                    batch.size(), batchPoints.get());
            
            // Add a delay between batches to be respectful of API limits
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("Completed scraping equity data, total data points: {}", totalDataPoints.get());
        
        return totalDataPoints.get();
    }
    
    private int scrapeInstrumentData(Instrument instrument, String interval, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching {} data for instrument: {} ({}) from {} to {}", 
                interval, instrument.getTradingSymbol(), instrument.getInstrumentKey(), startDate, endDate);
        
        List<CandleData> candleData = marketDataService.fetchHistoricalCandleData(
                instrument.getInstrumentKey(), interval, startDate, endDate);
        
        if (candleData == null || candleData.isEmpty()) {
            log.info("No data available for {} between {} and {}", 
                    instrument.getTradingSymbol(), startDate, endDate);
            return 0;
        }
        
        // Convert to entities and save
        List<com.quantz.marketdata.entity.CandleData> entities = candleData.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        // Save in batches
        List<List<com.quantz.marketdata.entity.CandleData>> batches = splitIntoBatches(entities, 200);
        
        int savedCount = 0;
        for (List<com.quantz.marketdata.entity.CandleData> batch : batches) {
            List<com.quantz.marketdata.entity.CandleData> saved = candleDataRepository.saveAll(batch);
            savedCount += saved.size();
        }
        
        log.info("Successfully scraped and saved {} data points for {} ({})", 
                savedCount, instrument.getTradingSymbol(), instrument.getInstrumentKey());
        
        return savedCount;
    }
    
    private Instrument convertToEntity(UpstoxInstrument upstoxInstrument) {
        return Instrument.builder()
                .instrumentKey(upstoxInstrument.getInstrumentKey())
                .exchange(upstoxInstrument.getExchange())
                .segment(upstoxInstrument.getSegment())
                .name(upstoxInstrument.getName())
                .isin(upstoxInstrument.getIsin())
                .instrumentType(upstoxInstrument.getInstrumentType())
                .tradingSymbol(upstoxInstrument.getTradingSymbol())
                .exchangeToken(upstoxInstrument.getExchangeToken())
                .lotSize(upstoxInstrument.getLotSize())
                .tickSize(upstoxInstrument.getTickSize())
                .expiry(upstoxInstrument.getExpiry())
                .strike(upstoxInstrument.getStrike())
                .optionType(upstoxInstrument.getOptionType())
                .build();
    }
    
    private com.quantz.marketdata.entity.CandleData convertToEntity(CandleData candleData) {
        return com.quantz.marketdata.entity.CandleData.builder()
                .instrumentKey(candleData.getInstrumentKey())
                .interval(candleData.getInterval())
                .timestamp(candleData.getTimestamp())
                .open(candleData.getOpen())
                .high(candleData.getHigh())
                .low(candleData.getLow())
                .close(candleData.getClose())
                .volume(candleData.getVolume())
                .build();
    }
    
    private <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            int end = Math.min(items.size(), i + batchSize);
            batches.add(items.subList(i, end));
        }
        return batches;
    }
    
    private void saveSuccessMetadata(LocalDate scrapeDate, int instrumentCount, int dataPoints, boolean isFullScrape) {
        ScrapingMetadata metadata = ScrapingMetadata.builder()
                .scrapeDate(scrapeDate)
                .scrapedAt(LocalDateTime.now())
                .instrumentsScraped(instrumentCount)
                .dataPointsScraped(dataPoints)
                .fullScrape(isFullScrape)
                .status("COMPLETED")
                .build();
        
        metadataRepository.save(metadata);
        log.info("Scraping operation completed successfully");
    }
    
    private void saveFailedMetadata(String status, String details) {
        ScrapingMetadata metadata = ScrapingMetadata.builder()
                .scrapeDate(LocalDate.now())
                .scrapedAt(LocalDateTime.now())
                .instrumentsScraped(0)
                .dataPointsScraped(0)
                .fullScrape(false)
                .status(status)
                .details(details)
                .build();
        
        metadataRepository.save(metadata);
        log.error("Scraping operation failed: {}", details);
    }
}