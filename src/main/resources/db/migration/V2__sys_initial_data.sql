-- =============================================================================
-- Flyway Migration V2: Vitalys - SYS Module Initial Seed Data
-- =============================================================================

-- ── Departments ───────────────────────────────────────────────────────────
INSERT INTO sys_department (code, name, description) VALUES
    ('ADMIN',     'Administration',           'System administration department'),
    ('HEMATOLOGY','Hematology Laboratory',    'Blood analysis and cell counting'),
    ('BIOCHEM',   'Biochemistry Laboratory',  'Chemical analysis and metabolic testing'),
    ('MICROBIO',  'Microbiology Laboratory',  'Bacterial and viral culture testing'),
    ('PATHOLOGY', 'Pathology Laboratory',     'Tissue and biopsy analysis');

-- ── Roles ─────────────────────────────────────────────────────────────────
INSERT INTO sys_role (code, name, description, is_system) VALUES
    ('IT_ADMIN',    'IT Administrator',     'Full system access, user & role management', TRUE),
    ('MANAGER',     'Laboratory Manager',   'Manage lab operations, view all reports',    TRUE),
    ('LAB_ADMIN',   'Lab Administrator',    'Manage samples, orders within department',   TRUE),
    ('SUPERVISOR',  'Lab Supervisor',       'Supervise lab technicians, approve results', TRUE),
    ('OPERATOR',    'Lab Operator',         'Enter test results, handle samples',         TRUE),
    ('GUEST',       'Guest / Read-only',    'Read-only access to allowed modules',        TRUE);

-- ── Permissions ───────────────────────────────────────────────────────────
-- SYS_USER module permissions
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SYS', 'USER', 'READ',   'View user list and user details'),
    ('SYS', 'USER', 'CREATE', 'Create new users'),
    ('SYS', 'USER', 'UPDATE', 'Edit existing users'),
    ('SYS', 'USER', 'DELETE', 'Delete users'),
    ('SYS', 'USER', 'EXPORT', 'Export user data to CSV');

-- SYS_ROLE module permissions
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SYS', 'ROLE', 'READ',   'View roles and permission matrix'),
    ('SYS', 'ROLE', 'CREATE', 'Create new roles'),
    ('SYS', 'ROLE', 'UPDATE', 'Edit roles and permission assignments'),
    ('SYS', 'ROLE', 'DELETE', 'Delete non-system roles');

-- SYS_AUDIT module permissions
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SYS', 'AUDIT', 'READ',   'View audit trail records'),
    ('SYS', 'AUDIT', 'EXPORT', 'Export audit trail to CSV');

-- SYS_LOG module permissions
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SYS', 'LOG', 'READ', 'View system event logs');

-- SYS_DEPT module permissions
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SYS', 'DEPARTMENT', 'READ',   'View department list'),
    ('SYS', 'DEPARTMENT', 'CREATE', 'Create departments'),
    ('SYS', 'DEPARTMENT', 'UPDATE', 'Edit departments'),
    ('SYS', 'DEPARTMENT', 'DELETE', 'Delete departments');

-- Financial visibility permission
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SYS', 'FINANCIAL', 'VIEW_PRICE', 'View cost and price fields');

-- ── Admin User (password: Admin@123, BCrypt encoded) ──────────────────────
-- BCrypt hash for "Admin@123" with strength 12:
INSERT INTO sys_user (username, password_hash, full_name, email, auth_provider, status, password_changed_at, department_id) VALUES
    ('admin', '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'System Administrator', 'admin@vitalys.com', 'LOCAL', 'ACTIVE', CURRENT_TIMESTAMP,
     (SELECT id FROM sys_department WHERE code = 'ADMIN'));

-- ── Assign IT_ADMIN role to admin user ────────────────────────────────────
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'admin'
  AND  r.code = 'IT_ADMIN'
  AND  d.code = 'ADMIN';

-- ── Assign ALL permissions to IT_ADMIN role ───────────────────────────────
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'IT_ADMIN';

-- ── Assign read permissions to MANAGER role ───────────────────────────────
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'MANAGER'
  AND  p.action IN ('READ', 'EXPORT', 'VIEW_PRICE');

-- ── Assign limited permissions to OPERATOR role ───────────────────────────
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'OPERATOR'
  AND  p.module = 'SYS'
  AND  p.screen = 'USER'
  AND  p.action = 'READ';

-- ── Assign read-only permissions to GUEST role ────────────────────────────
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'GUEST'
  AND  p.action = 'READ'
  AND  p.screen NOT IN ('AUDIT', 'LOG');

-- ── LAB_ADMIN gets user + audit read within their dept ────────────────────
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'LAB_ADMIN'
  AND  ((p.module = 'SYS' AND p.screen = 'USER' AND p.action IN ('READ', 'CREATE', 'UPDATE'))
     OR (p.module = 'SYS' AND p.screen = 'AUDIT' AND p.action = 'READ')
     OR (p.module = 'SYS' AND p.screen = 'FINANCIAL' AND p.action = 'VIEW_PRICE'));

-- ── SUPERVISOR gets user read + audit read + price view ───────────────────
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'SUPERVISOR'
  AND  ((p.module = 'SYS' AND p.screen = 'USER'      AND p.action = 'READ')
     OR (p.module = 'SYS' AND p.screen = 'AUDIT'     AND p.action = 'READ')
     OR (p.module = 'SYS' AND p.screen = 'FINANCIAL' AND p.action = 'VIEW_PRICE'));

-- ── Initial audit log entry ───────────────────────────────────────────────
INSERT INTO sys_audit_trail (module, entity_name, entity_id, action, performed_by, ip_address, new_value)
VALUES ('SYS', 'SysUser', '1', 'CREATE', 'SYSTEM', '127.0.0.1',
        '{"username":"admin","email":"admin@vitalys.com","status":"ACTIVE"}'::jsonb);
