-- =============================================================================
-- Flyway Migration V3: Vitalys - SYS Module Mock Users for All Roles
-- Default password for all mock accounts: Admin@123
-- BCrypt hash (strength 12): $2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG
-- =============================================================================

-- ── 1. Ensure admin user has correct department and valid password ────────────
UPDATE sys_user 
SET    department_id = (SELECT id FROM sys_department WHERE code = 'ADMIN'),
       password_hash = '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
       failed_login_attempts = 0,
       status = 'ACTIVE'
WHERE  username = 'admin';

-- ── 2. Insert mock users for all system roles ────────────────────────────────
-- Accounts created:
-- 1. itadmin     - Role: IT_ADMIN    - Dept: Administration (ADMIN)
-- 2. manager     - Role: MANAGER     - Dept: Administration (ADMIN)
-- 3. labadmin    - Role: LAB_ADMIN   - Dept: Hematology Lab (HEMATOLOGY)
-- 4. supervisor  - Role: SUPERVISOR  - Dept: Biochemistry Lab (BIOCHEM)
-- 5. operator    - Role: OPERATOR    - Dept: Microbiology Lab (MICROBIO)
-- 6. guest       - Role: GUEST       - Dept: Pathology Lab (PATHOLOGY)

INSERT INTO sys_user (username, password_hash, full_name, email, phone_number, auth_provider, status, password_changed_at, department_id)
VALUES
    ('itadmin', 
     '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'IT Admin Officer', 
     'itadmin@vitalys.com', 
     '+84901000001', 
     'LOCAL', 
     'ACTIVE', 
     CURRENT_TIMESTAMP, 
     (SELECT id FROM sys_department WHERE code = 'ADMIN')),

    ('manager', 
     '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'Laboratory Manager', 
     'manager@vitalys.com', 
     '+84901000002', 
     'LOCAL', 
     'ACTIVE', 
     CURRENT_TIMESTAMP, 
     (SELECT id FROM sys_department WHERE code = 'ADMIN')),

    ('labadmin', 
     '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'Lab Administrator (Hematology)', 
     'labadmin@vitalys.com', 
     '+84901000003', 
     'LOCAL', 
     'ACTIVE', 
     CURRENT_TIMESTAMP, 
     (SELECT id FROM sys_department WHERE code = 'HEMATOLOGY')),

    ('supervisor', 
     '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'Lab Supervisor (Biochem)', 
     'supervisor@vitalys.com', 
     '+84901000004', 
     'LOCAL', 
     'ACTIVE', 
     CURRENT_TIMESTAMP, 
     (SELECT id FROM sys_department WHERE code = 'BIOCHEM')),

    ('operator', 
     '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'Lab Operator (Microbiology)', 
     'operator@vitalys.com', 
     '+84901000005', 
     'LOCAL', 
     'ACTIVE', 
     CURRENT_TIMESTAMP, 
     (SELECT id FROM sys_department WHERE code = 'MICROBIO')),

    ('guest', 
     '$2a$12$9R4ZQcJ66.Z/0ixgWJ7/bemHPtGeaAblNgeXnmMtkZsEHzU6QWXlG',
     'Guest User (Read-Only)', 
     'guest@vitalys.com', 
     '+84901000006', 
     'LOCAL', 
     'ACTIVE', 
     CURRENT_TIMESTAMP, 
     (SELECT id FROM sys_department WHERE code = 'PATHOLOGY'))
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    status        = 'ACTIVE',
    failed_login_attempts = 0;

-- ── 3. Assign Role and Department mappings (sys_user_role_dept) ───────────────

-- IT_ADMIN role for itadmin
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'itadmin'
  AND  r.code = 'IT_ADMIN'
  AND  d.code = 'ADMIN'
ON CONFLICT (user_id, role_id, department_id) DO NOTHING;

-- MANAGER role for manager (assigned across ADMIN, HEMATOLOGY, BIOCHEM)
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'manager'
  AND  r.code = 'MANAGER'
  AND  d.code IN ('ADMIN', 'HEMATOLOGY', 'BIOCHEM')
ON CONFLICT (user_id, role_id, department_id) DO NOTHING;

-- LAB_ADMIN role for labadmin (Hematology)
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'labadmin'
  AND  r.code = 'LAB_ADMIN'
  AND  d.code = 'HEMATOLOGY'
ON CONFLICT (user_id, role_id, department_id) DO NOTHING;

-- SUPERVISOR role for supervisor (Biochemistry)
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'supervisor'
  AND  r.code = 'SUPERVISOR'
  AND  d.code = 'BIOCHEM'
ON CONFLICT (user_id, role_id, department_id) DO NOTHING;

-- OPERATOR role for operator (Microbiology)
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'operator'
  AND  r.code = 'OPERATOR'
  AND  d.code = 'MICROBIO'
ON CONFLICT (user_id, role_id, department_id) DO NOTHING;

-- GUEST role for guest (Pathology)
INSERT INTO sys_user_role_dept (user_id, role_id, department_id)
SELECT u.id, r.id, d.id
FROM   sys_user u, sys_role r, sys_department d
WHERE  u.username = 'guest'
  AND  r.code = 'GUEST'
  AND  d.code = 'PATHOLOGY'
ON CONFLICT (user_id, role_id, department_id) DO NOTHING;

-- ── 4. Audit Trail entries for mock user creation ────────────────────────────
INSERT INTO sys_audit_trail (module, entity_name, entity_id, action, performed_by, ip_address, new_value)
SELECT 
    'SYS', 
    'SysUser', 
    u.id::text, 
    'CREATE', 
    'SYSTEM', 
    '127.0.0.1',
    jsonb_build_object(
        'username', u.username,
        'email', u.email,
        'fullName', u.full_name,
        'status', u.status,
        'department', d.name
    )
FROM sys_user u
LEFT JOIN sys_department d ON u.department_id = d.id
WHERE u.username IN ('itadmin', 'manager', 'labadmin', 'supervisor', 'operator', 'guest');
