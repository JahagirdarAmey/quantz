package com.quantz.instruments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object representing a single price data point for a financial instrument.
 * Contains OHLCV (Open, High, Low, Close, Volume) data for a specific timestamp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePointDto {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Double open;

    private Double high;

    private Double low;

    private Double close;

    private Double volume;

    private Double adjustedClose;

    private Boolean estimated;

    private ExtraDataDto extraData;
    
    /**
     * Nested class for representing additional data that varies by instrument type
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraDataDto {
        private Double openInterest;

        private Double impliedVolatility;

        private Double bid;

        private Double ask;

        private Double spread;
    }
}