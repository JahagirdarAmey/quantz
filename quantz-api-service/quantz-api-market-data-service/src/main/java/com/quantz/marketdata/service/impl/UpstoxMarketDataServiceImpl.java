package com.quantz.marketdata.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantz.marketdata.config.UpstoxProperties;
import com.quantz.marketdata.model.CandleData;
import com.quantz.marketdata.model.CandleDataResponse;
import com.quantz.marketdata.model.LtpQuoteData;
import com.quantz.marketdata.model.UpstoxResponse;
import com.quantz.marketdata.service.UpstoxAuthService;
import com.quantz.marketdata.service.UpstoxHttpClient;
import com.quantz.marketdata.service.UpstoxMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpstoxMarketDataServiceImpl implements UpstoxMarketDataService {

    private final UpstoxHttpClient upstoxHttpClient;
    private final UpstoxAuthService authService;
    private final UpstoxProperties upstoxProperties;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final String HISTORICAL_CANDLE_PATH = "/historical-candle/{instrumentKey}/{interval}/{toDate}/{fromDate}";
    private static final String INTRADAY_CANDLE_PATH = "/historical-candle/intraday/{instrumentKey}/{interval}";
    private static final String LTP_QUOTES_PATH = "/market-quote/ltp";

    @Override
    public List<CandleData> fetchHistoricalCandleData(
            String instrumentKey, String interval, LocalDate fromDate, LocalDate toDate) {

        try {
            String path = HISTORICAL_CANDLE_PATH
                    .replace("{instrumentKey}", instrumentKey)
                    .replace("{interval}", interval)
                    .replace("{toDate}", toDate.format(DATE_FORMATTER))
                    .replace("{fromDate}", fromDate.format(DATE_FORMATTER));

            ResponseEntity<UpstoxResponse> response =
                    upstoxHttpClient.get(path, UpstoxResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UpstoxResponse<CandleDataResponse> upstoxResponse = response.getBody();

                if ("success".equals(upstoxResponse.getStatus()) && upstoxResponse.getData() != null) {
                    CandleDataResponse candleDataResponse = upstoxResponse.getData();

                    return processCandleData(candleDataResponse.getCandles(), instrumentKey, interval);
                }
            }

            log.warn("Failed to fetch historical candle data for instrument: {}", instrumentKey);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching historical candle data for {}: {}", instrumentKey, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<CandleData> fetchIntradayCandleData(String instrumentKey, String interval) {
        try {
            String path = INTRADAY_CANDLE_PATH
                    .replace("{instrumentKey}", instrumentKey)
                    .replace("{interval}", interval);

            ResponseEntity<UpstoxResponse> response =
                    upstoxHttpClient.get(path, UpstoxResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UpstoxResponse<CandleDataResponse> upstoxResponse = response.getBody();

                if ("success".equals(upstoxResponse.getStatus()) && upstoxResponse.getData() != null) {
                    CandleDataResponse candleDataResponse = upstoxResponse.getData();

                    return processCandleData(candleDataResponse.getCandles(), instrumentKey, interval);
                }
            }

            log.warn("Failed to fetch intraday candle data for instrument: {}", instrumentKey);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error fetching intraday candle data for {}: {}", instrumentKey, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, LtpQuoteData> getLtpQuotes(List<String> instrumentKeys) {
        if (instrumentKeys == null || instrumentKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // Upstox API requires comma-separated instrument keys
            String instrumentKeysStr = String.join(",", instrumentKeys);

            // Build the request
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("instrument_key", instrumentKeysStr);

            ResponseEntity<UpstoxResponse> response =
                    upstoxHttpClient.get(LTP_QUOTES_PATH, queryParams, UpstoxResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UpstoxResponse<Map<String, LtpQuoteData>> upstoxResponse = response.getBody();

                if ("success".equals(upstoxResponse.getStatus()) && upstoxResponse.getData() != null) {
                    return upstoxResponse.getData();
                }
            }

            log.warn("Failed to fetch LTP quotes for instruments");
            return Collections.emptyMap();

        } catch (Exception e) {
            log.error("Error fetching LTP quotes: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void connectToMarketDataStream(List<String> instrumentKeys) {
        // Implementation for WebSocket connection would go here
        // This is a placeholder for the actual WebSocket implementation
        log.info("Connecting to market data stream for {} instruments", instrumentKeys.size());

        // The real implementation would:
        // 1. Get an authorization token for WebSocket connection
        // 2. Connect to the WebSocket endpoint
        // 3. Subscribe to the instruments
        // 4. Process incoming messages

        // Note: WebSocket implementation is complex and would require additional components
        // like a WebSocketClient, message handlers, etc.
    }

    /**
     * Process raw candle data from Upstox API response
     */
    private List<CandleData> processCandleData(List<Object[]> rawCandles, String instrumentKey, String interval) {
        if (rawCandles == null || rawCandles.isEmpty()) {
            return Collections.emptyList();
        }

        List<CandleData> processedCandles = new ArrayList<>();

        for (Object[] candle : rawCandles) {
            if (candle.length < 6) {
                log.warn("Invalid candle data format for {}", instrumentKey);
                continue;
            }

            try {
                // Format: [timestamp, open, high, low, close, volume]
                String timestampStr = (String) candle[0];
                Double open = parseDouble(candle[1]);
                Double high = parseDouble(candle[2]);
                Double low = parseDouble(candle[3]);
                Double close = parseDouble(candle[4]);
                Long volume = parseLong(candle[5]);

                // Parse timestamp
                LocalDateTime timestamp = OffsetDateTime.parse(timestampStr).toLocalDateTime();

                CandleData candleData = CandleData.builder()
                        .instrumentKey(instrumentKey)
                        .interval(interval)
                        .timestamp(timestamp)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .volume(volume)
                        .build();

                processedCandles.add(candleData);

            } catch (Exception e) {
                log.error("Error processing candle data for {}: {}", instrumentKey, e.getMessage());
            }
        }

        return processedCandles;
    }

    /**
     * Safely parse Double from Object
     */
    private Double parseDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Safely parse Long from Object
     */
    private Long parseLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }
}