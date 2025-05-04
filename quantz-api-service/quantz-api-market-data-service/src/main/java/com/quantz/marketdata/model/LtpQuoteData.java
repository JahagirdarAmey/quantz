package com.quantz.marketdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model for Last Traded Price (LTP) quote data from Upstox API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LtpQuoteData {
    /**
     * Last traded price of the instrument
     */
    @JsonProperty("last_price")
    private BigDecimal lastPrice;

    /**
     * Instrument token in format "EXCHANGE|ISIN" (e.g., "NSE_EQ|INE848E01016")
     */
    @JsonProperty("instrument_token")
    private String instrumentToken;

    /**
     * Last traded time (if available from API)
     */
    @JsonProperty("last_trade_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime lastTradeTime;

    /**
     * Previous day close price (if available from API)
     */
    @JsonProperty("close_price")
    private BigDecimal closePrice;

    /**
     * Price change compared to previous close
     */
    @JsonProperty("price_change")
    private BigDecimal priceChange;

    /**
     * Percentage change compared to previous close
     */
    @JsonProperty("price_change_percent")
    private BigDecimal priceChangePercent;

    /**
     * Last traded quantity (if available from API)
     */
    @JsonProperty("last_quantity")
    private Long lastQuantity;

    /**
     * Volume traded today (if available from API)
     */
    @JsonProperty("volume")
    private Long volume;

    /**
     * Average traded price (if available from API)
     */
    @JsonProperty("average_price")
    private BigDecimal averagePrice;

    /**
     * Open interest for derivatives (if available)
     */
    @JsonProperty("open_interest")
    private Long openInterest;

    /**
     * Total buy quantity available in order book
     */
    @JsonProperty("total_buy_quantity")
    private Long totalBuyQuantity;

    /**
     * Total sell quantity available in order book
     */
    @JsonProperty("total_sell_quantity")
    private Long totalSellQuantity;

    /**
     * Convenience method to calculate whether price has increased
     *
     * @return true if price has increased from previous close
     */
    public boolean isPriceIncreased() {
        return priceChange != null && priceChange.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Convenience method to calculate absolute price change
     *
     * @return absolute price change amount
     */
    public BigDecimal getAbsolutePriceChange() {
        return priceChange != null ? priceChange.abs() : null;
    }
}