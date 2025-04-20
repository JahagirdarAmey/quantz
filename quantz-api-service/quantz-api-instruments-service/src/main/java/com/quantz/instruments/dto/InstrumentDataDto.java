package com.quantz.instruments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data transfer object representing historical data for a specific instrument.
 * This includes all the price points within the requested time range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentDataDto {


    private InstrumentDto instrument;
    private String interval;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    private List<PricePointDto> dataPoints;
    private DataMetadataDto metadata;

    public int getCount() {
        return dataPoints != null ? dataPoints.size() : 0;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataMetadataDto {

        private String source;
        private boolean adjusted;
        private String quality;
        private Double estimatedPercentage;
        private Long lastUpdated;
    }
}