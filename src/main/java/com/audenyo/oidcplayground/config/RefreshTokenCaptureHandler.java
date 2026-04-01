package com.audenyo.oidcplayground.config;

import com.audenyo.oidcplayground.service.RefreshTokenStorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RefreshTokenCaptureHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCaptureHandler.class);

    private final RefreshTokenStorageService storageService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public RefreshTokenCaptureHandler(
            RefreshTokenStorageService storageService,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        this.storageService = storageService;
        this.authorizedClientService = authorizedClientService;
        setDefaultTargetUrl("/user-info");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OidcUser oidcUser = (OidcUser) token.getPrincipal();

            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    token.getAuthorizedClientRegistrationId(), token.getName());

            if (authorizedClient.getRefreshToken() != null) {
                storageService.storeOrUpdate(
                        oidcUser.getSubject(),
                        oidcUser.getAttribute("upn"),
                        authorizedClient.getRefreshToken().getTokenValue());
            } else {
                log.warn("No refresh token received for subject '{}' — ensure 'offline_access' scope is granted in ADFS",
                        oidcUser.getSubject());
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
