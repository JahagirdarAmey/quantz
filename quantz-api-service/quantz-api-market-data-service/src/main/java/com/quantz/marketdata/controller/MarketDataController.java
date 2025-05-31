package com.quantz.marketdata.controller;

import com.quantz.marketdata.entity.CandleData;
import com.quantz.marketdata.entity.Instrument;
import com.quantz.marketdata.entity.ScrapingMetadata;
import com.quantz.marketdata.service.MarketDataScraperService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/market-data")
@AllArgsConstructor
public class MarketDataController {

    private final MarketDataScraperService marketDataScraperService;

    @PostMapping("/scrape")
    public ResponseEntity<String> triggerScraping() {
        marketDataScraperService.manualScraping();
        return ResponseEntity.ok("Market data scraping started successfully");
    }

    @GetMapping("/instruments")
    public ResponseEntity<List<Instrument>> getInstruments(
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String instrumentType,
            @RequestParam(required = false) String search) {
        List<Instrument> instruments = marketDataScraperService.findInstruments(exchange, segment, instrumentType, search);
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/instruments/{instrumentKey}")
    public ResponseEntity<Instrument> getInstrument(@PathVariable String instrumentKey) {
        return marketDataScraperService.findInstrumentByKey(instrumentKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/candles/{instrumentKey}")
    public ResponseEntity<List<CandleData>> getCandleData(
            @PathVariable String instrumentKey,
            @RequestParam(required = false, defaultValue = "1d") String interval,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<CandleData> candleData = marketDataScraperService.findCandleData(instrumentKey, interval, startTime, endTime);
        return ResponseEntity.ok(candleData);
    }


    @GetMapping("/scraping-history")
    public ResponseEntity<List<ScrapingMetadata>> getScrapingHistory() {
        List<ScrapingMetadata> history = marketDataScraperService.getScrapingHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/scraping-history/latest")
    public ResponseEntity<ScrapingMetadata> getLatestScraping() {
        return marketDataScraperService.getLatestScrapingMetadata()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}