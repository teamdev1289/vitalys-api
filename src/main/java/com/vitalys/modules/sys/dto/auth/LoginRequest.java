package com.vitalys.modules.sys.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Login request payload for local username/password authentication. */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
