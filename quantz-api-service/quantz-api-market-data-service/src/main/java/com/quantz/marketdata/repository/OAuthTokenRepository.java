package com.quantz.marketdata.repository;

import com.quantz.marketdata.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

    @Query("SELECT t FROM OAuthToken t ORDER BY t.createdAt DESC")
    Optional<OAuthToken> findLatestToken();
}