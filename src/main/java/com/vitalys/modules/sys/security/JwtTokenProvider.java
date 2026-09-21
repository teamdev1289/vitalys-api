package com.vitalys.modules.sys.security;

import com.vitalys.common.Constants;
import com.vitalys.modules.sys.entity.SysUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT token provider: generates, validates, and parses access and refresh tokens.
 * Access tokens are short-lived (15 min). Refresh tokens are long-lived (7 days).
 * Claims include userId, roles, permissions, and department IDs for data-scope filtering.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Value("${jwt.issuer}")
    private String issuer;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a short-lived access token carrying user identity and permissions.
     */
    public String generateAccessToken(SysUser user, List<String> roles, List<String> permissions, List<Long> deptIds) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(user.getUsername())
                .claim(Constants.CLAIM_USER_ID, user.getId())
                .claim(Constants.CLAIM_ROLES, roles)
                .claim(Constants.CLAIM_PERMISSIONS, permissions)
                .claim(Constants.CLAIM_DEPT_IDS, deptIds)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a long-lived refresh token (username only, no permissions).
     */
    public String generateRefreshToken(String username) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiryMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate token signature and expiry. Returns false if invalid.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract all claims from a token (does not validate expiry).
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return extractClaims(token).getId();
    }

    public Date extractExpiry(String token) {
        return extractClaims(token).getExpiration();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        Claims claims = extractClaims(token);
        Object perms  = claims.get(Constants.CLAIM_PERMISSIONS);
        if (perms instanceof List<?>) {
            return (List<String>) perms;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Long> extractDeptIds(String token) {
        Claims claims = extractClaims(token);
        Object depts  = claims.get(Constants.CLAIM_DEPT_IDS);
        if (depts instanceof List<?> list) {
            return list.stream()
                    .map(d -> Long.parseLong(d.toString()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
