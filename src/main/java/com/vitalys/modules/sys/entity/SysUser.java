package com.vitalys.modules.sys.entity;

import com.vitalys.common.Constants;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * System user entity. Supports both local (username/password) and OAuth2 authentication.
 * Password policy: lockout after 5 failed attempts, expiry after 90 days.
 */
@Entity
@Table(name = "sys_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * Authentication provider: LOCAL or OAUTH2.
     */
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private String authProvider = Constants.PROVIDER_LOCAL;

    /**
     * Account lifecycle status: ACTIVE, INACTIVE, LOCKED, EXPIRED.
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = Constants.STATUS_ACTIVE;

    /**
     * Counter for consecutive failed login attempts.
     * Resets to 0 on successful login.
     */
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

    /**
     * Timestamp of the last password change.
     * Used to enforce password expiry policy.
     */
    @Column(name = "password_changed_at")
    private OffsetDateTime passwordChangedAt;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private SysDepartment department;

    /**
     * Bidirectional mapping to role-department assignments.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SysUserRoleDept> userRoleDepts = new HashSet<>();

    // ── Helper methods ────────────────────────────────────────────────────

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public boolean isActive() {
        return Constants.STATUS_ACTIVE.equals(this.status);
    }

    public boolean isLocked() {
        return Constants.STATUS_LOCKED.equals(this.status);
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
