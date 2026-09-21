-- =============================================================================
-- Flyway Migration V1: Vitalys - SYS Module Schema Initialization
-- =============================================================================

-- ── Departments ───────────────────────────────────────────────────────────
CREATE TABLE sys_department (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ── Users ─────────────────────────────────────────────────────────────────
CREATE TABLE sys_user (
    id                    BIGSERIAL PRIMARY KEY,
    username              VARCHAR(50)  NOT NULL UNIQUE,
    password_hash         VARCHAR(255),
    full_name             VARCHAR(100) NOT NULL,
    email                 VARCHAR(100) NOT NULL UNIQUE,
    phone_number          VARCHAR(20),
    auth_provider         VARCHAR(20)  DEFAULT 'LOCAL',    -- LOCAL, OAUTH2
    status                VARCHAR(20)  DEFAULT 'ACTIVE',   -- ACTIVE, INACTIVE, LOCKED, EXPIRED
    failed_login_attempts INT          DEFAULT 0,
    password_changed_at   TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    department_id         BIGINT REFERENCES sys_department(id) ON DELETE SET NULL
);

CREATE INDEX idx_sys_user_username ON sys_user(username);
CREATE INDEX idx_sys_user_email    ON sys_user(email);
CREATE INDEX idx_sys_user_status   ON sys_user(status);

-- ── Roles ─────────────────────────────────────────────────────────────────
CREATE TABLE sys_role (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,  -- IT_ADMIN, MANAGER, LAB_ADMIN, etc.
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_system   BOOLEAN DEFAULT FALSE          -- System roles cannot be deleted
);

-- ── Permissions ───────────────────────────────────────────────────────────
CREATE TABLE sys_permission (
    id          BIGSERIAL PRIMARY KEY,
    module      VARCHAR(50)  NOT NULL,         -- e.g., SYS_USER, SYS_ROLE, LAB_SAMPLE
    screen      VARCHAR(50)  NOT NULL,         -- Level 1: screen access
    action      VARCHAR(50)  NOT NULL,         -- Level 2: READ, CREATE, UPDATE, DELETE, EXPORT
    description TEXT,
    CONSTRAINT uq_perm UNIQUE (module, screen, action)
);

CREATE INDEX idx_perm_module ON sys_permission(module);

-- ── User-Role-Department Mapping (multi-role, multi-department) ───────────
CREATE TABLE sys_user_role_dept (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id       BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    department_id BIGINT REFERENCES sys_department(id) ON DELETE SET NULL,
    CONSTRAINT uq_user_role_dept UNIQUE (user_id, role_id, department_id)
);

CREATE INDEX idx_urd_user_id ON sys_user_role_dept(user_id);
CREATE INDEX idx_urd_role_id ON sys_user_role_dept(role_id);

-- ── Role-Permission Mapping ───────────────────────────────────────────────
CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ── Immutable Audit Trail ─────────────────────────────────────────────────
CREATE TABLE sys_audit_trail (
    id           BIGSERIAL PRIMARY KEY,
    module       VARCHAR(50)  NOT NULL,
    entity_name  VARCHAR(100) NOT NULL,
    entity_id    VARCHAR(100) NOT NULL,
    action       VARCHAR(20)  NOT NULL,       -- CREATE, UPDATE, DELETE
    performed_by VARCHAR(50)  NOT NULL,
    ip_address   VARCHAR(45),
    timestamp    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    old_value    JSONB,
    new_value    JSONB
);

CREATE INDEX idx_audit_module      ON sys_audit_trail(module);
CREATE INDEX idx_audit_entity      ON sys_audit_trail(entity_name, entity_id);
CREATE INDEX idx_audit_performed   ON sys_audit_trail(performed_by);
CREATE INDEX idx_audit_timestamp   ON sys_audit_trail(timestamp DESC);

-- Prevent any UPDATE or DELETE on audit trail rows (immutability guarantee)
CREATE OR REPLACE FUNCTION prevent_audit_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit Trail records are immutable and cannot be modified or deleted.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_protect_audit_trail
BEFORE UPDATE OR DELETE ON sys_audit_trail
FOR EACH ROW EXECUTE FUNCTION prevent_audit_modification();

-- ── System Event Logs ─────────────────────────────────────────────────────
CREATE TABLE sys_log (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50),
    action        VARCHAR(100) NOT NULL,
    module        VARCHAR(50),
    ip_address    VARCHAR(45),
    user_agent    TEXT,
    status        VARCHAR(20)  NOT NULL,       -- SUCCESS, FAILED
    error_message TEXT,
    timestamp     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_log_username  ON sys_log(username);
CREATE INDEX idx_log_status    ON sys_log(status);
CREATE INDEX idx_log_timestamp ON sys_log(timestamp DESC);

-- ── Token Blacklist (for logout / revocation) ─────────────────────────────
CREATE TABLE sys_token_blacklist (
    id          BIGSERIAL PRIMARY KEY,
    jti         VARCHAR(255) NOT NULL UNIQUE,  -- JWT ID claim
    username    VARCHAR(50)  NOT NULL,
    expiry_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_token_jti    ON sys_token_blacklist(jti);
CREATE INDEX idx_token_expiry ON sys_token_blacklist(expiry_at);
