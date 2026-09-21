package com.vitalys.modules.sys.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps a user to a role within an optional department context.
 * Enables multi-role and multi-department assignment per user.
 */
@Entity
@Table(name = "sys_user_role_dept",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_role_dept",
               columnNames = {"user_id", "role_id", "department_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUserRoleDept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private SysRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private SysDepartment department;
}
