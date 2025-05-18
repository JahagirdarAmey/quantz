package com.quantz.marketdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Instrument model from Upstox
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpstoxInstrument {

    private String segment;
    private String name;
    private String exchange;
    private String isin;

    @JsonProperty("instrument_type")
    private String instrumentType;

    @JsonProperty("instrument_key")
    private String instrumentKey;

    @JsonProperty("lot_size")
    private Integer lotSize;

    @JsonProperty("freeze_quantity")
    private Double freezeQuantity;

    @JsonProperty("exchange_token")
    private String exchangeToken;

    @JsonProperty("tick_size")
    private Double tickSize;

    @JsonProperty("trading_symbol")
    private String tradingSymbol;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("security_type")
    private String securityType;

    // Additional fields for derivatives
    private String expiry;
    private Double strike;

    @JsonProperty("option_type")
    private String optionType;
}

