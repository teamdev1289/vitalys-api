-- =============================================================================
-- Flyway Migration V5: Vitalys - Equipment Module Enhancements & Permissions
-- =============================================================================

-- ── 1. Enhance equipment_instrument table ────────────────────────────────────
ALTER TABLE equipment_instrument
    ADD COLUMN IF NOT EXISTS serial_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(150),
    ADD COLUMN IF NOT EXISTS location VARCHAR(150),
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'IN_SERVICE' NOT NULL,
    ADD COLUMN IF NOT EXISTS purchase_date DATE,
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- ── 2. Add Equipment Permissions to sys_permission ───────────────────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('EQUIPMENT', 'INSTRUMENT', 'READ',    'View laboratory equipment and instrument details'),
    ('EQUIPMENT', 'INSTRUMENT', 'CREATE',  'Register new laboratory instruments'),
    ('EQUIPMENT', 'INSTRUMENT', 'UPDATE',  'Edit instrument details and status'),
    ('EQUIPMENT', 'INSTRUMENT', 'DELETE',  'Decommission or remove instruments'),
    ('EQUIPMENT', 'CALIBRATION','CREATE',  'Log calibration events for instruments'),
    ('EQUIPMENT', 'MAINTENANCE','CREATE',  'Log maintenance and service events')
ON CONFLICT (module, screen, action) DO NOTHING;

-- ── 3. Assign permissions to IT_ADMIN, MANAGER, LAB_ADMIN, SUPERVISOR ─────────
-- IT_ADMIN gets all new permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'IT_ADMIN'
  AND  p.module = 'EQUIPMENT'
ON CONFLICT DO NOTHING;

-- MANAGER gets READ, CREATE, UPDATE, CALIBRATION, MAINTENANCE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'MANAGER'
  AND  p.module = 'EQUIPMENT'
ON CONFLICT DO NOTHING;

-- LAB_ADMIN & SUPERVISOR get READ, UPDATE, CALIBRATION, MAINTENANCE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code IN ('LAB_ADMIN', 'SUPERVISOR')
  AND  p.module = 'EQUIPMENT'
  AND  p.action IN ('READ', 'UPDATE', 'CREATE')
ON CONFLICT DO NOTHING;

-- OPERATOR gets READ and logging calibration/maintenance
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'OPERATOR'
  AND  p.module = 'EQUIPMENT'
  AND  p.action IN ('READ', 'CREATE')
ON CONFLICT DO NOTHING;

-- GUEST gets READ only
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'GUEST'
  AND  p.module = 'EQUIPMENT'
  AND  p.action = 'READ'
ON CONFLICT DO NOTHING;

-- ── 4. Seed sample laboratory instruments ────────────────────────────────────
INSERT INTO equipment_instrument (department_id, name, model, asset_code, serial_number, manufacturer, location, status, purchase_date, notes)
VALUES
    ((SELECT id FROM sys_department WHERE code = 'BIOCHEM'),
     'Waters ACQUITY UPLC H-Class Plus', 'ACQUITY UPLC H-Class', 'EQ-HPLC-001', 'UPLC-982143', 'Waters Corporation', 'Lab Room 201 - Bench A', 'IN_SERVICE', '2023-03-15', 'Primary chromatography system for active ingredient assay'),
     
    ((SELECT id FROM sys_department WHERE code = 'BIOCHEM'),
     'Agilent 1260 Infinity II HPLC System', '1260 Infinity II', 'EQ-HPLC-002', 'AG-882710', 'Agilent Technologies', 'Lab Room 201 - Bench B', 'IN_SERVICE', '2022-07-20', 'Equipped with DAD detector for dissolution and impurity testing'),
     
    ((SELECT id FROM sys_department WHERE code = 'BIOCHEM'),
     'Shimadzu UV-1900i Spectrophotometer', 'UV-1900i', 'EQ-UV-001', 'SH-441092', 'Shimadzu', 'Lab Room 202 - Optical Bay', 'IN_SERVICE', '2023-01-10', 'Double-beam spectrophotometer with LabSolutions DB connectivity'),
     
    ((SELECT id FROM sys_department WHERE code = 'HEMATOLOGY'),
     'Mettler Toledo XPR205 Analytical Balance', 'XPR205', 'EQ-BAL-001', 'MT-654321', 'Mettler Toledo', 'Weighing Room 101', 'IN_SERVICE', '2022-11-05', '5-decimal precision micro-balance with automated draft shield'),
     
    ((SELECT id FROM sys_department WHERE code = 'MICROBIO'),
     'Thermo Scientific Heracell 150i CO2 Incubator', 'Heracell 150i', 'EQ-INC-001', 'TS-112233', 'Thermo Fisher Scientific', 'Microbiology Cleanroom B', 'IN_SERVICE', '2023-05-18', 'Solid copper chamber for sterile culture incubation');
