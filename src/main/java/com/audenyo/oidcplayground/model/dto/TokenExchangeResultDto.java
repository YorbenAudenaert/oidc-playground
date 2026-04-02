package com.audenyo.oidcplayground.model.dto;

import java.time.Instant;
import java.util.Map;

public record TokenExchangeResultDto(
        String sub,
        String rawAccessToken,
        String tokenType,
        Map<String, Object> decodedClaims,
        Instant executedAt
) {}
