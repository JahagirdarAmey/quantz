package com.quantz.instruments.repository;

import com.quantz.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for Instrument entities with support for JPA Specifications
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String>, JpaSpecificationExecutor<Instrument> {
    // Custom query methods can be added here if needed
}