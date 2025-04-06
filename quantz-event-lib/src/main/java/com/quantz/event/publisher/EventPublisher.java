package com.quantz.event.publisher;

public interface EventPublisher {
    void publish(String topic, Object event);
}