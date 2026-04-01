package com.audenyo.oidcplayground.service;

import com.audenyo.oidcplayground.model.dto.UserInfoDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public UserInfoDto extractUserInfo(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token
                && token.getPrincipal() instanceof OidcUser oidcUser) {
            return new UserInfoDto(
                    oidcUser.getSubject(),
                    oidcUser.getEmail(),
                    oidcUser.getFullName(),
                    token.getAuthorizedClientRegistrationId(),
                    oidcUser.getClaims()
            );
        }
        return null;
    }
}
