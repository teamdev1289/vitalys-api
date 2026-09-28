package com.vitalys.modules.approval.service;

import com.vitalys.modules.sys.entity.SysUser;
import com.vitalys.modules.sys.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;

/**
 * 21 CFR Part 11 Electronic Signature verification and SHA-256 hash generation service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ESignatureService {

    private final SysUserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Re-authenticates user identity with raw password as mandated by 21 CFR Part 11.
     */
    public SysUser verifySignerCredentials(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Username and password are required for 21 CFR Part 11 electronic signature");
        }

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("21 CFR Part 11 Electronic signature rejected: invalid password for user {}", username);
            throw new IllegalArgumentException("Xác thực chữ ký điện tử thất bại: Mật khẩu không chính xác (21 CFR Part 11)");
        }

        return user;
    }

    /**
     * Generates a tamper-evident SHA-256 digital signature hash.
     */
    public String generateSignatureHash(String username, Long entityId, String meaning, OffsetDateTime timestamp) {
        try {
            String payload = String.format("USER:%s|ENTITY:%d|MEANING:%s|TIME:%s|SALT:VITALYS_21CFR11_GxP",
                    username, entityId, meaning, timestamp.toString());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
