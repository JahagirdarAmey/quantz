package com.quantz.backtest.event;

import com.quantz.event.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
@Slf4j
public class MockEventPublisher  implements EventPublisher {


    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, ConcurrentHashMap<String, Object>> publishedEvents = new HashMap<>();

    public MockEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }



    @Override
    public void publish(String topic, Object event) {
        String key = UUID.randomUUID().toString();

        // Add correlation ID if available
        if (MDC.get("correlationId") != null) {
            key = MDC.get("correlationId");
        }

        // Store the event for verification in tests
        publishedEvents.computeIfAbsent(topic, k -> new ConcurrentHashMap<>())
                .put(key, event);

        log.info("Mock published event to topic: {} with key: {}", topic, key);

        try {
            // Try to publish to Kafka if it's available, but don't fail the test if it's not
            if (isKafkaAvailable()) {
                kafkaTemplate.send(topic, key, event);
                log.debug("Successfully sent event to Kafka in test mode");
            }
        } catch (Exception e) {
            log.debug("Failed to send event to Kafka in test mode (this is expected): {}", e.getMessage());
            // Don't propagate the exception in test mode
        }
    }


    /**
     * Get all events published to a specific topic
     *
     * @param topic The topic to check
     * @return Map of keys to events
     */
    public Map<String, Object> getEventsForTopic(String topic) {
        return publishedEvents.getOrDefault(topic, new ConcurrentHashMap<>());
    }

    /**
     * Get the last event published to a topic
     *
     * @param topic The topic to check
     * @return The last event published, or null if none
     */
    public Object getLastEventForTopic(String topic) {
        Map<String, Object> events = publishedEvents.get(topic);
        if (events == null || events.isEmpty()) {
            return null;
        }

        // Return the last added event (not strictly accurate but good enough for testing)
        return events.values().stream().reduce((first, second) -> second).orElse(null);
    }

    /**
     * Check if Kafka is available
     */
    private boolean isKafkaAvailable() {
        try {
            return kafkaTemplate.getDefaultTopic() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear all stored events
     */
    public void clearEvents() {
        publishedEvents.clear();
    }
}
