package com.audenyo.oidcplayground.service;

import com.audenyo.oidcplayground.model.entity.KnownUser;
import com.audenyo.oidcplayground.repository.KnownUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LoginEventListener {

    private static final Logger log = LoggerFactory.getLogger(LoginEventListener.class);

    private final KnownUserRepository knownUserRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public LoginEventListener(KnownUserRepository knownUserRepository,
                              OAuth2AuthorizedClientService authorizedClientService) {
        this.knownUserRepository = knownUserRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof OAuth2LoginAuthenticationToken token
                && token.getPrincipal() instanceof OidcUser oidcUser) {

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    token.getClientRegistration().getRegistrationId(),
                    token.getName()
            );
            if (client == null) {
                log.warn("No authorized client found for sub={}", oidcUser.getSubject());
                return;
            }

            OAuth2AccessToken accessToken = client.getAccessToken();
            OAuth2RefreshToken refreshToken = client.getRefreshToken();

            String sub = oidcUser.getSubject();
            String name = oidcUser.getFullName();
            Instant expiresAt = accessToken.getExpiresAt();
            String refreshTokenValue = refreshToken != null ? refreshToken.getTokenValue() : null;

            knownUserRepository.findById(sub).ifPresentOrElse(
                    existing -> {
                        existing.updateTokens(accessToken.getTokenValue(), expiresAt, refreshTokenValue);
                        knownUserRepository.save(existing);
                        log.debug("Updated tokens for user: sub={}", sub);
                    },
                    () -> {
                        knownUserRepository.save(new KnownUser(sub, name, Instant.now(),
                                accessToken.getTokenValue(), expiresAt, refreshTokenValue));
                        log.debug("Registered new user: sub={}, name={}", sub, name);
                    }
            );
        }
    }
}
