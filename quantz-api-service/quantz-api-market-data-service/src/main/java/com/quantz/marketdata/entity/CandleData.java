package com.quantz.marketdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing OHLC candle data
 */
@Entity
@Table(name = "candle_data",
        indexes = {
                @Index(name = "idx_candle_instr_date", columnList = "instrument_key, timestamp"),
                @Index(name = "idx_candle_interval", columnList = "interval")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_key", nullable = false)
    private String instrumentKey;

    @Column(name = "interval", nullable = false, length = 10)
    private String interval;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Double open;

    @Column(nullable = false)
    private Double high;

    @Column(nullable = false)
    private Double low;

    @Column(nullable = false)
    private Double close;

    @Column(nullable = false)
    private Long volume;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}