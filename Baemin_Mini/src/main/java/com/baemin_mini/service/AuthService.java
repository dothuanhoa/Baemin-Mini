package com.baemin_mini.service;

import com.baemin_mini.dto.auth.AuthResponse;
import com.baemin_mini.dto.auth.CurrentUserResponse;
import com.baemin_mini.dto.auth.LoginRequest;
import com.baemin_mini.dto.auth.LogoutRequest;
import com.baemin_mini.dto.auth.RefreshTokenRequest;
import com.baemin_mini.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String deviceInfo);

    AuthResponse refresh(RefreshTokenRequest request, String deviceInfo);

    void logout(LogoutRequest request);

    CurrentUserResponse me(String username);
}