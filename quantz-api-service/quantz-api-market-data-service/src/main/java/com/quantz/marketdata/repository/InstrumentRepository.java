package com.quantz.marketdata.repository;

import com.quantz.marketdata.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String> {

    List<Instrument> findByExchange(String exchange);

    List<Instrument> findBySegment(String segment);

    List<Instrument> findByInstrumentType(String instrumentType);

    @Query("SELECT i FROM Instrument i WHERE i.segment = :segment AND i.instrumentType = :instrumentType")
    List<Instrument> findBySegmentAndInstrumentType(
            @Param("segment") String segment,
            @Param("instrumentType") String instrumentType);

    Optional<Instrument> findByTradingSymbolAndExchange(String tradingSymbol, String exchange);

    @Query("SELECT i FROM Instrument i WHERE i.name LIKE %:searchTerm% OR i.tradingSymbol LIKE %:searchTerm%")
    List<Instrument> searchByNameOrSymbol(@Param("searchTerm") String searchTerm);
}

