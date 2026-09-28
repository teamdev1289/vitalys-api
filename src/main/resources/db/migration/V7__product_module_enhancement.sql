-- =============================================================================
-- Flyway Migration V7: Vitalys - Product & Batch Module Enhancements & Permissions
-- =============================================================================

-- ── 1. Enhance product_product table ─────────────────────────────────────────
ALTER TABLE product_product
    ADD COLUMN IF NOT EXISTS product_code VARCHAR(100) UNIQUE,
    ADD COLUMN IF NOT EXISTS description TEXT;

-- ── 2. Enhance product_batch table ───────────────────────────────────────────
ALTER TABLE product_batch
    ADD COLUMN IF NOT EXISTS product_id BIGINT,
    ADD COLUMN IF NOT EXISTS unit VARCHAR(50) DEFAULT 'TABLETS',
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- ── 3. Add Product & Batch Permissions to sys_permission ─────────────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('PRODUCT', 'MASTER', 'READ',   'View pharmaceutical product catalog and formulations'),
    ('PRODUCT', 'MASTER', 'CREATE', 'Register new products, dosage forms and active ingredients'),
    ('PRODUCT', 'MASTER', 'UPDATE', 'Edit product registration details and specifications'),
    ('PRODUCT', 'MASTER', 'DELETE', 'Discontinue or delete pharmaceutical products'),
    ('PRODUCT', 'BATCH',  'READ',   'View production and trial batches'),
    ('PRODUCT', 'BATCH',  'CREATE', 'Log and register manufacturing batches'),
    ('PRODUCT', 'BATCH',  'UPDATE', 'Update batch status (QUARANTINE, RELEASED, REJECTED)'),
    ('PRODUCT', 'BATCH',  'DELETE', 'Archive or delete batches')
ON CONFLICT (module, screen, action) DO NOTHING;

-- ── 4. Assign permissions to roles ───────────────────────────────────────────
-- IT_ADMIN gets all PRODUCT permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'IT_ADMIN'
  AND  p.module = 'PRODUCT'
ON CONFLICT DO NOTHING;

-- MANAGER gets full PRODUCT permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'MANAGER'
  AND  p.module = 'PRODUCT'
ON CONFLICT DO NOTHING;

-- LAB_ADMIN & SUPERVISOR get READ, CREATE, UPDATE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code IN ('LAB_ADMIN', 'SUPERVISOR')
  AND  p.module = 'PRODUCT'
  AND  p.action IN ('READ', 'CREATE', 'UPDATE')
ON CONFLICT DO NOTHING;

-- OPERATOR gets READ and BATCH UPDATE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'OPERATOR'
  AND  p.module = 'PRODUCT'
  AND  p.action IN ('READ', 'UPDATE')
ON CONFLICT DO NOTHING;

-- GUEST gets READ
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'GUEST'
  AND  p.module = 'PRODUCT'
  AND  p.action = 'READ'
ON CONFLICT DO NOTHING;

-- ── 5. Seed sample pharmaceutical products & ingredients ─────────────────────
INSERT INTO product_ingredient (name, inn_name, cas_number, type)
VALUES
    ('Paracetamol BP', 'Paracetamol / Acetaminophen', '103-90-2', 'API'),
    ('Amoxicillin Trihydrate', 'Amoxicillin', '61336-70-7', 'API'),
    ('Ascorbic Acid USP', 'Ascorbic Acid / Vitamin C', '50-81-7', 'API'),
    ('Microcrystalline Cellulose PH102', 'Cellulose, Microcrystalline', '9004-34-6', 'EXCIPIENT'),
    ('Magnesium Stearate', 'Magnesium Stearate', '557-04-0', 'EXCIPIENT')
ON CONFLICT DO NOTHING;

INSERT INTO product_product (product_code, registration_number, product_name, dosage_form, packaging_spec, shelf_life_months, registrant, manufacturer, country_of_origin, quality_standard, product_category, classification, status, description)
VALUES
    ('PRD-PARA-500', 'VD-31245-19', 'Paracetamol 500mg Film-Coated Tablets', 'Film-Coated Tablet', 'Blister 10x10 tablets / Box', 36, 'Hau Giang Pharmaceutical JSC', 'DHG Pharma Factory - Tan Phu Thanh', 'Vietnam', 'USP 43', 'FINISHED_PRODUCT', 'OTC', 'ACTIVE', 'Analgesic and antipyretic medication indicated for mild to moderate pain relief and fever reduction'),
    
    ('PRD-AMOX-500', 'VN-22180-18', 'Amoxicillin 500mg Hard Capsules', 'Hard Gelatin Capsule', 'Blister 10x10 capsules / Box', 24, 'Sanofi Vietnam Co., Ltd.', 'Sanofi High-Tech Facility HCMC', 'Vietnam', 'BP 2022', 'FINISHED_PRODUCT', 'ETC', 'ACTIVE', 'Broad-spectrum penicillin antibiotic for systemic bacterial infections'),
    
    ('PRD-VITC-1000', 'VD-29810-18', 'Vitamin C 1000mg/5ml Solution for Injection', 'Sterile Injectable Solution', 'Ampoule 5ml, Box of 10 ampoules', 24, 'Traphaco JSC', 'Traphaco Hung Yen High-Tech Factory', 'Vietnam', 'Vietnamese Pharmacopoeia V', 'FINISHED_PRODUCT', 'ETC', 'ACTIVE', 'High-dose parenteral ascorbic acid for acute deficiency or adjunctive therapy')
ON CONFLICT (product_code) DO NOTHING;

-- ── 6. Seed sample batches ───────────────────────────────────────────────────
INSERT INTO product_batch (product_id, batch_number, manufacturing_date, expiry_date, quantity_produced, unit, status, notes)
VALUES
    ((SELECT id FROM product_product WHERE product_code = 'PRD-PARA-500'),
     'BATCH-260101', '2026-01-15 00:00:00+00', '2029-01-14 23:59:59+00', 500000.0, 'TABLETS', 'RELEASED', 'Commercial production batch passed all release assay and dissolution tests'),

    ((SELECT id FROM product_product WHERE product_code = 'PRD-PARA-500'),
     'BATCH-260305', '2026-03-05 00:00:00+00', '2029-03-04 23:59:59+00', 500000.0, 'TABLETS', 'QUARANTINE', 'Undergoing routine chemical QC testing, awaiting microbiological release'),

    ((SELECT id FROM product_product WHERE product_code = 'PRD-AMOX-500'),
     'BATCH-AMX-2602', '2026-02-10 00:00:00+00', '2028-02-09 23:59:59+00', 250000.0, 'CAPSULES', 'RELEASED', 'Validated aseptic packaging line batch, assay 100.2% label claim'),

    ((SELECT id FROM product_product WHERE product_code = 'PRD-VITC-1000'),
     'BATCH-VTC-2604', '2026-04-01 00:00:00+00', '2028-03-31 23:59:59+00', 50000.0, 'AMPOULES', 'IN_TESTING', 'Accelerated stability protocol pull month 0 baseline verification')
ON CONFLICT DO NOTHING;
