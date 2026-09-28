-- =============================================================================
-- Flyway Migration V6: Vitalys - Partner Module Enhancements & Permissions
-- =============================================================================

-- ── 1. Enhance partner_customer table ────────────────────────────────────────
ALTER TABLE partner_customer
    ADD COLUMN IF NOT EXISTS code VARCHAR(50) UNIQUE,
    ADD COLUMN IF NOT EXISTS address TEXT,
    ADD COLUMN IF NOT EXISTS tax_id VARCHAR(50),
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- ── 2. Enhance partner_project table ─────────────────────────────────────────
ALTER TABLE partner_project
    ADD COLUMN IF NOT EXISTS code VARCHAR(50) UNIQUE,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL,
    ADD COLUMN IF NOT EXISTS start_date DATE,
    ADD COLUMN IF NOT EXISTS end_date DATE,
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- ── 3. Add Partner Permissions to sys_permission ─────────────────────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('PARTNER', 'CUSTOMER', 'READ',   'View customer profiles and contact list'),
    ('PARTNER', 'CUSTOMER', 'CREATE', 'Register new corporate or external customers'),
    ('PARTNER', 'CUSTOMER', 'UPDATE', 'Edit customer details and contact info'),
    ('PARTNER', 'CUSTOMER', 'DELETE', 'Deactivate or delete customer accounts'),
    ('PARTNER', 'PROJECT',  'READ',   'View analytical testing projects for customers'),
    ('PARTNER', 'PROJECT',  'CREATE', 'Create new customer testing projects'),
    ('PARTNER', 'PROJECT',  'UPDATE', 'Edit project details, status and timeline'),
    ('PARTNER', 'PROJECT',  'DELETE', 'Archive or delete projects')
ON CONFLICT (module, screen, action) DO NOTHING;

-- ── 4. Assign permissions to roles ───────────────────────────────────────────
-- IT_ADMIN gets all PARTNER permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'IT_ADMIN'
  AND  p.module = 'PARTNER'
ON CONFLICT DO NOTHING;

-- MANAGER gets full PARTNER permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'MANAGER'
  AND  p.module = 'PARTNER'
ON CONFLICT DO NOTHING;

-- LAB_ADMIN & SUPERVISOR get READ, CREATE, UPDATE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code IN ('LAB_ADMIN', 'SUPERVISOR')
  AND  p.module = 'PARTNER'
  AND  p.action IN ('READ', 'CREATE', 'UPDATE')
ON CONFLICT DO NOTHING;

-- OPERATOR gets READ
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'OPERATOR'
  AND  p.module = 'PARTNER'
  AND  p.action = 'READ'
ON CONFLICT DO NOTHING;

-- GUEST gets READ
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'GUEST'
  AND  p.module = 'PARTNER'
  AND  p.action = 'READ'
ON CONFLICT DO NOTHING;

-- ── 5. Seed sample customers & projects ──────────────────────────────────────
INSERT INTO partner_customer (code, name, contact_email, contact_phone, address, tax_id, status, notes)
VALUES
    ('CUST-DHG-001', 'Hau Giang Pharmaceutical JSC', 'qc@dhgpharma.com.vn', '+84 292 3891433', '288 Bis Nguyen Van Cu, An Hoa, Ninh Kieu, Can Tho', '1800156801', 'ACTIVE', 'Major domestic generic manufacturer - Regular finished product assays'),
    ('CUST-TRAPHACO-002', 'Traphaco Joint Stock Company', 'qa@traphaco.com.vn', '+84 24 37341797', '75 Yen Ninh, Ba Dinh, Hanoi', '0100108656', 'ACTIVE', 'Herbal medicines and modern pharmaceutical formulation partner'),
    ('CUST-SANOFI-003', 'Sanofi Vietnam Co., Ltd.', 'contact.vn@sanofi.com', '+84 28 38298526', 'Lot I-8-1, N2 Road, Saigon Hi-Tech Park, District 9, HCMC', '0300806497', 'ACTIVE', 'Multinational audit standard contract testing')
ON CONFLICT (code) DO NOTHING;

INSERT INTO partner_project (code, customer_id, name, description, status, start_date, end_date, notes)
VALUES
    ('PRJ-DHG-STAB-2026', 
     (SELECT id FROM partner_customer WHERE code = 'CUST-DHG-001'),
     'Paracetamol 500mg Stability Testing Program',
     'Accelerated and long-term stability testing for Paracetamol 500mg tablets in Zone IVb',
     'ACTIVE', '2026-01-01', '2027-12-31', '3-month, 6-month, 12-month interval pulls'),

    ('PRJ-DHG-IMP-2026', 
     (SELECT id FROM partner_customer WHERE code = 'CUST-DHG-001'),
     'Related Substances & Impurities Profiling',
     'HPLC-DAD impurity profiling for active pharmaceutical ingredients (API)',
     'ACTIVE', '2026-03-01', '2026-09-30', 'Compliance with USP/BP standards'),

    ('PRJ-TRAPH-HERBAL-01', 
     (SELECT id FROM partner_customer WHERE code = 'CUST-TRAPHACO-002'),
     'Ginkgo Biloba Extract Heavy Metal & Pesticide Assay',
     'Testing total flavonoids and ginkgolides content via HPLC',
     'ACTIVE', '2026-02-15', '2026-08-31', 'Raw material validation batch')
ON CONFLICT (code) DO NOTHING;
