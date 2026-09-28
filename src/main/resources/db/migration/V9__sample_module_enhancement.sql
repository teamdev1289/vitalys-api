-- ============================================================================
-- V9__sample_module_enhancement.sql
-- Module: SAMPLE (Test Requests, Sample Accessioning, Barcoding, Chain of Custody)
-- Compliance: 21 CFR Part 11, GxP, ALCOA+ Audit Trail
-- ============================================================================

-- ── 1. Enhance sample_test_request table ──────────────────────────────────────
ALTER TABLE sample_test_request ADD COLUMN IF NOT EXISTS request_code VARCHAR(100);
ALTER TABLE sample_test_request ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE sample_test_request ADD COLUMN IF NOT EXISTS spec_set_id BIGINT;
ALTER TABLE sample_test_request ADD COLUMN IF NOT EXISTS notes TEXT;

-- Foreign keys
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_sample_test_request_product_id') THEN
        ALTER TABLE sample_test_request ADD CONSTRAINT fk_sample_test_request_product_id FOREIGN KEY (product_id) REFERENCES product_product(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_sample_test_request_spec_set_id') THEN
        ALTER TABLE sample_test_request ADD CONSTRAINT fk_sample_test_request_spec_set_id FOREIGN KEY (spec_set_id) REFERENCES method_specification_set(id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_sample_test_request_code ON sample_test_request(request_code);

-- ── 2. Enhance sample_sample table ───────────────────────────────────────────
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS barcode VARCHAR(100);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS spec_set_id BIGINT;
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS storage_condition VARCHAR(100);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS current_location VARCHAR(255);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS quantity DOUBLE PRECISION;
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS unit VARCHAR(50);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS received_by VARCHAR(100);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS assigned_to VARCHAR(100);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS sampling_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS sampling_location VARCHAR(255);
ALTER TABLE sample_sample ADD COLUMN IF NOT EXISTS notes TEXT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_sample_sample_spec_set_id') THEN
        ALTER TABLE sample_sample ADD CONSTRAINT fk_sample_sample_spec_set_id FOREIGN KEY (spec_set_id) REFERENCES method_specification_set(id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_sample_sample_code ON sample_sample(sample_code);
CREATE INDEX IF NOT EXISTS idx_sample_sample_barcode ON sample_sample(barcode);
CREATE INDEX IF NOT EXISTS idx_sample_sample_status ON sample_sample(status);

-- ── 3. Create sample_chain_of_custody table ──────────────────────────────────
CREATE TABLE IF NOT EXISTS sample_chain_of_custody (
    id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT NOT NULL REFERENCES sample_sample(id) ON DELETE RESTRICT,
    from_user VARCHAR(100),
    to_user VARCHAR(100) NOT NULL,
    from_location VARCHAR(255),
    to_location VARCHAR(255) NOT NULL,
    transferred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    purpose VARCHAR(255),
    sample_condition VARCHAR(100),
    storage_condition VARCHAR(100),
    signature_token VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sample_coc_sample_id ON sample_chain_of_custody(sample_id);
CREATE INDEX IF NOT EXISTS idx_sample_coc_transferred_at ON sample_chain_of_custody(transferred_at);

-- ── 4. Seed Permissions for SAMPLE module ─────────────────────────────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('SAMPLE', 'REQUEST',    'READ',     'View testing requests and details'),
    ('SAMPLE', 'REQUEST',    'CREATE',   'Submit new testing request'),
    ('SAMPLE', 'REQUEST',    'UPDATE',   'Update pending testing request'),
    ('SAMPLE', 'REQUEST',    'CANCEL',   'Cancel unreceived testing request'),

    ('SAMPLE', 'ACCESSION',  'READ',     'View sample accessioning dashboard'),
    ('SAMPLE', 'ACCESSION',  'CREATE',   'Receive samples, generate barcodes and labels'),

    ('SAMPLE', 'SAMPLE',     'READ',     'View samples list, details, and tracking'),
    ('SAMPLE', 'SAMPLE',     'UPDATE',   'Update sample metadata, test assignment'),
    ('SAMPLE', 'SAMPLE',     'DISPOSE',  'Record sample disposal/destruction'),

    ('SAMPLE', 'CUSTODY',    'READ',     'View Chain of Custody logs and history'),
    ('SAMPLE', 'CUSTODY',    'TRANSFER', 'Transfer sample custody and location')
ON CONFLICT (module, screen, action) DO NOTHING;

-- Assign all SAMPLE permissions to IT_ADMIN, MANAGER, LAB_ADMIN, SUPERVISOR, OPERATOR
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE p.module = 'SAMPLE'
  AND r.code IN ('IT_ADMIN', 'MANAGER', 'LAB_ADMIN', 'SUPERVISOR')
ON CONFLICT DO NOTHING;

-- OPERATOR gets read, accession, update, custody
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE p.module = 'SAMPLE'
  AND r.code = 'OPERATOR'
  AND p.action IN ('READ', 'CREATE', 'UPDATE', 'TRANSFER')
ON CONFLICT DO NOTHING;

-- ── 5. Seed sample Test Requests ──────────────────────────────────────────────
INSERT INTO sample_test_request (
    request_code, source_type, sample_type, product_id, batch_id, customer_id, project_id,
    spec_set_id, requested_by, request_date, due_date, priority, test_scope, status, notes
)
VALUES
    (
        'REQ-20260901-001', 'BATCH', 'FINISHED_PRODUCT',
        (SELECT id FROM product_product WHERE product_code = 'PRD-PARA-500'),
        (SELECT id FROM product_batch WHERE batch_number = 'BATCH-260101'),
        NULL, NULL,
        (SELECT id FROM method_specification_set WHERE spec_code = 'SPEC-PARA-500-VN'),
        'DHG Production Line 1', '2026-09-01 08:30:00+00', '2026-09-05 17:00:00+00',
        'NORMAL', 'RELEASE_TESTING', 'RECEIVED',
        'Finished tablet release testing per VN Pharmacopoeia V monograph'
    ),
    (
        'REQ-20260915-002', 'CUSTOMER', 'FINISHED_PRODUCT',
        (SELECT id FROM product_product WHERE product_code = 'PRD-AMOX-500'),
        (SELECT id FROM product_batch WHERE batch_number = 'BATCH-AMX-2602'),
        (SELECT id FROM partner_customer WHERE code = 'CUST-SNF-001'),
        (SELECT id FROM partner_project WHERE code = 'PRJ-SNF-001'),
        (SELECT id FROM method_specification_set WHERE spec_code = 'SPEC-AMOX-500-BP'),
        'Sanofi Vietnam QA', '2026-09-15 09:00:00+00', '2026-09-18 17:00:00+00',
        'URGENT', 'STABILITY_PULL_TESTING', 'IN_TESTING',
        '3-month accelerated stability pull testing at 40°C / 75% RH'
    ),
    (
        'REQ-20260925-003', 'BATCH', 'STERILE_INJECTABLE',
        (SELECT id FROM product_product WHERE product_code = 'PRD-VITC-1000'),
        (SELECT id FROM product_batch WHERE batch_number = 'BATCH-VTC-2604'),
        NULL, NULL,
        (SELECT id FROM method_specification_set WHERE spec_code = 'SPEC-PARA-500-VN'),
        'Traphaco Parenteral Dept', '2026-09-25 14:00:00+00', '2026-09-30 17:00:00+00',
        'EMERGENCY', 'RELEASE_TESTING', 'SUBMITTED',
        'Urgent commercial batch release assay & sterility verification'
    )
ON CONFLICT (request_code) DO NOTHING;

-- ── 6. Seed sample Samples ────────────────────────────────────────────────────
INSERT INTO sample_sample (
    request_id, department_id, spec_set_id, sample_code, barcode,
    storage_condition, current_location, quantity, unit,
    received_by, assigned_to, sampling_date, sampling_location,
    status, received_at, notes
)
VALUES
    (
        (SELECT id FROM sample_test_request WHERE request_code = 'REQ-20260901-001'),
        (SELECT id FROM sys_department WHERE code = 'BIOCHEM'),
        (SELECT id FROM method_specification_set WHERE spec_code = 'SPEC-PARA-500-VN'),
        'SMP-20260901-0001', 'BAR-SMP-260901-001',
        'AMBIENT_15_25C', 'QC Chemistry Room - Rack B2', 100.0, 'TABLETS',
        'labadmin', 'operator', '2026-09-01 08:00:00+00', 'DHG Cleanroom Suite 3',
        'TESTING', '2026-09-01 09:00:00+00',
        'Primary sample for Paracetamol HPLC assay & dissolution testing'
    ),
    (
        (SELECT id FROM sample_test_request WHERE request_code = 'REQ-20260901-001'),
        (SELECT id FROM sys_department WHERE code = 'BIOCHEM'),
        (SELECT id FROM method_specification_set WHERE spec_code = 'SPEC-PARA-500-VN'),
        'SMP-20260901-0002', 'BAR-SMP-260901-002',
        'COLD_2_8C', 'Cold Storage Unit 1 - Shelf A3', 100.0, 'TABLETS',
        'labadmin', 'operator', '2026-09-01 08:00:00+00', 'DHG Cleanroom Suite 3',
        'RECEIVED', '2026-09-01 09:00:00+00',
        'Retention reserve sample (retained in cold storage for 36 months)'
    ),
    (
        (SELECT id FROM sample_test_request WHERE request_code = 'REQ-20260915-002'),
        (SELECT id FROM sys_department WHERE code = 'BIOCHEM'),
        (SELECT id FROM method_specification_set WHERE spec_code = 'SPEC-AMOX-500-BP'),
        'SMP-20260915-0003', 'BAR-SMP-260915-003',
        'FREEZER_MINUS_20C', 'Deep Freezer F-01 - Box 4', 50.0, 'CAPSULES',
        'labadmin', 'operator', '2026-09-15 08:30:00+00', 'Sanofi Stability Chamber #2',
        'TESTING', '2026-09-15 09:30:00+00',
        'Amoxicillin stability testing month 3 pull sample'
    )
ON CONFLICT (sample_code) DO NOTHING;

-- ── 7. Seed Chain of Custody History ─────────────────────────────────────────
INSERT INTO sample_chain_of_custody (
    sample_id, from_user, to_user, from_location, to_location,
    transferred_at, purpose, sample_condition, storage_condition, notes
)
VALUES
    (
        (SELECT id FROM sample_sample WHERE sample_code = 'SMP-20260901-0001'),
        'DHG Logistics Courier', 'labadmin',
        'DHG Warehouse Dock', 'Sample Accessioning Desk',
        '2026-09-01 09:00:00+00', 'Sample Accessioning & Initial Receipt',
        'INTACT', 'AMBIENT_15_25C', 'Package sealed, security tamper-evident tape intact'
    ),
    (
        (SELECT id FROM sample_sample WHERE sample_code = 'SMP-20260901-0001'),
        'labadmin', 'operator',
        'Sample Accessioning Desk', 'QC Chemistry Room - Rack B2',
        '2026-09-01 10:15:00+00', 'Handover for HPLC Assay Preparation',
        'INTACT', 'AMBIENT_15_25C', 'Transferred to analyst bench for sample weighing'
    ),
    (
        (SELECT id FROM sample_sample WHERE sample_code = 'SMP-20260901-0002'),
        'labadmin', 'labadmin',
        'Sample Accessioning Desk', 'Cold Storage Unit 1 - Shelf A3',
        '2026-09-01 10:30:00+00', 'Long-term Retention Archiving',
        'INTACT', 'COLD_2_8C', 'Placed in monitored retention cold room at 4.2°C'
    );

-- ── 8. Seed Sample Status History ─────────────────────────────────────────────
INSERT INTO sample_sample_status_history (
    sample_id, from_status, to_status, changed_by, changed_at, reason
)
VALUES
    (
        (SELECT id FROM sample_sample WHERE sample_code = 'SMP-20260901-0001'),
        'SUBMITTED', 'RECEIVED', 'labadmin', '2026-09-01 09:00:00+00',
        'Sample physically received at accessioning desk and verified against packing slip'
    ),
    (
        (SELECT id FROM sample_sample WHERE sample_code = 'SMP-20260901-0001'),
        'RECEIVED', 'TESTING', 'operator', '2026-09-01 10:30:00+00',
        'Assigned to analyst and started dissolution testing'
    ),
    (
        (SELECT id FROM sample_sample WHERE sample_code = 'SMP-20260901-0002'),
        'SUBMITTED', 'RECEIVED', 'labadmin', '2026-09-01 09:00:00+00',
        'Reserve sample received and logged for retention archiving'
    );
