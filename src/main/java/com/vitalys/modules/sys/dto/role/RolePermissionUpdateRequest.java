package com.vitalys.modules.sys.dto.role;

import lombok.Data;

import java.util.Set;

/** Request to update the set of permissions assigned to a role. */
@Data
public class RolePermissionUpdateRequest {

    /** Set of permission IDs to assign to this role. Replaces existing assignments. */
    private Set<Long> permissionIds;
}
