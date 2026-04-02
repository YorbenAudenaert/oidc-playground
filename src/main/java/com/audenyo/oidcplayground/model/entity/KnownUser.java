package com.audenyo.oidcplayground.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class KnownUser {

    @Id
    private String sub;
    private String name;
    private Instant firstSeenAt;

    @Column(length = 4096)
    private String accessToken;
    private Instant accessTokenExpiresAt;

    @Column(length = 4096)
    private String refreshToken;

    protected KnownUser() {}

    public KnownUser(String sub, String name, Instant firstSeenAt,
                     String accessToken, Instant accessTokenExpiresAt, String refreshToken) {
        this.sub = sub;
        this.name = name;
        this.firstSeenAt = firstSeenAt;
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshToken = refreshToken;
    }

    public String getSub() { return sub; }
    public String getName() { return name; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public String getAccessToken() { return accessToken; }
    public Instant getAccessTokenExpiresAt() { return accessTokenExpiresAt; }
    public String getRefreshToken() { return refreshToken; }

    public void updateTokens(String accessToken, Instant accessTokenExpiresAt, String refreshToken) {
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshToken = refreshToken;
    }
}
