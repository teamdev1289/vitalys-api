package com.vitalys.modules.sys.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sys.dto.user.UserCreateRequest;
import com.vitalys.modules.sys.dto.user.UserResponse;
import com.vitalys.modules.sys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User management controller: CRUD, status management.
 * All endpoints require SYS:USER:* permissions enforced via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations for system users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "List users with search and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('SYS:USER:READ')")
    public ResponseEntity<ResponseDto<Page<UserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(userService.getUsers(search, status, departmentId, pageable)));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS:USER:READ')")
    public ResponseEntity<ResponseDto<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(userService.getUserById(id)));
    }

    @Operation(summary = "Create a new user")
    @PostMapping
    @PreAuthorize("hasAuthority('SYS:USER:CREATE')")
    public ResponseEntity<ResponseDto<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created(created));
    }

    @Operation(summary = "Update user status (ACTIVE, INACTIVE)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SYS:USER:UPDATE')")
    public ResponseEntity<ResponseDto<UserResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(
                ResponseDto.ok("Status updated", userService.updateUserStatus(id, status)));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS:USER:DELETE')")
    public ResponseEntity<ResponseDto<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ResponseDto.noContent("User deleted successfully"));
    }
}
