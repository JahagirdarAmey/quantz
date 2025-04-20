package com.quantz.instruments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Model class representing a single price data point for a financial instrument.
 * Internal model for instrument service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePoint {
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime timestamp;

    private Double open;

    private Double high;

    private Double low;

    private Double close;

    private Double volume;

    private Double adjustedClose;
}