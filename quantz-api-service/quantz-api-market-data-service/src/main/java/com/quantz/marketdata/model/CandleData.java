package com.quantz.marketdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Processed candle data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleData {
    private String instrumentKey;
    private String interval;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}
