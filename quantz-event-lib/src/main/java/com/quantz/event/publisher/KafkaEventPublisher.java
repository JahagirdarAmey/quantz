package com.quantz.event.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!test")
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, Object event) {
        String key = UUID.randomUUID().toString();

        // Add correlation ID if available
        if (MDC.get("correlationId") != null) {
            key = MDC.get("correlationId");
        }

        // Set headers for tracing
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);
        record.headers().add("source", "quantz-service".getBytes());
        record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());

        kafkaTemplate.send(record);
    }
}