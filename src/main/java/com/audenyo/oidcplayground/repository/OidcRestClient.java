package com.audenyo.oidcplayground.repository;

import com.audenyo.oidcplayground.model.entity.StoredRefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OidcRestClient implements OidcClient {

    private static final Logger log = LoggerFactory.getLogger(OidcRestClient.class);

    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;
    private final RestClient restClient;

    public OidcRestClient(
            @Value("${spring.security.oauth2.client.provider.adfs.token-uri}") String clientId,
            @Value("${spring.security.oauth2.client.registration.adfs.client-id}") String clientSecret,
            @Value("${spring.security.oauth2.client.registration.adfs.client-secret}") String tokenUri,
            @Qualifier("adfsRestClient") RestClient restClient
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
        this.restClient = restClient;
    }

    @Override
    public Map<String, Object> refresh(StoredRefreshToken token) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", token.getRefreshToken());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        try {
            return restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, response) -> {
                        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        throw new RuntimeException(body);
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception ex) {
            String status = ex.getMessage() != null && ex.getMessage().contains("invalid_grant")
                    ? "INVALID_GRANT"
                    : "ERROR: " + ex.getMessage();
            log.warn("Token refresh failed for subject '{}': {}", token.getSubject(), status);
            return null;
        }
    }
}
