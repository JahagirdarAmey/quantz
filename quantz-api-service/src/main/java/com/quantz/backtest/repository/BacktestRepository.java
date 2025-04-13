package com.quantz.backtest.repository;

import com.quantz.backtest.entity.BacktestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacktestRepository extends JpaRepository<BacktestEntity, String>, BacktestRepositoryCustom {
    List<BacktestEntity> findByStatus(String status);
    // Other query methods as needed
}