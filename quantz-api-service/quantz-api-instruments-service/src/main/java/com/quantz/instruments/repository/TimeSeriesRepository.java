package com.quantz.instruments.repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.quantz.instruments.dto.PricePointDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for accessing time-series data from Cassandra
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class TimeSeriesRepository {

    private final Session cassandraSession;
    
    /**
     * Fetch time series data for a specific instrument within a date range
     *
     * @param instrumentId ID of the instrument
     * @param startDate Start date/time for the data
     * @param endDate End date/time for the data
     * @param interval Data interval (e.g., "1d" for daily data)
     * @return List of price points
     */
    public List<PricePointDto> fetchTimeSeries(String instrumentId, LocalDateTime startDate,
                                             LocalDateTime endDate, String interval) {
        log.info("Fetching time series data for instrument {} from {} to {} with interval {}", 
                instrumentId, startDate, endDate, interval);
        
        // Prepare statement to fetch data with the correct clustering order
        PreparedStatement preparedStatement = cassandraSession.prepare(
                "SELECT timestamp, open, high, low, close, volume, adjusted_close " +
                "FROM instrument_price_data " +
                "WHERE instrument_id = ? AND interval = ? AND timestamp >= ? AND timestamp <= ? " +
                "ORDER BY timestamp ASC");
        
        BoundStatement boundStatement = preparedStatement.bind(
                instrumentId, interval, startDate, endDate);
        
        // Execute query
        List<Row> rows = cassandraSession.execute(boundStatement).all();
        
        // Map results to DTOs
        return rows.stream()
                .map(row -> {
                    // Convert from Date to LocalDateTime
                    Date timestamp = row.getTimestamp("timestamp");
                    LocalDateTime localTimestamp = LocalDateTime.ofInstant(
                            timestamp.toInstant(), ZoneId.systemDefault());

                    return PricePointDto.builder()
                            .timestamp(localTimestamp)
                        .open(row.getDouble("open"))
                        .high(row.getDouble("high"))
                        .low(row.getDouble("low"))
                        .close(row.getDouble("close"))
                        .volume(row.getDouble("volume"))
                        .adjustedClose(row.getDouble("adjusted_close"))
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Bulk insert time series data
     *
     * @param instrumentId ID of the instrument
     * @param interval Data interval
     * @param dataPoints List of price points to insert
     */
    public void bulkInsertTimeSeries(String instrumentId, String interval, List<PricePointDto> dataPoints) {
        log.info("Bulk inserting {} data points for instrument {}", dataPoints.size(), instrumentId);
        
        PreparedStatement preparedStatement = cassandraSession.prepare(
                "INSERT INTO instrument_price_data " +
                "(instrument_id, interval, timestamp, open, high, low, close, volume, adjusted_close) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        List<BoundStatement> statements = new ArrayList<>();
        
        for (PricePointDto point : dataPoints) {
            // Convert LocalDateTime to java.util.Date for Cassandra
            Date timestamp = Date.from(
                    point.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());

            BoundStatement boundStatement = preparedStatement.bind(
                    instrumentId,
                    interval,
                    timestamp,
                    point.getOpen(),
                    point.getHigh(),
                    point.getLow(),
                    point.getClose(),
                    point.getVolume(),
                    point.getAdjustedClose()
            );
            statements.add(boundStatement);
        }
        
        // Execute batch insert
        for (BoundStatement statement : statements) {
            cassandraSession.execute(statement);
        }
    }
}