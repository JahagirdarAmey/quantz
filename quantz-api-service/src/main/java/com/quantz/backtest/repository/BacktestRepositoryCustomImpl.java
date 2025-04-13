package com.quantz.backtest.repository;

import com.quantz.backtest.entity.BacktestEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of custom repository methods for Backtest queries
 */
@Repository
public class BacktestRepositoryCustomImpl implements BacktestRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<BacktestEntity> findBacktestsForUser(String userId, String status, int limit, int offset) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BacktestEntity> query = cb.createQuery(BacktestEntity.class);
        Root<BacktestEntity> backtest = query.from(BacktestEntity.class);
        
        query.select(backtest);
        query.where(buildPredicates(cb, backtest, userId, status));
        
        // Order by creation date descending (newest first)
        query.orderBy(cb.desc(backtest.get("createdAt")));
        
        TypedQuery<BacktestEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(limit);
        typedQuery.setFirstResult(offset);
        
        return typedQuery.getResultList();
    }
    
    @Override
    public int countBacktestsForUser(String userId, String status) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<BacktestEntity> backtest = query.from(BacktestEntity.class);
        
        query.select(cb.count(backtest));
        query.where(buildPredicates(cb, backtest, userId, status));
        
        return entityManager.createQuery(query).getSingleResult().intValue();
    }
    
    /**
     * Build the predicates for filtering backtests
     */
    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<BacktestEntity> backtest, String userId, String status) {
        List<Predicate> predicates = new ArrayList<>();
        
        // User ID predicate - always filter by user
        predicates.add(cb.equal(backtest.get("userId"), userId));
        
        // Status predicate - only if provided
        if (status != null && !status.isEmpty()) {
            predicates.add(cb.equal(backtest.get("status"), status.toUpperCase()));
        }
        
        return predicates.toArray(new Predicate[0]);
    }
}