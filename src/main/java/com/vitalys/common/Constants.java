package com.vitalys.common;

/**
 * Application-wide constants for status codes, role names, and permission keys.
 */
public final class Constants {

    private Constants() {}

    // ── User statuses ─────────────────────────────────────────────────────
    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_LOCKED   = "LOCKED";
    public static final String STATUS_EXPIRED  = "EXPIRED";

    // ── Auth providers ────────────────────────────────────────────────────
    public static final String PROVIDER_LOCAL  = "LOCAL";
    public static final String PROVIDER_OAUTH2 = "OAUTH2";

    // ── System role codes ─────────────────────────────────────────────────
    public static final String ROLE_IT_ADMIN   = "IT_ADMIN";
    public static final String ROLE_MANAGER    = "MANAGER";
    public static final String ROLE_LAB_ADMIN  = "LAB_ADMIN";
    public static final String ROLE_SUPERVISOR = "SUPERVISOR";
    public static final String ROLE_OPERATOR   = "OPERATOR";
    public static final String ROLE_GUEST      = "GUEST";

    // ── Audit actions ─────────────────────────────────────────────────────
    public static final String AUDIT_CREATE = "CREATE";
    public static final String AUDIT_UPDATE = "UPDATE";
    public static final String AUDIT_DELETE = "DELETE";

    // ── System log actions ────────────────────────────────────────────────
    public static final String LOG_ACTION_LOGIN           = "USER_LOGIN";
    public static final String LOG_ACTION_LOGOUT          = "USER_LOGOUT";
    public static final String LOG_ACTION_LOGIN_FAILED    = "USER_LOGIN_FAILED";
    public static final String LOG_ACTION_ACCOUNT_LOCKED  = "ACCOUNT_LOCKED";
    public static final String LOG_ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGED";

    // ── System log statuses ───────────────────────────────────────────────
    public static final String LOG_STATUS_SUCCESS = "SUCCESS";
    public static final String LOG_STATUS_FAILED  = "FAILED";

    // ── Permission actions ────────────────────────────────────────────────
    public static final String PERM_READ       = "READ";
    public static final String PERM_CREATE     = "CREATE";
    public static final String PERM_UPDATE     = "UPDATE";
    public static final String PERM_DELETE     = "DELETE";
    public static final String PERM_EXPORT     = "EXPORT";
    public static final String PERM_VIEW_PRICE = "VIEW_PRICE";

    // ── JWT claims ────────────────────────────────────────────────────────
    public static final String CLAIM_ROLES       = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_DEPT_IDS    = "deptIds";
    public static final String CLAIM_USER_ID     = "userId";

    // ── WebSocket destinations ────────────────────────────────────────────
    public static final String WS_TOPIC_LOGS    = "/topic/logs";
    public static final String WS_TOPIC_AUDIT   = "/topic/audit";
    public static final String WS_APP_PREFIX    = "/app";
}
