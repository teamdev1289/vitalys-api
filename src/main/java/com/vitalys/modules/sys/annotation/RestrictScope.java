package com.vitalys.modules.sys.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as requiring department-level data scope restriction.
 * Intercepted by {@link com.vitalys.modules.sys.aspect.DataPermissionAspect}.
 *
 * When applied, the aspect injects the authenticated user's department IDs
 * into the method context for filtering queries.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestrictScope {

    /**
     * If true, IT_ADMIN role bypasses department filtering and sees all data.
     * Default: true (admin bypass enabled).
     */
    boolean adminBypass() default true;
}
