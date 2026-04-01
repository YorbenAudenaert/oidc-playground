package com.audenyo.oidcplayground.service;

import com.audenyo.oidcplayground.model.entity.StoredRefreshToken;
import com.audenyo.oidcplayground.repository.StoredRefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RefreshTokenStorageService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenStorageService.class);

    private final StoredRefreshTokenRepository repository;

    public RefreshTokenStorageService(StoredRefreshTokenRepository repository) {
        this.repository = repository;
    }

    public void storeOrUpdate(String subject, String userId, String refreshToken) {
        StoredRefreshToken stored = repository.findBySubject(subject)
                .orElseGet(StoredRefreshToken::new);

        stored.setSubject(subject);
        stored.setUserId(userId);
        stored.setRefreshToken(refreshToken);
        stored.setStoredAt(Instant.now());

        repository.save(stored);
        log.debug("Stored refresh token for subject '{}'", subject);
    }

    @Transactional(readOnly = true)
    public Optional<StoredRefreshToken> findBySubject(String subject) {
        return repository.findBySubject(subject);
    }

    @Transactional(readOnly = true)
    public List<StoredRefreshToken> findAll() {
        return repository.findAll();
    }

    public void updateAfterRefresh(String subject, Instant lastUsedAt, String status) {
        repository.findBySubject(subject).ifPresent(stored -> {
            stored.setLastUsedAt(lastUsedAt);
            stored.setLastRefreshStatus(status);
            repository.save(stored);
        });
    }
}
