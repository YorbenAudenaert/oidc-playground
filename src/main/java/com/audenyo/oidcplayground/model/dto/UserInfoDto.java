package com.audenyo.oidcplayground.model.dto;

import java.util.Map;

public record UserInfoDto(
        String subject,
        String email,
        String fullName,
        String provider,
        Map<String, Object> claims
) {}
