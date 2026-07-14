package com.baemin_mini.dto.auth;

import java.time.LocalDateTime;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        LocalDateTime accessTokenExpiresAt,
        LocalDateTime refreshTokenExpiresAt,
        CurrentUserResponse user) {
}