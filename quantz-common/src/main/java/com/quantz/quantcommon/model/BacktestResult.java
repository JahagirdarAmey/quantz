package com.quantz.quantcommon.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a backtest result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Unique identifier for the backtest
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    /**
     * ID of the strategy that was tested
     */
    private String strategyId;
    
    /**
     * Type of the strategy
     */
    private String strategyType;
    
    /**
     * Parameters used for the backtest
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * Instruments that were tested
     */
    @Builder.Default
    private List<String> instruments = new ArrayList<>();
    
    /**
     * Start date of the backtest
     */
    private String startDate;
    
    /**
     * End date of the backtest
     */
    private String endDate;
    
    /**
     * Initial capital
     */
    private BigDecimal initialCapital;
    
    /**
     * Final capital
     */
    private BigDecimal finalCapital;
    
    /**
     * Total return percentage
     */
    private BigDecimal totalReturn;
    
    /**
     * Annualized return percentage
     */
    private BigDecimal annualizedReturn;
    
    /**
     * Sharpe ratio
     */
    private BigDecimal sharpeRatio;
    
    /**
     * Maximum drawdown percentage
     */
    private BigDecimal maxDrawdown;
    
    /**
     * Total number of trades
     */
    private int totalTrades;
    
    /**
     * Number of winning trades
     */
    private int winningTrades;
    
    /**
     * Number of losing trades
     */
    private int losingTrades;
    
    /**
     * Win rate percentage
     */
    private BigDecimal winRate;
    
    /**
     * Profit factor (gross profit / gross loss)
     */
    private BigDecimal profitFactor;
    
    /**
     * Average profit per winning trade
     */
    private BigDecimal averageProfit;
    
    /**
     * Average loss per losing trade
     */
    private BigDecimal averageLoss;
    
    /**
     * List of equity values over time
     */
    @Builder.Default
    private List<BigDecimal> equityCurve = new ArrayList<>();
    
    /**
     * List of trades executed during the backtest
     */
    @Builder.Default
    private List<Trade> trades = new ArrayList<>();
    
    /**
     * Creation timestamp
     */
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    /**
     * Whether the backtest included fees
     */
    private boolean includeFees;
    
    /**
     * Time frame used for the backtest
     */
    private String timeframe;
    
    /**
     * Additional metadata
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Represents a trade executed during a backtest
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trade implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * Unique identifier for the trade
         */
        @Builder.Default
        private String id = UUID.randomUUID().toString();
        
        /**
         * Symbol traded
         */
        private String symbol;
        
        /**
         * Entry timestamp
         */
        private Instant entryTime;
        
        /**
         * Exit timestamp
         */
        private Instant exitTime;
        
        /**
         * Entry price
         */
        private BigDecimal entryPrice;
        
        /**
         * Exit price
         */
        private BigDecimal exitPrice;
        
        /**
         * Quantity traded
         */
        private BigDecimal quantity;
        
        /**
         * Side of the trade (BUY or SELL)
         */
        private OrderSide side;
        
        /**
         * Profit or loss amount
         */
        private BigDecimal pnl;
        
        /**
         * Profit or loss percentage
         */
        private BigDecimal pnlPercentage;
        
        /**
         * Whether the trade was a winner
         */
        private boolean winner;
        
        /**
         * Fees paid
         */
        private BigDecimal fees;
        
        /**
         * Reason for entry
         */
        private String entryReason;
        
        /**
         * Reason for exit
         */
        private String exitReason;
    }
}