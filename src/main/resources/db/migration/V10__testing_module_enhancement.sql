-- ============================================================================
-- V10__testing_module_enhancement.sql
-- Module: TESTING, EXECUTION, DYNAMIC RESULTS, REVISIONS & OOS INVESTIGATION
-- Vitalys LIMS / SDMS / ELN Platform - Phase 4
-- ============================================================================

-- ── 1. Enhance testing_analytical_run ─────────────────────────────────────────
ALTER TABLE testing_analytical_run
    ADD COLUMN IF NOT EXISTS run_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS analyst VARCHAR(100),
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS batch_size INTEGER DEFAULT 1,
    ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE INDEX IF NOT EXISTS idx_testing_run_code ON testing_analytical_run(run_code);
CREATE INDEX IF NOT EXISTS idx_testing_run_analyst ON testing_analytical_run(analyst);

-- ── 2. Enhance testing_test ───────────────────────────────────────────────────
ALTER TABLE testing_test
    ADD COLUMN IF NOT EXISTS test_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS spec_item_id BIGINT REFERENCES method_specification_item(id),
    ADD COLUMN IF NOT EXISTS assigned_to VARCHAR(100),
    ADD COLUMN IF NOT EXISTS priority VARCHAR(50) DEFAULT 'NORMAL',
    ADD COLUMN IF NOT EXISTS due_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE INDEX IF NOT EXISTS idx_testing_test_code ON testing_test(test_code);
CREATE INDEX IF NOT EXISTS idx_testing_test_assigned_to ON testing_test(assigned_to);
CREATE INDEX IF NOT EXISTS idx_testing_test_status ON testing_test(status);
CREATE INDEX IF NOT EXISTS idx_testing_test_sample_id ON testing_test(sample_id);

-- ── 3. Enhance testing_result ─────────────────────────────────────────────────
ALTER TABLE testing_result
    ADD COLUMN IF NOT EXISTS text_value TEXT,
    ADD COLUMN IF NOT EXISTS spec_min DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS spec_max DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS spec_target VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_oos BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS oos_investigation_id BIGINT,
    ADD COLUMN IF NOT EXISTS entered_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS entered_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_testing_result_test_id ON testing_result(test_id);
CREATE INDEX IF NOT EXISTS idx_testing_result_is_oos ON testing_result(is_oos);

