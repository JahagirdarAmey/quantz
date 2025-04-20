package com.quantz.instruments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data transfer object representing a financial instrument.
 * Contains all the metadata about an instrument that can be used for backtesting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class InstrumentDto {


    private String id;
    private String symbol;
    private String name;
    private String type;
    private String exchange;
    private String currency;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate listedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate delistedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;

    private TypeSpecificDetailsDto details;
    private String[] availableIntervals;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataStartDate;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataEndDate;

    /**
     * Nested class for type-specific details that vary by instrument type
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TypeSpecificDetailsDto {
        // Stock specific

        private String sector;
        private String industry;
        private Double marketCap;
        private Long outstandingShares;

        // ETF specific

        private String assetClass;
        private Double expenseRatio;
        private Double aum;

        // Futures specific

        private Double contractSize;
        private String contractUnit;
        private Double tickSize;
        private Double tickValue;

        // Options specific

        private String optionType;
        private Double strikePrice;


        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expirationDate;


        private String underlyingId;

        // Crypto specific

        private Double maxSupply;
        private Double circulatingSupply;

        // Additional custom properties
        private Map<String, Object> additionalProperties;
    }
}