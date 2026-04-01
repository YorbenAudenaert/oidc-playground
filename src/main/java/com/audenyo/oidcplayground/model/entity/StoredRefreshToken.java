package com.audenyo.oidcplayground.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "stored_refresh_tokens")
public class StoredRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String subject;

    @Column(nullable = false, length = 4096)
    private String refreshToken;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant storedAt;

    @Column
    private Instant lastUsedAt;

    @Column
    private String lastRefreshStatus;

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String email) {
        this.userId = email;
    }

    public Instant getStoredAt() {
        return storedAt;
    }

    public void setStoredAt(Instant storedAt) {
        this.storedAt = storedAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getLastRefreshStatus() {
        return lastRefreshStatus;
    }

    public void setLastRefreshStatus(String lastRefreshStatus) {
        this.lastRefreshStatus = lastRefreshStatus;
    }
}
