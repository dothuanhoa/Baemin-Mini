package com.baemin_mini.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record AppJwtProperties(
        String secret,
        String issuer,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays) {
}