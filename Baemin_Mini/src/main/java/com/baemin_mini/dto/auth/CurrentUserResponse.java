package com.baemin_mini.dto.auth;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String username,
        String fullName,
        String phone,
        String email,
        Boolean isActive,
        List<String> roles) {
}