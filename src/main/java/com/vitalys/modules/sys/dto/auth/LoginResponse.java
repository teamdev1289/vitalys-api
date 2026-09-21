package com.vitalys.modules.sys.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Login response containing access token and user context. */
@Data
@Builder
public class LoginResponse {

    private String       accessToken;
    private String       refreshToken;
    private String       tokenType;
    private long         expiresIn;         // seconds
    private Long         userId;
    private String       username;
    private String       fullName;
    private List<String> roles;
    private List<String> permissions;
    private List<Long>   departmentIds;
}
