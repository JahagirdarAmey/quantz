package com.quantz.quantstrategy.repository;

import com.quantz.quantstrategy.model.Strategy;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StrategyRepository extends ReactiveCrudRepository<Strategy, String> {
    // Custom query methods can be added here if needed
}
