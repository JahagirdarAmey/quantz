package com.quantz.marketdata.controller;

import com.quantz.marketdata.entity.CandleData;
import com.quantz.marketdata.entity.Instrument;
import com.quantz.marketdata.entity.ScrapingMetadata;
import com.quantz.marketdata.repository.CandleDataRepository;
import com.quantz.marketdata.repository.InstrumentRepository;
import com.quantz.marketdata.repository.ScrapingMetadataRepository;
import com.quantz.marketdata.service.DataScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController {

    private final DataScraperService dataScraperService;
    private final InstrumentRepository instrumentRepository;
    private final CandleDataRepository candleDataRepository;
    private final ScrapingMetadataRepository metadataRepository;

    @PostMapping("/scrape")
    public ResponseEntity<String> triggerScraping() {
        try {
            dataScraperService.manualScraping();
            return ResponseEntity.ok("Market data scraping started successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error starting scraping: " + e.getMessage());
        }
    }

    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getInstruments(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String instrumentType,
            @RequestParam(required = false) String search) {

        List<Instrument> instruments;

        if (exchange != null && !exchange.isEmpty()) {
            instruments = instrumentRepository.findByExchange(exchange);
        } else if (segment != null && !segment.isEmpty()) {
            instruments = instrumentRepository.findBySegment(segment);
        } else if (instrumentType != null && !instrumentType.isEmpty()) {
            instruments = instrumentRepository.findByInstrumentType(instrumentType);
        } else if (search != null && !search.isEmpty()) {
            instruments = instrumentRepository.searchByNameOrSymbol(search);
        } else {
            instruments = instrumentRepository.findAll();
        }

        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/instruments/{instrumentKey}")
    public ResponseEntity<Instrument> getInstrument(@PathVariable String instrumentKey) {
        return instrumentRepository.findById(instrumentKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/candles/{instrumentKey}")
    public ResponseEntity<List<CandleData>> getCandleData(
            @PathVariable String instrumentKey,
            @RequestParam(required = false, defaultValue = "1d") String interval,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<CandleData> candleData;

        if (startTime != null && endTime != null) {
            candleData = candleDataRepository.findByInstrumentKeyAndIntervalAndTimestampBetweenOrderByTimestampAsc(
                    instrumentKey, interval, startTime, endTime);
        } else {
            candleData = candleDataRepository.findByInstrumentKeyAndIntervalOrderByTimestampAsc(
                    instrumentKey, interval);
        }

        return ResponseEntity.ok(candleData);
    }

    @GetMapping("/scraping-history")
    public ResponseEntity<List<ScrapingMetadata>> getScrapingHistory() {
        return ResponseEntity.ok(metadataRepository.findAll());
    }

    @GetMapping("/scraping-history/latest")
    public ResponseEntity<ScrapingMetadata> getLatestScraping() {
        Optional<ScrapingMetadata> latestScraping = metadataRepository.findLatestScraping();
        return latestScraping
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}