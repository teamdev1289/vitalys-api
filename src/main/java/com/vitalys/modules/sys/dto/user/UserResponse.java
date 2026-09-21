package com.vitalys.modules.sys.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/** User response DTO. Cost/price fields are conditionally masked. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long   id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String authProvider;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String departmentName;

    /** Role-department assignments for display. */
    private List<RoleDeptDto> assignments;

    @Data
    @Builder
    public static class RoleDeptDto {
        private Long   roleId;
        private String roleCode;
        private String roleName;
        private Long   departmentId;
        private String departmentName;
    }
}
