package com.quantz.quantstrategy.service.impl;


import com.quantz.quantcommon.exception.StrategyException;
import com.quantz.quantstrategy.model.Strategy;
import com.quantz.quantstrategy.repository.StrategyRepository;
import com.quantz.quantstrategy.service.StrategyService;
import com.quantz.quantstrategy.strategy.StrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyServiceImpl implements StrategyService {

    private final StrategyRepository strategyRepository;
    private final StrategyFactory strategyFactory;

    @Override
    public Mono<Strategy> createStrategy(Strategy strategy) {
        // Generate an ID if not provided
        if (strategy.getId() == null || strategy.getId().isEmpty()) {
            strategy.setId(UUID.randomUUID().toString());
        }
        
        // Validate the strategy type
        if (!strategyFactory.isSupportedType(strategy.getType())) {
            return Mono.error(new StrategyException("Unsupported strategy type: " + strategy.getType()));
        }
        
        return strategyRepository.save(strategy)
                .doOnSuccess(s -> log.info("Strategy created: {}", s.getId()))
                .doOnError(e -> log.error("Error creating strategy", e));
    }

    @Override
    public Mono<Strategy> getStrategyById(String id) {
        return strategyRepository.findById(id)
                .switchIfEmpty(Mono.error(new StrategyException("Strategy not found with ID: " + id)));
    }

    @Override
    public Flux<Strategy> getAllStrategies() {
        return strategyRepository.findAll();
    }

    @Override
    public Mono<Strategy> updateStrategy(String id, Strategy strategy) {
        return strategyRepository.findById(id)
                .switchIfEmpty(Mono.error(new StrategyException("Strategy not found with ID: " + id)))
                .flatMap(existingStrategy -> {
                    // Validate the strategy type
                    if (!strategyFactory.isSupportedType(strategy.getType())) {
                        return Mono.error(new StrategyException("Unsupported strategy type: " + strategy.getType()));
                    }
                    
                    // Update fields
                    strategy.setId(id); // Ensure ID is preserved
                    return strategyRepository.save(strategy);
                })
                .doOnSuccess(s -> log.info("Strategy updated: {}", s.getId()))
                .doOnError(e -> log.error("Error updating strategy", e));
    }

    @Override
    public Mono<Void> deleteStrategy(String id) {
        return strategyRepository.findById(id)
                .switchIfEmpty(Mono.error(new StrategyException("Strategy not found with ID: " + id)))
                .flatMap(strategy -> strategyRepository.delete(strategy))
                .doOnSuccess(v -> log.info("Strategy deleted: {}", id))
                .doOnError(e -> log.error("Error deleting strategy", e));
    }

    @Override
    public Flux<Signal> generateSignals(String strategyId, List<MarketData> marketDataList) {
        return strategyRepository.findById(strategyId)
                .switchIfEmpty(Mono.error(new StrategyException("Strategy not found with ID: " + strategyId)))
                .flatMapMany(strategy -> {
                    com.quantz.quantstrategy.strategy.Strategy strategyImpl = 
                            strategyFactory.createStrategy(strategy.getType(), strategy.getParameters());
                    
                    List<Signal> signals = strategyImpl.generateSignals(marketDataList);
                    return Flux.fromIterable(signals);
                })
                .doOnError(e -> log.error("Error generating signals for strategy: {}", strategyId, e));
    }

    @Override
    public Mono<Strategy> startStrategy(String id) {
        return strategyRepository.findById(id)
                .switchIfEmpty(Mono.error(new StrategyException("Strategy not found with ID: " + id)))
                .flatMap(strategy -> {
                    strategy.setActive(true);
                    return strategyRepository.save(strategy);
                })
                .doOnSuccess(s -> log.info("Strategy started: {}", s.getId()))
                .doOnError(e -> log.error("Error starting strategy", e));
    }

    @Override
    public Mono<Strategy> stopStrategy(String id) {
        return strategyRepository.findById(id)
                .switchIfEmpty(Mono.error(new StrategyException("Strategy not found with ID: " + id)))
                .flatMap(strategy -> {
                    strategy.setActive(false);
                    return strategyRepository.save(strategy);
                })
                .doOnSuccess(s -> log.info("Strategy stopped: {}", s.getId()))
                .doOnError(e -> log.error("Error stopping strategy", e));
    }
}