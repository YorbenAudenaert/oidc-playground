package com.audenyo.oidcplayground.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtAssertionService {

    @Value("${token-exchange.private-key-path}")
    private Resource privateKeyResource;

    @Value("${spring.security.oauth2.client.registration.adfs-backend.client-id}")
    private String backendClientId;

    @Value("${spring.security.oauth2.client.provider.adfs.token-uri}")
    private String tokenUri;

    private RSAPrivateKey privateKey;

    @PostConstruct
    public void loadPrivateKey() throws Exception {
        String pem = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    public String buildAssertion(String userSub) throws Exception {
        Date now = new Date();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(backendClientId)
                .subject(userSub)
                .audience(List.of(tokenUri))
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + 60_000))
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(privateKey));
        return jwt.serialize();
    }
}
