package com.audenyo.oidcplayground.service;

import com.audenyo.oidcplayground.model.entity.StoredRefreshToken;
import com.audenyo.oidcplayground.repository.OidcClient;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SilentTokenRefreshService {

    private static final Logger log = LoggerFactory.getLogger(SilentTokenRefreshService.class);

    private final RefreshTokenStorageService storageService;
    private final OidcClient oidcClient;

    public SilentTokenRefreshService(
            RefreshTokenStorageService storageService,
            OidcClient oidcClient
    ) {
        this.oidcClient = oidcClient;
        this.storageService = storageService;
    }

    public void refreshAll() {
        List<StoredRefreshToken> tokens = storageService.findAll();
        log.info("Token refresh job triggered — processing {} user(s)", tokens.size());

        for (StoredRefreshToken stored : tokens) {
            refresh(stored).ifPresent(accessToken -> {
                try {
                    JWTClaimsSet claims = JWTParser.parse(accessToken).getJWTClaimsSet();
                    log.info("JWT claims for user '{}': {}", stored.getUserId(), claims.getClaims());
                } catch (Exception e) {
                    log.warn("Could not parse access token as JWT for user '{}': {}", stored.getUserId(), e.getMessage());
                }
            });
        }
    }

    private Optional<String> refresh(StoredRefreshToken token) {
        log.debug("Refreshing token for subject '{}'", token.getSubject());


        Map<String, Object> tokenResponse = oidcClient.refresh(token);
        // Handle refresh token rotation
        if (tokenResponse.containsKey("refresh_token")) {
            String newRefreshToken = (String) tokenResponse.get("refresh_token");
            storageService.storeOrUpdate(token.getSubject(), token.getUserId(), newRefreshToken);
            log.debug("Refresh token rotated for subject '{}'", token.getSubject());
        }

        storageService.updateAfterRefresh(token.getSubject(), Instant.now(), "OK");
        log.debug("Token refreshed successfully for subject '{}'", token.getSubject());

        return Optional.ofNullable((String) tokenResponse.get("access_token"));
    }
}
