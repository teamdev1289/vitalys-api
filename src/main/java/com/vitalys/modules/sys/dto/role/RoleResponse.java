package com.vitalys.modules.sys.dto.role;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Role with full permission list for matrix view. */
@Data
@Builder
public class RoleResponse {

    private Long    id;
    private String  code;
    private String  name;
    private String  description;
    private boolean isSystem;
    private Set<String> permissionKeys; // e.g., ["SYS:USER:READ", "SYS:ROLE:UPDATE"]
}