-- ── 4. Create testing_oos_investigation table ──────────────────────────────────
CREATE TABLE IF NOT EXISTS testing_oos_investigation (
    id BIGSERIAL PRIMARY KEY,
    investigation_code VARCHAR(100) UNIQUE NOT NULL,
    result_id BIGINT NOT NULL REFERENCES testing_result(id) ON DELETE RESTRICT,
    test_id BIGINT NOT NULL REFERENCES testing_test(id) ON DELETE RESTRICT,
    sample_id BIGINT NOT NULL REFERENCES sample_sample(id) ON DELETE RESTRICT,
    phase VARCHAR(50) NOT NULL DEFAULT 'PHASE_1_LAB',
    root_cause_category VARCHAR(100),
    immediate_action TEXT,
    investigation_findings TEXT,
    investigated_by VARCHAR(100) NOT NULL,
    investigated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    supervisor_reviewed_by VARCHAR(100),
    supervisor_comments TEXT,
    retest_approved BOOLEAN DEFAULT FALSE,
    conclusion VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_testing_oos_code ON testing_oos_investigation(investigation_code);
CREATE INDEX IF NOT EXISTS idx_testing_oos_sample_id ON testing_oos_investigation(sample_id);
CREATE INDEX IF NOT EXISTS idx_testing_oos_status ON testing_oos_investigation(status);

-- ── 5. Seed Permissions for TESTING Module ───────────────────────────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('TESTING', 'WORKLIST',   'READ',        'View analyst worklist assignments'),
    ('TESTING', 'TEST',       'READ',        'View testing execution details'),
    ('TESTING', 'TEST',       'ASSIGN',      'Assign tests to analysts and set due dates'),
    ('TESTING', 'TEST',       'EXECUTE',     'Start and execute testing procedures'),
    ('TESTING', 'RESULT',     'ENTER',       'Enter and submit quantitative and qualitative results'),
    ('TESTING', 'RESULT',     'REVISE',      'Revise test results with mandatory GxP justification'),
    ('TESTING', 'OOS',        'INVESTIGATE', 'Manage Out of Specification (OOS) lab investigations'),
    ('TESTING', 'RUN',        'CREATE',      'Log new analytical instrument runs and sequences'),
    ('TESTING', 'RUN',        'READ',        'View analytical instrument run sequences')
ON CONFLICT (module, screen, action) DO NOTHING;

-- Grant all permissions to IT_ADMIN, MANAGER, LAB_ADMIN, SUPERVISOR
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE p.module = 'TESTING'
  AND r.code IN ('IT_ADMIN', 'MANAGER', 'LAB_ADMIN', 'SUPERVISOR')
ON CONFLICT DO NOTHING;

-- Grant OPERATOR permissions (WORKLIST, TEST:READ, TEST:EXECUTE, RESULT:ENTER, RESULT:REVISE, RUN:CREATE, RUN:READ)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE p.module = 'TESTING'
  AND r.code = 'OPERATOR'
  AND p.action IN ('READ', 'EXECUTE', 'ENTER', 'REVISE', 'CREATE')
ON CONFLICT DO NOTHING;

-- ── 6. Seed Realistic Data for Phase 4 Testing & Worklist ─────────────────────

-- Insert Analytical Run 1 (HPLC Run)
INSERT INTO testing_analytical_run (id, run_code, name, instrument_id, run_date, analyst, status, batch_size, notes)
VALUES (1, 'RUN-20260901-001', 'HPLC Paracetamol Monograph Assay Sequence', 1, '2026-09-01 10:00:00+00', 'operator', 'COMPLETED', 12, 'Agilent 1260 HPLC Sequence completed with acceptable RSD < 1.0%')
ON CONFLICT (id) DO NOTHING;

-- Insert Active Tests for Sample 4 (Paracetamol sample)
INSERT INTO testing_test (id, test_code, sample_id, method_id, run_id, form_template_id, spec_item_id, status, assigned_to, priority, due_date, started_at, notes)
VALUES
    (1, 'TST-20260901-0001', 4, 1, 1, 1, 1, 'COMPLETED', 'operator', 'NORMAL', CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '4 hours', 'Paracetamol Active Ingredient Content Assay'),
    (2, 'TST-20260901-0002', 4, 1, NULL, NULL, 2, 'IN_PROGRESS', 'operator', 'NORMAL', CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 hour', 'Tablet Dissolution Test 30min in 0.05M Phosphate buffer'),
    (3, 'TST-20260901-0003', 4, 1, NULL, NULL, 3, 'ASSIGNED', 'analyst_hoa', 'URGENT', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, '4-Aminophenol Related Impurity Assay')
ON CONFLICT (id) DO NOTHING;

-- Insert Result for Test 1 (Passed Assay: 100.4%)
INSERT INTO testing_result (id, test_id, analyte, value, text_value, unit, spec_min, spec_max, spec_target, pass_fail, is_oos, entered_by, entered_at)
VALUES
    (1, 1, 'Paracetamol Content Assay', 100.4, '100.4', '% LC', 98.5, 101.5, '98.5% - 101.5% of label claim', 'PASS', FALSE, 'operator', CURRENT_TIMESTAMP - INTERVAL '2 hours')
ON CONFLICT (id) DO NOTHING;

-- Adjust Sequences
SELECT setval('testing_analytical_run_id_seq', (SELECT COALESCE(MAX(id), 1) FROM testing_analytical_run));
SELECT setval('testing_test_id_seq', (SELECT COALESCE(MAX(id), 1) FROM testing_test));
SELECT setval('testing_result_id_seq', (SELECT COALESCE(MAX(id), 1) FROM testing_result));
