package com.quantz.instruments.events;

import com.quantz.instruments.service.CacheService;
import com.quantz.instruments.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentEventConsumer {

    private final InstrumentService instrumentService;
    private final CacheService cacheService;

    @KafkaListener(topics = "market-data-updates", groupId = "instruments-service")
    public void consumePriceUpdates(InstrumentEvent event) {
        log.info("Received price update event: {}", event);
        
        if (event.getEventType() == EventType.PRICE_UPDATED) {
            String instrumentId = event.getInstrumentId();
            
            // Evict any cached data for this instrument
            cacheService.evictInstrumentDataCache(instrumentId);
            
            // Process the price update (e.g., store in database)
            // This would typically call a method in the service layer
            log.info("Processing price update for instrument: {}", instrumentId);
        }
    }
    
    @KafkaListener(topics = "instrument-updates", groupId = "instruments-service")
    public void consumeInstrumentUpdates(InstrumentEvent event) {
        log.info("Received instrument update event: {}", event);
        
        String instrumentId = event.getInstrumentId();
        
        switch (event.getEventType()) {
            case INSTRUMENT_CREATED:
            case INSTRUMENT_UPDATED:
            case INSTRUMENT_DELETED:
                // Evict cached instrument listings
                cacheService.evictInstrumentListCache();
                log.info("Evicted instrument list cache due to {} event", event.getEventType());
                break;
            default:
                log.warn("Unhandled event type: {}", event.getEventType());
        }
    }
    
    @KafkaListener(topics = "instruments-dlt", groupId = "instruments-service-dlt")
    public void processDLT(InstrumentEvent failedEvent) {
        log.error("Processing dead letter event: {}", failedEvent);
        // Implement dead letter queue handling logic
        // Could include:
        // 1. Logging the failed event
        // 2. Sending alerts
        // 3. Attempting specialized recovery based on event type
        // 4. Storing in a separate database for manual review
    }
}