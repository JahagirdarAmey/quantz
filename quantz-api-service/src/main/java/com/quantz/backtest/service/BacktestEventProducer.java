package com.quantz.backtest.service;

import com.quantz.backtest.model.BacktestRequest;
import com.quantz.backtest.mapper.BacktestMapper;
import com.quantz.event.model.BacktestCreatedEvent;
import com.quantz.event.publisher.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Component for producing backtest-related events
 */
@Component
public class BacktestEventProducer {
    private static final Logger log = LoggerFactory.getLogger(BacktestEventProducer.class);
    
    private final EventPublisher eventPublisher;
    private final BacktestMapper backtestMapper;
    
    @Value("${backtest.event.topic}")
    private String backtestCreatedTopic;
    
    public BacktestEventProducer(EventPublisher eventPublisher, BacktestMapper backtestMapper) {
        this.eventPublisher = eventPublisher;
        this.backtestMapper = backtestMapper;
    }
    
    /**
     * Create and publish a BacktestCreatedEvent
     * 
     * @param backtestRequest the original request
     * @param backtestId the ID of the created backtest
     * @param userId the ID of the user who created the backtest
     */
    public void publishBacktestCreatedEvent(BacktestRequest backtestRequest, UUID backtestId, UUID userId) {
        BacktestCreatedEvent event = backtestMapper.toEvent(backtestRequest, backtestId, userId);
        eventPublisher.publish(backtestCreatedTopic, event);
        log.info("Published backtest created event for backtest ID: {}", backtestId);
    }
}