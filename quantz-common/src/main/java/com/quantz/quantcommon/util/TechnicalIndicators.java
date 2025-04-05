package com.quantz.quantcommon.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.quantz.quantcommon.model.MarketData;

/**
 * Utility class for technical analysis calculations
 */
public class TechnicalIndicators {
    
    private TechnicalIndicators() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Calculates the Simple Moving Average (SMA) for a list of market data
     * 
     * @param marketDataList List of market data
     * @param period The period for the SMA
     * @return The SMA value
     */
    public static BigDecimal calculateSMA(List<MarketData> marketDataList, int period) {
        if (marketDataList == null || marketDataList.size() < period) {
            return null;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        
        for (int i = marketDataList.size() - period; i < marketDataList.size(); i++) {
            BigDecimal close = marketDataList.get(i).getClose();
            if (close == null) {
                return null;
            }
            sum = sum.add(close);
        }
        
        return sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculates the Exponential Moving Average (EMA) for a list of market data
     * 
     * @param marketDataList List of market data
     * @param period The period for the EMA
     * @return The EMA value
     */
    public static BigDecimal calculateEMA(List<MarketData> marketDataList, int period) {
        if (marketDataList == null || marketDataList.size() < period) {
            return null;
        }
        
        // First EMA is the SMA
        BigDecimal ema = calculateSMA(marketDataList.subList(0, period), period);
        
        // Multiplier: 2 / (period + 1)
        BigDecimal multiplier = BigDecimal.valueOf(2)
                .divide(BigDecimal.valueOf(period + 1), 8, RoundingMode.HALF_UP);
        
        // Calculate EMA: (Close - EMA(previous)) * multiplier + EMA(previous)
        for (int i = period; i < marketDataList.size(); i++) {
            BigDecimal close = marketDataList.get(i).getClose();
            ema = close.subtract(ema).multiply(multiplier).add(ema);
        }
        
        return ema;
    }
    
    /**
     * Calculates the Relative Strength Index (RSI) for a list of market data
     * 
     * @param marketDataList List of market data
     * @param period The period for the RSI
     * @return The RSI value
     */
    public static BigDecimal calculateRSI(List<MarketData> marketDataList, int period) {
        if (marketDataList == null || marketDataList.size() <= period) {
            return null;
        }
        
        BigDecimal[] gains = new BigDecimal[marketDataList.size() - 1];
        BigDecimal[] losses = new BigDecimal[marketDataList.size() - 1];
        
        // Calculate price changes, gains and losses
        for (int i = 1; i < marketDataList.size(); i++) {
            BigDecimal change = marketDataList.get(i).getClose()
                    .subtract(marketDataList.get(i - 1).getClose());
            
            if (change.compareTo(BigDecimal.ZERO) >= 0) {
                gains[i - 1] = change;
                losses[i - 1] = BigDecimal.ZERO;
            } else {
                gains[i - 1] = BigDecimal.ZERO;
                losses[i - 1] = change.abs();
            }
        }
        
        // Calculate first average gain and loss
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        for (int i = 0; i < period; i++) {
            avgGain = avgGain.add(gains[i]);
            avgLoss = avgLoss.add(losses[i]);
        }
        
        avgGain = avgGain.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        
        // Calculate subsequent values
        for (int i = period; i < gains.length; i++) {
            avgGain = (avgGain.multiply(BigDecimal.valueOf(period - 1))
                    .add(gains[i]))
                    .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
            
            avgLoss = (avgLoss.multiply(BigDecimal.valueOf(period - 1))
                    .add(losses[i]))
                    .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        }
        
        // Calculate RS and RSI
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 8, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                .divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
        
        return rsi;
    }
    
    /**
     * Calculates the Bollinger Bands for a list of market data
     * 
     * @param marketDataList List of market data
     * @param period The period for the SMA calculation
     * @param deviations The number of standard deviations for the bands
     * @return An array containing [middle band, upper band, lower band]
     */
    public static BigDecimal[] calculateBollingerBands(List<MarketData> marketDataList, int period, double deviations) {
        if (marketDataList == null || marketDataList.size() < period) {
            return null;
        }
        
        // Calculate SMA (middle band)
        BigDecimal sma = calculateSMA(marketDataList, period);
        
        // Calculate standard deviation
        BigDecimal sumSquaredDeviations = BigDecimal.ZERO;
        
        for (int i = marketDataList.size() - period; i < marketDataList.size(); i++) {
            BigDecimal deviation = marketDataList.get(i).getClose().subtract(sma);
            sumSquaredDeviations = sumSquaredDeviations.add(deviation.pow(2));
        }
        
        BigDecimal variance = sumSquaredDeviations.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        // Calculate bands
        BigDecimal upperBand = sma.add(standardDeviation.multiply(BigDecimal.valueOf(deviations)));
        BigDecimal lowerBand = sma.subtract(standardDeviation.multiply(BigDecimal.valueOf(deviations)));
        
        return new BigDecimal[] { sma, upperBand, lowerBand };
    }
    
    /**
     * Calculates the MACD (Moving Average Convergence Divergence) for a list of market data
     * 
     * @param marketDataList List of market data
     * @param fastPeriod The period for the fast EMA
     * @param slowPeriod The period for the slow EMA
     * @param signalPeriod The period for the signal line
     * @return An array containing [MACD line, signal line, histogram]
     */
    public static BigDecimal[] calculateMACD(List<MarketData> marketDataList, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (marketDataList == null || marketDataList.size() < Math.max(fastPeriod, slowPeriod) + signalPeriod) {
            return null;
        }
        
        // Calculate fast and slow EMAs
        BigDecimal fastEMA = calculateEMA(marketDataList, fastPeriod);
        BigDecimal slowEMA = calculateEMA(marketDataList, slowPeriod);
        
        // Calculate MACD line
        BigDecimal macdLine = fastEMA.subtract(slowEMA);
        
        // Calculate signal line (9-day EMA of MACD line)
        // Note: This is a simplified approach; normally we'd calculate the EMA of the MACD line
        BigDecimal signalLine = calculateEMA(marketDataList.subList(marketDataList.size() - signalPeriod, marketDataList.size()), signalPeriod);
        
        // Calculate histogram
        BigDecimal histogram = macdLine.subtract(signalLine);
        
        return new BigDecimal[] { macdLine, signalLine, histogram };
    }
}