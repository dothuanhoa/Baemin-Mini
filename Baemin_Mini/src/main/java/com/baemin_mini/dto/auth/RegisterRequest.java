package com.baemin_mini.dto.auth;

import com.baemin_mini.domain.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 20) String phone,
        @Email @Size(max = 100) String email,
        @NotNull RoleName role) {
}