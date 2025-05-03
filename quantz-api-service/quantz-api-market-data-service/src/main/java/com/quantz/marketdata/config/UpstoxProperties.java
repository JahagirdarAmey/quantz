package com.quantz.marketdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "upstox")
public class UpstoxProperties {
    
    private String baseUrl = "https://api.upstox.com/v2";
    private Auth auth = new Auth();
    private Api api = new Api();
    private WebSocket webSocket = new WebSocket();
    private Instruments instruments = new Instruments();
    
    @Data
    public static class Auth {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationEndpoint = "https://api.upstox.com/v2/login/authorization/dialog";
        private String tokenEndpoint = "https://api.upstox.com/v2/login/authorization/token";
        private Long tokenExpiryBufferSeconds = 300L; // 5 minutes buffer before token expiry
    }
    
    @Data
    public static class Api {
        private int connectTimeoutMillis = 5000;
        private int readTimeoutMillis = 30000;
        private int maxRetries = 3;
        private long retryDelayMillis = 1000;
        private int maxRequestsPerSecond = 10; // Default API rate limit
    }
    
    @Data
    public static class WebSocket {
        private String marketDataEndpoint = "wss://api.upstox.com/v2/feed/market-data-feed";
        private int reconnectDelayMillis = 5000;
        private int pingIntervalMillis = 30000;
        private int maxReconnectAttempts = 10;
    }
    
    @Data
    public static class Instruments {
        private String bodInstrumentsUrl = "https://assets.upstox.com/market-data/instruments/exchange/NSE.json";
        private String nfoInstrumentsUrl = "https://assets.upstox.com/market-data/instruments/exchange/NSE_FO.json";
        private String bseInstrumentsUrl = "https://assets.upstox.com/market-data/instruments/exchange/BSE.json";
        private String bfoInstrumentsUrl = "https://assets.upstox.com/market-data/instruments/exchange/BSE_FO.json";
        private String mcxInstrumentsUrl = "https://assets.upstox.com/market-data/instruments/exchange/MCX.json";
        private long cacheExpiryMinutes = 720; // 12 hours
    }
}