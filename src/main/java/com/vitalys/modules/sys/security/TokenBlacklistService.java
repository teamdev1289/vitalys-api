package com.vitalys.modules.sys.security;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token blacklist for logout/revocation support.
 * In production, replace with Redis for distributed deployments.
 *
 * Entries are automatically removed after the token's natural expiry.
 */
@Service
public class TokenBlacklistService {

    // Simple in-memory store: JTI -> expiry timestamp (ms)
    // Replace with Redis for horizontal scaling
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * Add a token JTI to the blacklist until its natural expiry.
     */
    public void revoke(String jti, Date expiry) {
        blacklist.put(jti, expiry.getTime());
    }

    /**
     * Check if a JTI is currently blacklisted.
     * Automatically evicts expired entries on access.
     */
    public boolean isBlacklisted(String jti) {
        Long expiry = blacklist.get(jti);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            blacklist.remove(jti); // Evict expired entry
            return false;
        }
        return true;
    }
}
