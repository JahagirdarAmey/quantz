package com.quantz.marketdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing instrument information
 */
@Entity
@Table(name = "instruments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {

    @Id
    @Column(name = "instrument_key", length = 100)
    private String instrumentKey;

    @Column(nullable = false, length = 50)
    private String exchange;

    @Column(nullable = false, length = 50)
    private String segment;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String isin;

    @Column(name = "instrument_type", nullable = false, length = 50)
    private String instrumentType;

    @Column(name = "trading_symbol", nullable = false, length = 50)
    private String tradingSymbol;

    @Column(name = "exchange_token", length = 50)
    private String exchangeToken;

    @Column(name = "lot_size")
    private Integer lotSize;

    @Column(name = "tick_size")
    private Double tickSize;

    // For derivatives
    @Column(length = 20)
    private String expiry;

    private Double strike;

    @Column(name = "option_type", length = 10)
    private String optionType;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
