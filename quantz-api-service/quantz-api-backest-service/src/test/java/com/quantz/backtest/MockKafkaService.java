package com.quantz.backtest;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Profile("test")
public class MockKafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, Object> lastSentMessages = new HashMap<>();

    public MockKafkaService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send a message to a Kafka topic.
     * In test mode, it stores the message locally and returns a completed future.
     *
     * @param topic the topic to send the message to
     * @param key the message key
     * @param message the message payload
     * @return a CompletableFuture that completes when the send operation is done
     */

    public CompletableFuture<Void> sendMessage(String topic, String key, Object message) {
        // Store the message for verification in tests
        lastSentMessages.put(topic, message);

        try {
            // In real environment, actually send to Kafka
            if (isRealEnvironment()) {
                return kafkaTemplate.send(topic, key, message)
                        .thenRun(() -> {})
                        .toCompletableFuture();
            } else {
                // In test environment, just return a completed future
                return CompletableFuture.completedFuture(null);
            }
        } catch (Exception e) {
            // In test environment, we don't want failures if Kafka is not available
            if (isRealEnvironment()) {
                throw e;
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Get the last message sent to a topic
     *
     * @param topic the topic name
     * @return the last message sent to the topic
     */
    public Object getLastMessageSentToTopic(String topic) {
        return lastSentMessages.get(topic);
    }

    /**
     * Check if we're running in a real environment or test environment
     *
     * @return true if real environment (not test), false otherwise
     */
    private boolean isRealEnvironment() {
        try {
            // Try to connect to Kafka
            return kafkaTemplate.getProducerFactory().createProducer().metrics() != null;
        } catch (Exception e) {
            // If we can't connect, we're in a test environment
            return false;
        }
    }

}
