package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.ConflictException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.config.AppJwtProperties;
import com.baemin_mini.domain.entity.RefreshToken;
import com.baemin_mini.domain.entity.Role;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.RoleName;
import com.baemin_mini.dto.auth.AuthResponse;
import com.baemin_mini.dto.auth.CurrentUserResponse;
import com.baemin_mini.dto.auth.LoginRequest;
import com.baemin_mini.dto.auth.LogoutRequest;
import com.baemin_mini.dto.auth.RefreshTokenRequest;
import com.baemin_mini.dto.auth.RegisterRequest;
import com.baemin_mini.repository.RefreshTokenRepository;
import com.baemin_mini.repository.RoleRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.security.JwtService;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements com.baemin_mini.service.AuthService {
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppJwtProperties jwtProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == RoleName.ADMIN || request.role() == RoleName.RESTAURANT) {
            throw new BadRequestException("Public registration only supports CUSTOMER or SHIPPER");
        }
        ensureUniqueUser(request.username(), request.email(), request.phone());

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new BadRequestException("Role is not initialized: " + request.role()));

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(blankToNull(request.phone()));
        user.setEmail(blankToNull(request.email()));
        user.setIsActive(true);
        user.addRole(role);
        userRepository.save(user);

        return issueTokens(user, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return issueTokens(user, deviceInfo);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String deviceInfo) {
        String username;
        String currentJti;
        Instant refreshExpiresAt;
        try {
            if (!jwtService.isRefreshToken(request.refreshToken())) {
                throw new BadCredentialsException("Invalid refresh token");
            }
            username = jwtService.extractUsername(request.refreshToken());
            currentJti = jwtService.extractJti(request.refreshToken());
            refreshExpiresAt = jwtService.extractExpiration(request.refreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (currentJti == null || currentJti.isBlank() || refreshExpiresAt == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        RefreshToken currentSession = refreshTokenRepository.findByJti(currentJti)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (!currentSession.getUser().getId().equals(user.getId())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (!currentSession.getTokenHash().equals(hashToken(request.refreshToken()))) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        LocalDateTime now = LocalDateTime.now();
        if (currentSession.getRevokedAt() != null || !currentSession.getExpiresAt().isAfter(now)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Instant instantNow = Instant.now();
        Instant accessExpiresAt = instantNow.plusSeconds(jwtProperties.accessTokenTtlMinutes() * 60);
        Instant nextRefreshExpiresAt = instantNow.plusSeconds(jwtProperties.refreshTokenTtlDays() * 24 * 60 * 60);
        String nextJti = jwtService.generateJti();
        String accessToken = jwtService.generateAccessToken(user, instantNow, accessExpiresAt);
        String refreshToken = jwtService.generateRefreshToken(user, nextJti, instantNow, nextRefreshExpiresAt);

        currentSession.setRevokedAt(now);
        currentSession.setReplacedByJti(nextJti);
        refreshTokenRepository.save(currentSession);

        RefreshToken nextSession = createRefreshTokenSession(user, refreshToken, nextJti, nextRefreshExpiresAt, deviceInfo);
        refreshTokenRepository.save(nextSession);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                toLocalDateTime(accessExpiresAt),
                toLocalDateTime(nextRefreshExpiresAt),
                toCurrentUser(user));
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String jti;
        try {
            if (!jwtService.isRefreshToken(request.refreshToken())) {
                return;
            }
            jti = jwtService.extractJti(request.refreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            return;
        }
        refreshTokenRepository.findByJti(jti).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("User is inactive");
        }
        return toCurrentUser(user);
    }

    private AuthResponse issueTokens(User user, String deviceInfo) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusSeconds(jwtProperties.accessTokenTtlMinutes() * 60);
        Instant refreshExpiresAt = now.plusSeconds(jwtProperties.refreshTokenTtlDays() * 24 * 60 * 60);
        String refreshJti = jwtService.generateJti();
        String accessToken = jwtService.generateAccessToken(user, now, accessExpiresAt);
        String refreshToken = jwtService.generateRefreshToken(user, refreshJti, now, refreshExpiresAt);

        RefreshToken session = createRefreshTokenSession(user, refreshToken, refreshJti, refreshExpiresAt, deviceInfo);
        refreshTokenRepository.save(session);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                toLocalDateTime(accessExpiresAt),
                toLocalDateTime(refreshExpiresAt),
                toCurrentUser(user));
    }

    private RefreshToken createRefreshTokenSession(User user, String rawToken, String jti, Instant expiresAt, String deviceInfo) {
        RefreshToken session = new RefreshToken();
        session.setUser(user);
        session.setTokenHash(hashToken(rawToken));
        session.setJti(jti);
        session.setExpiresAt(toLocalDateTime(expiresAt));
        session.setDeviceInfo(deviceInfo);
        return session;
    }

    private void ensureUniqueUser(String username, String email, String phone) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }
        String normalizedEmail = blankToNull(email);
        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already exists");
        }
        String normalizedPhone = blankToNull(phone);
        if (normalizedPhone != null && userRepository.existsByPhone(normalizedPhone)) {
            throw new ConflictException("Phone already exists");
        }
    }

    private CurrentUserResponse toCurrentUser(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().name())
                .sorted()
                .toList();
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getIsActive(),
                roles);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, SYSTEM_ZONE);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}