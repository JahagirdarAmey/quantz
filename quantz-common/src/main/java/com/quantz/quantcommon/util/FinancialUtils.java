package com.quantz.quantcommon.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.quantz.quantcommon.model.Order;
import com.quantz.quantcommon.model.Position;
import com.quantz.quantcommon.model.Signal;

/**
 * Utility class for financial calculations
 */
public class FinancialUtils {
    
    private FinancialUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Calculate position size based on risk parameters
     * 
     * @param accountBalance Total account balance
     * @param riskPercentage Risk percentage per trade (e.g., 1.0 for 1%)
     * @param entryPrice Entry price
     * @param stopLossPrice Stop loss price
     * @return Position size (quantity)
     */
    public static BigDecimal calculatePositionSize(
            BigDecimal accountBalance, 
            BigDecimal riskPercentage, 
            BigDecimal entryPrice, 
            BigDecimal stopLossPrice) {
        
        // Validate inputs
        if (accountBalance == null || riskPercentage == null || 
            entryPrice == null || stopLossPrice == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }
        
        // Calculate risk amount
        BigDecimal riskAmount = accountBalance.multiply(riskPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Calculate risk per share
        BigDecimal riskPerShare = entryPrice.subtract(stopLossPrice).abs();
        
        // Validate risk per share
        if (riskPerShare.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Risk per share cannot be zero");
        }
        
        // Calculate position size
        return riskAmount.divide(riskPerShare, 0, RoundingMode.DOWN);
    }
    
    /**
     * Calculate risk to reward ratio
     * 
     * @param entryPrice Entry price
     * @param stopLossPrice Stop loss price
     * @param takeProfitPrice Take profit price
     * @return Risk to reward ratio
     */
    public static BigDecimal calculateRiskRewardRatio(
            BigDecimal entryPrice, 
            BigDecimal stopLossPrice, 
            BigDecimal takeProfitPrice) {
        
        // Validate inputs
        if (entryPrice == null || stopLossPrice == null || takeProfitPrice == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }
        
        // Calculate risk and reward
        BigDecimal risk = entryPrice.subtract(stopLossPrice).abs();
        BigDecimal reward = entryPrice.subtract(takeProfitPrice).abs();
        
        // Validate risk
        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Risk cannot be zero");
        }
        
        // Calculate risk to reward ratio
        return reward.divide(risk, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate the maximum drawdown from a list of equity values
     * 
     * @param equityValues List of equity values over time
     * @return Maximum drawdown as a percentage
     */
    public static BigDecimal calculateMaxDrawdown(List<BigDecimal> equityValues) {
        if (equityValues == null || equityValues.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = equityValues.get(0);
        
        for (BigDecimal value : equityValues) {
            if (value.compareTo(peak) > 0) {
                peak = value;
            } else {
                BigDecimal drawdown = peak.subtract(value)
                        .divide(peak, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                }
            }
        }
        
        return maxDrawdown;
    }
    
    /**
     * Calculate the Sharpe ratio for a list of returns
     * 
     * @param returns List of period returns (as decimals)
     * @param riskFreeRate Risk-free rate (as a decimal)
     * @return Sharpe ratio
     */
    public static BigDecimal calculateSharpeRatio(List<BigDecimal> returns, BigDecimal riskFreeRate) {
        if (returns == null || returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate average return
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal ret : returns) {
            sum = sum.add(ret);
        }
        BigDecimal avgReturn = sum.divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        
        // Calculate excess return
        BigDecimal excessReturn = avgReturn.subtract(riskFreeRate);
        
        // Calculate standard deviation
        BigDecimal sumSquaredDeviations = BigDecimal.ZERO;
        for (BigDecimal ret : returns) {
            BigDecimal deviation = ret.subtract(avgReturn);
            sumSquaredDeviations = sumSquaredDeviations.add(deviation.pow(2));
        }
        
        BigDecimal variance = sumSquaredDeviations.divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        // Calculate Sharpe ratio
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return excessReturn.divide(stdDev, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate the profit factor (gross profit / gross loss)
     * 
     * @param orders List of completed orders
     * @return Profit factor
     */
    public static BigDecimal calculateProfitFactor(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ONE;
        }
        
        BigDecimal grossProfit = BigDecimal.ZERO;
        BigDecimal grossLoss = BigDecimal.ZERO;
        
        for (Order order : orders) {
            // Skip orders that aren't filled
            if (order.getStatus() == null || !order.getStatus().getValue().equals("FILLED")) {
                continue;
            }
            
            // Calculate profit/loss for this order
            if (order.getQuantity() != null && order.getAvgFillPrice() != null && order.getPrice() != null) {
                BigDecimal pnl = order.getAvgFillPrice().subtract(order.getPrice())
                        .multiply(order.getQuantity());
                
                if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                    grossProfit = grossProfit.add(pnl);
                } else {
                    grossLoss = grossLoss.add(pnl.abs());
                }
            }
        }
        
        // Avoid division by zero
        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) {
            return grossProfit.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(999) : BigDecimal.ONE;
        }
        
        return grossProfit.divide(grossLoss, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate the win rate (winning trades / total trades)
     * 
     * @param orders List of completed orders
     * @return Win rate as a percentage
     */
    public static BigDecimal calculateWinRate(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        int totalTrades = 0;
        int winningTrades = 0;
        
        for (Order order : orders) {
            // Skip orders that aren't filled
            if (order.getStatus() == null || !order.getStatus().getValue().equals("FILLED")) {
                continue;
            }
            
            totalTrades++;
            
            // Calculate profit/loss for this order
            if (order.getQuantity() != null && order.getAvgFillPrice() != null && order.getPrice() != null) {
                BigDecimal pnl = order.getAvgFillPrice().subtract(order.getPrice())
                        .multiply(order.getQuantity());
                
                if (pnl.compareTo(BigDecimal.ZERO) > 0) {
                    winningTrades++;
                }
            }
        }
        
        // Avoid division by zero
        if (totalTrades == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(winningTrades)
                .divide(BigDecimal.valueOf(totalTrades), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}