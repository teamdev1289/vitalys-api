package com.vitalys.modules.sys.service;

import com.vitalys.common.Constants;
import com.vitalys.modules.sys.dto.auth.LoginRequest;
import com.vitalys.modules.sys.dto.auth.LoginResponse;
import com.vitalys.modules.sys.entity.*;
import com.vitalys.modules.sys.event.UserLoggedInEvent;
import com.vitalys.modules.sys.repository.*;
import com.vitalys.modules.sys.security.JwtTokenProvider;
import com.vitalys.modules.sys.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication service handling:
 * - Local login with password policy enforcement
 * - JWT access and refresh token issuance
 * - Account lockout after max failed attempts
 * - Logout with token revocation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserRepository       userRepository;
    private final JwtTokenProvider        jwtTokenProvider;
    private final TokenBlacklistService   blacklistService;
    private final AuthenticationManager   authManager;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${security.password.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.password.expiry-days:90}")
    private int passwordExpiryDays;

    /**
     * Authenticate user credentials and issue JWT tokens.
     * Enforces lockout policy and password expiry.
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = extractIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    publishLoginEvent(null, request.getUsername(), ipAddress, userAgent, false, "User not found");
                    return new BadCredentialsException("Invalid username or password");
                });

        // Check account lockout
        if (user.isLocked()) {
            publishLoginEvent(user.getId(), user.getUsername(), ipAddress, userAgent, false, "Account locked");
            throw new LockedException("Account is locked. Contact your administrator.");
        }

        try {
            // Spring Security authentication (validates password)
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            // Successful login: reset failed attempts
            user.resetFailedAttempts();

            // Check password expiry
            if (user.getPasswordChangedAt() != null) {
                OffsetDateTime expiry = user.getPasswordChangedAt().plusDays(passwordExpiryDays);
                if (OffsetDateTime.now().isAfter(expiry)) {
                    user.setStatus(Constants.STATUS_EXPIRED);
                    userRepository.save(user);
                    throw new IllegalStateException("Password has expired. Please reset your password.");
                }
            }

            userRepository.save(user);

            // Build token payload
            List<String> roles       = extractRoleCodes(user);
            List<String> permissions = extractPermissionKeys(user);
            List<Long>   deptIds     = extractDeptIds(user);

            String accessToken  = jwtTokenProvider.generateAccessToken(user, roles, permissions, deptIds);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            publishLoginEvent(user.getId(), user.getUsername(), ipAddress, userAgent, true, null);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900)  // 15 min in seconds
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .roles(roles)
                    .permissions(permissions)
                    .departmentIds(deptIds)
                    .build();

        } catch (AuthenticationException e) {
            // Increment failed attempts and lock if threshold reached
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
                user.setStatus(Constants.STATUS_LOCKED);
                log.warn("Account locked for user {} after {} failed attempts",
                        user.getUsername(), maxFailedAttempts);
            }
            userRepository.save(user);
            publishLoginEvent(user.getId(), user.getUsername(), ipAddress, userAgent, false, e.getMessage());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Revoke an access token by adding its JTI to the blacklist.
     */
    public void logout(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String jti    = jwtTokenProvider.extractJti(token);
            var    expiry = jwtTokenProvider.extractExpiry(token);
            blacklistService.revoke(jti, expiry);
        }
    }

    /**
     * Issue a new access token using a valid refresh token.
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        String username = jwtTokenProvider.extractUsername(refreshToken);
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<String> roles       = extractRoleCodes(user);
        List<String> permissions = extractPermissionKeys(user);
        List<Long>   deptIds     = extractDeptIds(user);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, roles, permissions, deptIds);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roles)
                .permissions(permissions)
                .departmentIds(deptIds)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private List<String> extractRoleCodes(SysUser user) {
        return user.getUserRoleDepts().stream()
                .map(urd -> urd.getRole().getCode())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractPermissionKeys(SysUser user) {
        return user.getUserRoleDepts().stream()
                .flatMap(urd -> urd.getRole().getPermissions().stream())
                .map(SysPermission::toKey)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> extractDeptIds(SysUser user) {
        return user.getUserRoleDepts().stream()
                .filter(urd -> urd.getDepartment() != null)
                .map(urd -> urd.getDepartment().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private void publishLoginEvent(Long userId, String username, String ip,
                                   String userAgent, boolean success, String error) {
        eventPublisher.publishEvent(
                new UserLoggedInEvent(this, username, ip, userAgent, success, error));
    }
}
