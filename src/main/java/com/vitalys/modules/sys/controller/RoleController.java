package com.vitalys.modules.sys.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sys.dto.role.RolePermissionUpdateRequest;
import com.vitalys.modules.sys.dto.role.RoleResponse;
import com.vitalys.modules.sys.entity.SysPermission;
import com.vitalys.modules.sys.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role and permission matrix controller.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role and permission matrix endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles with their permission sets — for matrix view")
    @GetMapping
    @PreAuthorize("hasAuthority('SYS:ROLE:READ')")
    public ResponseEntity<ResponseDto<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(ResponseDto.ok(roleService.getAllRolesWithPermissions()));
    }

    @Operation(summary = "Get all available permissions")
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('SYS:ROLE:READ')")
    public ResponseEntity<ResponseDto<List<SysPermission>>> getAllPermissions() {
        return ResponseEntity.ok(ResponseDto.ok(roleService.getAllPermissions()));
    }

    @Operation(summary = "Update permission set for a role (replaces existing)")
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('SYS:ROLE:UPDATE')")
    public ResponseEntity<ResponseDto<RoleResponse>> updatePermissions(
            @PathVariable Long roleId,
            @RequestBody RolePermissionUpdateRequest request) {
        return ResponseEntity.ok(
                ResponseDto.ok("Permissions updated", roleService.updateRolePermissions(roleId, request)));
    }
}
