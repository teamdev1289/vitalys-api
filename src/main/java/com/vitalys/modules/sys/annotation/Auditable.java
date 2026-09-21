package com.vitalys.modules.sys.annotation;

import java.lang.annotation.*;

/**
 * Marks a service method for automatic audit trail recording.
 * Intercepted by {@link com.vitalys.modules.sys.aspect.AuditAspect}.
 *
 * Usage:
 * <pre>
 *   {@literal @}Auditable(module = "SYS", entity = "SysUser")
 *   public UserResponse createUser(UserCreateRequest req) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** The functional module, e.g., "SYS", "LAB". */
    String module();

    /** The entity class name, e.g., "SysUser", "LabSample". */
    String entity();
}
