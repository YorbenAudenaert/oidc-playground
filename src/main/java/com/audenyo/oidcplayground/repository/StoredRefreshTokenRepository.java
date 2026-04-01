package com.audenyo.oidcplayground.repository;

import com.audenyo.oidcplayground.model.entity.StoredRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoredRefreshTokenRepository extends JpaRepository<StoredRefreshToken, Long> {

    Optional<StoredRefreshToken> findBySubject(String subject);
}
