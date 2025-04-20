package com.quantz.instruments.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event representing an instrument-related action or state change
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentEvent {
    
    private String eventId;
    private EventType eventType;
    private String instrumentId;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    private String version;
}