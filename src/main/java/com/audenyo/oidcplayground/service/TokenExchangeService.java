package com.audenyo.oidcplayground.service;

import com.audenyo.oidcplayground.model.dto.TokenExchangeResultDto;
import com.audenyo.oidcplayground.model.entity.KnownUser;
import com.audenyo.oidcplayground.repository.KnownUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenExchangeService {

    private static final String GRANT_JWT_BEARER = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String GRANT_REFRESH_TOKEN = "refresh_token";

    private final RestClient adfsRestClient;
    private final KnownUserRepository knownUserRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.provider.adfs.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.adfs.client-id}")
    private String webClientId;

    @Value("${spring.security.oauth2.client.registration.adfs.client-secret}")
    private String webClientSecret;

    @Value("${spring.security.oauth2.client.registration.adfs-backend.client-id}")
    private String backendClientId;

    @Value("${spring.security.oauth2.client.registration.adfs-backend.client-secret}")
    private String backendClientSecret;

    @Value("${token-exchange.resource}")
    private String resource;

    public TokenExchangeService(RestClient adfsRestClient,
                                KnownUserRepository knownUserRepository,
                                ObjectMapper objectMapper) {
        this.adfsRestClient = adfsRestClient;
        this.knownUserRepository = knownUserRepository;
        this.objectMapper = objectMapper;
    }

    public TokenExchangeResultDto exchangeForUser(KnownUser user) throws Exception {
        String accessToken = resolveAccessToken(user);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", GRANT_JWT_BEARER);
        params.add("client_id", backendClientId);
        params.add("client_secret", backendClientSecret);
        params.add("assertion", accessToken);
        params.add("requested_token_use", "on_behalf_of");
        params.add("resource", resource);
        params.add("scope", "openid profile email");

        Map<String, Object> response = adfsRestClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        String rawAccessToken = (String) response.get("access_token");
        String tokenType = (String) response.getOrDefault("token_type", "");
        Map<String, Object> decodedClaims = decodeJwtPayload(rawAccessToken);

        return new TokenExchangeResultDto(user.getSub(), rawAccessToken, tokenType, decodedClaims, Instant.now());
    }

    private String resolveAccessToken(KnownUser user) {
        Instant expiresAt = user.getAccessTokenExpiresAt();
        boolean expired = expiresAt == null || Instant.now().isAfter(expiresAt.minusSeconds(30));
        if (!expired) {
            return user.getAccessToken();
        }
        if (user.getRefreshToken() == null) {
            throw new IllegalStateException("Access token expired and no refresh token available for sub=" + user.getSub());
        }
        return refreshAccessToken(user);
    }

    private String refreshAccessToken(KnownUser user) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", GRANT_REFRESH_TOKEN);
        params.add("client_id", webClientId);
        params.add("client_secret", webClientSecret);
        params.add("refresh_token", user.getRefreshToken());

        Map<String, Object> response = adfsRestClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

        String newAccessToken = (String) response.get("access_token");
        String newRefreshToken = (String) response.getOrDefault("refresh_token", user.getRefreshToken());
        long expiresIn = ((Number) response.getOrDefault("expires_in", 3600)).longValue();
        Instant newExpiresAt = Instant.now().plusSeconds(expiresIn);

        user.updateTokens(newAccessToken, newExpiresAt, newRefreshToken);
        knownUserRepository.save(user);

        return newAccessToken;
    }

    private Map<String, Object> decodeJwtPayload(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
