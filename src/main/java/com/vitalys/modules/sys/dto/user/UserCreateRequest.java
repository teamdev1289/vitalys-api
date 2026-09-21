package com.vitalys.modules.sys.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/** Request body for creating a new user. */
@Data
public class UserCreateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username may only contain letters, digits, _ . -")
    private String username;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    /**
     * Initial password must satisfy the policy:
     * ≥8 chars, uppercase, lowercase, digit, special character.
     */
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must be ≥8 chars with uppercase, lowercase, digit, and special character"
    )
    private String password;

    /** Role-department assignment pairs. */
    private List<RoleDeptAssignment> roleDeptAssignments;

    @Data
    public static class RoleDeptAssignment {
        private Long roleId;
        private Long departmentId; // nullable: global role
    }
}
