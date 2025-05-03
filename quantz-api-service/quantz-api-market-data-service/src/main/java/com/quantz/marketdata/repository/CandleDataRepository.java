package com.quantz.marketdata.repository;

import com.quantz.marketdata.entity.CandleData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandleDataRepository extends JpaRepository<CandleData, Long> {
    
    List<CandleData> findByInstrumentKeyOrderByTimestampAsc(String instrumentKey);
    
    List<CandleData> findByInstrumentKeyAndIntervalOrderByTimestampAsc(String instrumentKey, String interval);
    
    List<CandleData> findByInstrumentKeyAndIntervalAndTimestampBetweenOrderByTimestampAsc(
            String instrumentKey, String interval, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT c FROM CandleData c WHERE c.instrumentKey = :instrumentKey AND c.interval = :interval " +
           "AND c.timestamp = (SELECT MAX(c2.timestamp) FROM CandleData c2 WHERE c2.instrumentKey = :instrumentKey AND c2.interval = :interval)")
    Optional<CandleData> findLatestCandleByInstrumentKeyAndInterval(
            @Param("instrumentKey") String instrumentKey, 
            @Param("interval") String interval);
    
    @Query("SELECT COUNT(c) FROM CandleData c WHERE c.instrumentKey = :instrumentKey AND c.interval = :interval")
    Long countByInstrumentKeyAndInterval(
            @Param("instrumentKey") String instrumentKey, 
            @Param("interval") String interval);
}