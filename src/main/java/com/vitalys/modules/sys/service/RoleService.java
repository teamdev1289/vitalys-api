package com.vitalys.modules.sys.service;

import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.sys.dto.role.RolePermissionUpdateRequest;
import com.vitalys.modules.sys.dto.role.RoleResponse;
import com.vitalys.modules.sys.entity.SysPermission;
import com.vitalys.modules.sys.entity.SysRole;
import com.vitalys.modules.sys.repository.SysPermissionRepository;
import com.vitalys.modules.sys.repository.SysRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Role and permission management service.
 * Provides the permission matrix data and allows atomic permission updates per role.
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleRepository       roleRepository;
    private final SysPermissionRepository permissionRepository;

    /** Fetch all roles with their permission keys — used to render the matrix. */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRolesWithPermissions() {
        return roleRepository.findAllWithPermissions().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Fetch all available permissions grouped by module. */
    @Transactional(readOnly = true)
    public List<SysPermission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Replace the permission set for a role.
     * Non-system roles can be fully updated; IT_ADMIN permissions cannot be removed.
     */
    @Auditable(module = "SYS", entity = "SysRole")
    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, RolePermissionUpdateRequest request) {
        SysRole role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));

        Set<SysPermission> newPermissions = new HashSet<>(
                permissionRepository.findAllById(request.getPermissionIds()));

        role.setPermissions(newPermissions);
        return toResponse(roleRepository.save(role));
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private RoleResponse toResponse(SysRole role) {
        Set<String> permKeys = role.getPermissions().stream()
                .map(SysPermission::toKey)
                .collect(Collectors.toSet());

        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.isSystem())
                .permissionKeys(permKeys)
                .build();
    }
}
