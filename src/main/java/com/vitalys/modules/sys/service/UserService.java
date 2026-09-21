package com.vitalys.modules.sys.service;

import com.vitalys.common.Constants;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.sys.dto.user.UserCreateRequest;
import com.vitalys.modules.sys.dto.user.UserResponse;
import com.vitalys.modules.sys.entity.*;
import com.vitalys.modules.sys.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service: CRUD, status management, role/department assignment.
 * All mutating operations emit audit trail via @Auditable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository        userRepository;
    private final SysRoleRepository        roleRepository;
    private final SysDepartmentRepository  deptRepository;
    private final SysUserRoleDeptRepository urdRepository;
    private final PasswordEncoder          passwordEncoder;

    /**
     * Create a new user with multi-role and department assignments.
     */
    @Auditable(module = "SYS", entity = "SysUser")
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .authProvider(Constants.PROVIDER_LOCAL)
                .status(Constants.STATUS_ACTIVE)
                .passwordChangedAt(OffsetDateTime.now())
                .build();

        user = userRepository.save(user);

        // Assign roles and departments
        if (request.getRoleDeptAssignments() != null) {
            final SysUser savedUser = user;
            List<SysUserRoleDept> assignments = request.getRoleDeptAssignments().stream()
                    .map(a -> {
                        SysRole role = roleRepository.findById(a.getRoleId())
                                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + a.getRoleId()));
                        SysDepartment dept = null;
                        if (a.getDepartmentId() != null) {
                            dept = deptRepository.findById(a.getDepartmentId())
                                    .orElseThrow(() -> new EntityNotFoundException("Department not found: " + a.getDepartmentId()));
                        }
                        return SysUserRoleDept.builder()
                                .user(savedUser)
                                .role(role)
                                .department(dept)
                                .build();
                    })
                    .collect(Collectors.toList());
            urdRepository.saveAll(assignments);
        }

        return toResponse(user);
    }

    /** Get paginated user list with optional search filters. */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String search, String status, Long departmentId, Pageable pageable) {
        return userRepository.searchUsers(search, status, departmentId, pageable)
                .map(this::toResponse);
    }

    /** Get a single user by ID. */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        return toResponse(user);
    }

    /**
     * Toggle user status: ACTIVE ↔ INACTIVE, or unlock a LOCKED account.
     */
    @Auditable(module = "SYS", entity = "SysUser")
    @Transactional
    public UserResponse updateUserStatus(Long id, String newStatus) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        if (!List.of(Constants.STATUS_ACTIVE, Constants.STATUS_INACTIVE).contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        user.setStatus(newStatus);
        if (Constants.STATUS_ACTIVE.equals(newStatus)) {
            user.resetFailedAttempts(); // Reset lockout on manual activation
        }
        return toResponse(userRepository.save(user));
    }

    /**
     * Delete a user. Cascades to role-dept assignments.
     */
    @Auditable(module = "SYS", entity = "SysUser")
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private UserResponse toResponse(SysUser user) {
        List<UserResponse.RoleDeptDto> assignments = user.getUserRoleDepts().stream()
                .map(urd -> UserResponse.RoleDeptDto.builder()
                        .roleId(urd.getRole().getId())
                        .roleCode(urd.getRole().getCode())
                        .roleName(urd.getRole().getName())
                        .departmentId(urd.getDepartment() != null ? urd.getDepartment().getId() : null)
                        .departmentName(urd.getDepartment() != null ? urd.getDepartment().getName() : null)
                        .build())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .authProvider(user.getAuthProvider())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .assignments(assignments)
                .build();
    }
}
