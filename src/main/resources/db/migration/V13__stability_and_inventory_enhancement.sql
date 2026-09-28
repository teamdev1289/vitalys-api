-- =============================================================================
-- Migration V13: Stability Testing (ICH Q1A) & Laboratory Inventory / Prep
-- Vitalys LIMS / SDMS / ELN Platform - Phase 7
-- =============================================================================

-- ── 1. Enhance INVENTORY Tables ───────────────────────────────────────────────
ALTER TABLE inventory_inventory_item
    ADD COLUMN IF NOT EXISTS code VARCHAR(100) UNIQUE,
    ADD COLUMN IF NOT EXISTS cas_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS grade VARCHAR(100),
    ADD COLUMN IF NOT EXISTS storage_condition VARCHAR(150),
    ADD COLUMN IF NOT EXISTS safety_hazard VARCHAR(100),
    ADD COLUMN IF NOT EXISTS current_stock DOUBLE PRECISION DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'ACTIVE';

ALTER TABLE inventory_inventory_lot
    ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(150),
    ADD COLUMN IF NOT EXISTS initial_quantity DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS unit VARCHAR(50),
    ADD COLUMN IF NOT EXISTS received_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS coa_available BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'AVAILABLE';

ALTER TABLE inventory_preparation_record
    ADD COLUMN IF NOT EXISTS solution_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS sop_reference VARCHAR(150),
    ADD COLUMN IF NOT EXISTS target_volume DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS unit VARCHAR(50),
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- ── 2. Create STABILITY Tables (ICH Q1A) ───────────────────────────────────────

-- Stability Study Header
CREATE TABLE IF NOT EXISTS stability_study (
    id BIGSERIAL PRIMARY KEY,
    study_code VARCHAR(100) UNIQUE NOT NULL,
    study_title VARCHAR(255) NOT NULL,
    product_id BIGINT REFERENCES product_product(id) ON DELETE SET NULL,
    batch_id BIGINT REFERENCES product_batch(id) ON DELETE SET NULL,
    study_type VARCHAR(50) NOT NULL, -- LONG_TERM, ACCELERATED, INTERMEDIATE
    protocol_number VARCHAR(100),
    duration_months INT NOT NULL DEFAULT 24,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- PLANNED, ACTIVE, COMPLETED, CANCELLED
    created_by VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Storage Condition (Climatic Chamber / Condition definition)
CREATE TABLE IF NOT EXISTS stability_storage_condition (
    id BIGSERIAL PRIMARY KEY,
    study_id BIGINT NOT NULL REFERENCES stability_study(id) ON DELETE CASCADE,
    chamber_name VARCHAR(150) NOT NULL,
    temperature_celsius DOUBLE PRECISION NOT NULL,
    temperature_tolerance DOUBLE PRECISION DEFAULT 2.0,
    relative_humidity DOUBLE PRECISION,
    humidity_tolerance DOUBLE PRECISION DEFAULT 5.0,
    light_condition VARCHAR(100) DEFAULT 'DARK',
    shelf_location VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Time Points (Sampling milestones: 0M, 3M, 6M, 9M, 12M, 24M, 36M)
CREATE TABLE IF NOT EXISTS stability_time_point (
    id BIGSERIAL PRIMARY KEY,
    study_id BIGINT NOT NULL REFERENCES stability_study(id) ON DELETE CASCADE,
    point_label VARCHAR(50) NOT NULL,
    month_offset INT NOT NULL,
    tolerance_days INT DEFAULT 7,
    test_regime VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Sample Pull Events (Rút mẫu theo lịch & Kết quả theo dõi suy giảm hàm lượng)
CREATE TABLE IF NOT EXISTS stability_pull_event (
    id BIGSERIAL PRIMARY KEY,
    study_id BIGINT NOT NULL REFERENCES stability_study(id) ON DELETE CASCADE,
    time_point_id BIGINT NOT NULL REFERENCES stability_time_point(id) ON DELETE CASCADE,
    storage_condition_id BIGINT NOT NULL REFERENCES stability_storage_condition(id) ON DELETE CASCADE,
    scheduled_date DATE NOT NULL,
    window_start DATE,
    window_end DATE,
    pull_date TIMESTAMP WITH TIME ZONE,
    pulled_by VARCHAR(100),
    sample_id BIGINT REFERENCES sample_sample(id) ON DELETE SET NULL,
    test_request_id BIGINT REFERENCES sample_test_request(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, PENDING_PULL, PULLED, IN_TESTING, COMPLETED, CANCELLED
    assay_result DOUBLE PRECISION, -- % Assay for trendline
    dissolution_result DOUBLE PRECISION, -- % Dissolution
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_stb_study_code ON stability_study(study_code);
CREATE INDEX IF NOT EXISTS idx_stb_study_product ON stability_study(product_id);
CREATE INDEX IF NOT EXISTS idx_stb_pull_scheduled ON stability_pull_event(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_stb_pull_status ON stability_pull_event(status);
CREATE INDEX IF NOT EXISTS idx_inv_item_code ON inventory_inventory_item(code);
CREATE INDEX IF NOT EXISTS idx_inv_lot_num ON inventory_inventory_lot(lot_number);

-- ── 3. Add Permissions & Mappings ─────────────────────────────────────────────
INSERT INTO sys_permission (module, screen, action, description)
VALUES
    ('STABILITY', 'STUDY', 'READ', 'Xem danh sách và chi tiết đề cương thử nghiệm độ ổn định'),
    ('STABILITY', 'STUDY', 'CREATE', 'Tạo mới nghiên cứu độ ổn định thuốc theo ICH Q1A'),
    ('STABILITY', 'STUDY', 'UPDATE', 'Chỉnh sửa hoặc đóng nghiên cứu độ ổn định'),
    ('STABILITY', 'PULL', 'READ', 'Xem lịch rút mẫu và biểu đồ suy giảm hàm lượng'),
    ('STABILITY', 'PULL', 'EXECUTE', 'Thực hiện thao tác rút mẫu và tự động sinh phép thử'),
    ('INVENTORY', 'ITEM', 'READ', 'Tra cứu danh mục hóa chất, chuẩn đối chiếu, cột sắc ký'),
    ('INVENTORY', 'ITEM', 'CREATE', 'Thêm mới hóa chất, chuẩn vào danh mục kho'),
    ('INVENTORY', 'ITEM', 'UPDATE', 'Cập nhật thông tin định mức hóa chất kho'),
    ('INVENTORY', 'LOT', 'READ', 'Xem danh sách lô hóa chất và hạn dùng'),
    ('INVENTORY', 'LOT', 'CREATE', 'Nhập kho lô hóa chất, chuẩn mới'),
    ('INVENTORY', 'LOT', 'DEDUCT', 'Trừ tồn kho hóa chất khi sử dụng trong phép thử'),
    ('INVENTORY', 'PREP', 'CREATE', 'Lập biên bản pha chế dung dịch chuẩn và thuốc thử'),
    ('INVENTORY', 'PREP', 'READ', 'Xem nhật ký pha chế dung dịch')
ON CONFLICT (module, screen, action) DO NOTHING;

-- Map permissions to roles
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code IN ('IT_ADMIN', 'LAB_ADMIN')
  AND p.module IN ('STABILITY', 'INVENTORY')
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'SUPERVISOR'
  AND p.module IN ('STABILITY', 'INVENTORY')
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'OPERATOR'
  AND (
       (p.module = 'STABILITY' AND p.action IN ('READ', 'EXECUTE'))
    OR (p.module = 'INVENTORY' AND p.action IN ('READ', 'DEDUCT', 'CREATE'))
  )
ON CONFLICT DO NOTHING;

-- ── 4. Seed Data: Realistic Inventory Chemicals & Reference Standards ────────
INSERT INTO inventory_inventory_item (
    id, department_id, name, code, category, cas_number, grade,
    storage_condition, safety_hazard, unit, reorder_level, current_stock, status, created_at, updated_at
)
VALUES
(
    1, 3, 'Chất chuẩn đối chiếu Paracetamol (Paracetamol Reference Standard)',
    'REF-PARA-001', 'REFERENCE_STANDARD', '103-90-2', 'USP Reference Standard (99.9%)',
    '15 - 25°C (Nơi khô ráo, tránh ánh sáng)', 'NONE', 'mg', '1000', 5000.0, 'ACTIVE', NOW(), NOW()
),
(
    2, 3, 'Chất chuẩn đối chiếu 4-Aminophenol (Tạp chất A Paracetamol)',
    'REF-4AP-001', 'REFERENCE_STANDARD', '123-30-8', 'EP Reference Standard (99.8%)',
    '2 - 8°C (Bảo quản lạnh)', 'TOXIC', 'mg', '200', 1000.0, 'ACTIVE', NOW(), NOW()
),
(
    3, 3, 'Methanol dùng cho sắc ký HPLC (HPLC Grade)',
    'SOLV-MEOH-001', 'REAGENT_SOLVENT', '67-56-1', 'HPLC / Spectrophotometry Grade >= 99.9%',
    '15 - 25°C (Kho dung môi chống cháy nổ)', 'FLAMMABLE', 'mL', '10000', 45000.0, 'ACTIVE', NOW(), NOW()
),
(
    4, 3, 'Acetonitrile Gradient Grade dùng cho HPLC',
    'SOLV-ACN-001', 'REAGENT_SOLVENT', '75-05-8', 'HPLC Gradient Grade >= 99.9%',
    '15 - 25°C (Kho dung môi chống cháy nổ)', 'FLAMMABLE', 'mL', '8000', 32000.0, 'ACTIVE', NOW(), NOW()
),
(
    5, 3, 'Natri Hydroxyd tinh khiết phân tích (NaOH AR)',
    'CHEM-NAOH-001', 'CHEMICAL', '1310-73-2', 'Analytical Reagent (AR >= 98.0%)',
    '15 - 25°C (Bình kín, chống ẩm)', 'CORROSIVE', 'g', '2000', 8500.0, 'ACTIVE', NOW(), NOW()
),
(
    6, 3, 'Cột sắc ký Zorbax Eclipse Plus C18 (4.6 x 150 mm, 5 µm)',
    'COL-C18-001', 'COLUMN', 'N/A', 'Agilent LC Analytical Column',
    '15 - 25°C (Bảo quản trong Acetonitrile 100%)', 'NONE', 'cột', '2', 5.0, 'ACTIVE', NOW(), NOW()
)
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    cas_number = EXCLUDED.cas_number,
    grade = EXCLUDED.grade,
    storage_condition = EXCLUDED.storage_condition,
    safety_hazard = EXCLUDED.safety_hazard,
    current_stock = EXCLUDED.current_stock;

-- Seed Inventory Lots
INSERT INTO inventory_inventory_lot (
    id, inventory_item_id, lot_number, manufacturer, expiry_date,
    quantity_remaining, initial_quantity, unit, received_date, coa_available, status, created_at, updated_at
)
VALUES
(
    1, 1, 'USP-PARA-2026A', 'USP Standards Rockville USA', NOW() + INTERVAL '24 months',
    4850.0, 5000.0, 'mg', NOW() - INTERVAL '1 month', TRUE, 'AVAILABLE', NOW(), NOW()
),
(
    2, 2, 'EP-4AP-2025D', 'EDQM Strasbourg France', NOW() + INTERVAL '14 months',
    950.0, 1000.0, 'mg', NOW() - INTERVAL '2 months', TRUE, 'AVAILABLE', NOW(), NOW()
),
(
    3, 3, 'MEOH-MERCK-26011', 'Merck KGaA Darmstadt Germany', NOW() + INTERVAL '30 months',
    42500.0, 50000.0, 'mL', NOW() - INTERVAL '15 days', TRUE, 'AVAILABLE', NOW(), NOW()
),
(
    4, 4, 'ACN-SIGMA-25098', 'Sigma-Aldrich / Honeywell', NOW() + INTERVAL '18 months',
    30500.0, 35000.0, 'mL', NOW() - INTERVAL '20 days', TRUE, 'AVAILABLE', NOW(), NOW()
),
(
    5, 5, 'NAOH-XILONG-2603', 'Xilong Scientific Chemical', NOW() + INTERVAL '36 months',
    8200.0, 10000.0, 'g', NOW() - INTERVAL '1 month', TRUE, 'AVAILABLE', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Seed Preparation Record (Dung dịch pha sẵn)
INSERT INTO inventory_preparation_record (
    id, solution_name, result_lot_id, method_id, prepared_by,
    prepared_at, expiry_at, sop_reference, target_volume, unit, status, notes, created_at, updated_at
)
VALUES
(
    1, 'Pha động HPLC Paracetamol: Methanol - Nước (15:85 v/v)',
    NULL, 1, 'operator', NOW() - INTERVAL '1 day', NOW() + INTERVAL '6 days',
    'SOP-HPLC-001', 1000.0, 'mL', 'APPROVED',
    'Hút 150 mL Methanol HPLC trộn với 850 mL Nước cất siêu sạch Milli-Q, siêu âm khử bọt khí 15 phút, lọc qua màng Nylon 0.45 µm.',
    NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'
)
ON CONFLICT (id) DO NOTHING;

-- ── 5. Seed Stability Study (ICH Q1A Long-term & Accelerated) ─────────────────
INSERT INTO stability_study (
    id, study_code, study_title, product_id, batch_id, study_type,
    protocol_number, duration_months, start_date, status, created_by, notes, created_at, updated_at
)
VALUES
(
    1, 'STB-2026-PARA500-01',
    'Nghiên cứu độ ổn định dài hạn & lão hóa cấp tốc Viên nén Paracetamol 500mg (Lô 260101)',
    1, -- Paracetamol 500mg
    1, -- Batch 260101
    'LONG_TERM',
    'PROT-STB-PARA-001',
    24,
    CURRENT_DATE - INTERVAL '6 months',
    'ACTIVE',
    'supervisor',
    'Nghiên cứu độ ổn định theo hướng dẫn ICH Q1A (R2) và ASEAN Guideline. Đánh giá tính chất vật lý, độ hòa tan và hàm lượng hoạt chất.',
    NOW() - INTERVAL '6 months', NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Storage Conditions for Study 1
INSERT INTO stability_storage_condition (
    id, study_id, chamber_name, temperature_celsius, temperature_tolerance,
    relative_humidity, humidity_tolerance, light_condition, shelf_location, created_at, updated_at
)
VALUES
(
    1, 1, 'Tủ vi khí hậu Climatic Chamber #01 (Điều kiện thường ICH)',
    25.0, 2.0, 60.0, 5.0, 'DARK', 'Ngăn A - Kệ 2', NOW(), NOW()
),
(
    2, 1, 'Tủ vi khí hậu Climatic Chamber #02 (Lão hóa cấp tốc ICH)',
    40.0, 2.0, 75.0, 5.0, 'DARK', 'Ngăn B - Kệ 1', NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Time Points for Study 1 (0M, 3M, 6M, 9M, 12M, 24M)
INSERT INTO stability_time_point (id, study_id, point_label, month_offset, tolerance_days, test_regime, created_at, updated_at)
VALUES
    (1, 1, '0M - Khởi điểm ban đầu', 0, 0, 'Hàm lượng Paracetamol, Độ hòa tan, Tạp chất A, Hình thức cảm quan', NOW(), NOW()),
    (2, 1, '3M - 3 Tháng bảo quản', 3, 7, 'Hàm lượng Paracetamol, Độ hòa tan, Tạp chất A', NOW(), NOW()),
    (3, 1, '6M - 6 Tháng bảo quản', 6, 7, 'Hàm lượng Paracetamol, Độ hòa tan, Tạp chất A, Độ rã', NOW(), NOW()),
    (4, 1, '9M - 9 Tháng bảo quản', 9, 7, 'Hàm lượng Paracetamol, Độ hòa tan', NOW(), NOW()),
    (5, 1, '12M - 12 Tháng bảo quản', 12, 10, 'Hàm lượng Paracetamol, Độ hòa tan, Tạp chất A, Giới hạn vi sinh', NOW(), NOW()),
    (6, 1, '24M - 24 Tháng kết thúc', 24, 14, 'Kiểm nghiệm đầy đủ tất cả chỉ tiêu theo DĐVN V', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed Pull Events (0M COMPLETED, 3M COMPLETED, 6M PENDING/PULLED, 9M SCHEDULED...)
INSERT INTO stability_pull_event (
    id, study_id, time_point_id, storage_condition_id, scheduled_date,
    window_start, window_end, pull_date, pulled_by, sample_id, test_request_id,
    status, assay_result, dissolution_result, notes, created_at, updated_at
)
VALUES
-- Condition 1: 25°C / 60%RH (Long-term)
(
    1, 1, 1, 1, CURRENT_DATE - INTERVAL '6 months',
    CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE - INTERVAL '6 months',
    NOW() - INTERVAL '6 months', 'operator', 4, 4,
    'COMPLETED', 100.2, 94.5, 'Rút mẫu T0 hoàn tất, kết quả đạt tiêu chuẩn xuất xưởng.',
    NOW() - INTERVAL '6 months', NOW() - INTERVAL '6 months'
),
(
    2, 1, 2, 1, CURRENT_DATE - INTERVAL '3 months',
    CURRENT_DATE - INTERVAL '3 months' - INTERVAL '7 days', CURRENT_DATE - INTERVAL '3 months' + INTERVAL '7 days',
    NOW() - INTERVAL '3 months', 'operator', 4, 4,
    'COMPLETED', 99.8, 93.8, 'Rút mẫu mốc 3M. Mẫu bảo quản tốt, không biến màu.',
    NOW() - INTERVAL '3 months', NOW() - INTERVAL '3 months'
),
(
    3, 1, 3, 1, CURRENT_DATE,
    CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE + INTERVAL '7 days',
    NOW(), 'operator', 4, 4,
    'PULLED', 99.4, 93.2, 'Đã rút mẫu mốc 6M, gửi bộ phận phân tích kiểm tra hàm lượng.',
    NOW(), NOW()
),
(
    4, 1, 4, 1, CURRENT_DATE + INTERVAL '3 months',
    CURRENT_DATE + INTERVAL '3 months' - INTERVAL '7 days', CURRENT_DATE + INTERVAL '3 months' + INTERVAL '7 days',
    NULL, NULL, NULL, NULL,
    'SCHEDULED', NULL, NULL, 'Mốc rút mẫu định kỳ 9 tháng.',
    NOW(), NOW()
),
(
    5, 1, 5, 1, CURRENT_DATE + INTERVAL '6 months',
    CURRENT_DATE + INTERVAL '6 months' - INTERVAL '10 days', CURRENT_DATE + INTERVAL '6 months' + INTERVAL '10 days',
    NULL, NULL, NULL, NULL,
    'SCHEDULED', NULL, NULL, 'Mốc rút mẫu định kỳ 12 tháng (Hạn dùng tạm thời).',
    NOW(), NOW()
),

-- Condition 2: 40°C / 75%RH (Accelerated)
(
    6, 1, 1, 2, CURRENT_DATE - INTERVAL '6 months',
    CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE - INTERVAL '6 months',
    NOW() - INTERVAL '6 months', 'operator', 4, 4,
    'COMPLETED', 100.2, 94.5, 'Khởi điểm cấp tốc T0.',
    NOW() - INTERVAL '6 months', NOW() - INTERVAL '6 months'
),
(
    7, 1, 2, 2, CURRENT_DATE - INTERVAL '3 months',
    CURRENT_DATE - INTERVAL '3 months' - INTERVAL '7 days', CURRENT_DATE - INTERVAL '3 months' + INTERVAL '7 days',
    NOW() - INTERVAL '3 months', 'operator', 4, 4,
    'COMPLETED', 98.9, 91.5, 'Cấp tốc 3M: hàm lượng giảm nhẹ 1.3%, vẫn nằm trong giới hạn [95.0% - 105.0%].',
    NOW() - INTERVAL '3 months', NOW() - INTERVAL '3 months'
),
(
    8, 1, 3, 2, CURRENT_DATE,
    CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE + INTERVAL '7 days',
    NOW(), 'operator', 4, 4,
    'PULLED', 98.2, 89.8, 'Cấp tốc 6M (Mốc then chốt ICH): Đang tiến hành chạy sắc ký HPLC.',
    NOW(), NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Reset Sequences
SELECT setval('inventory_inventory_item_id_seq', (SELECT COALESCE(MAX(id), 1) FROM inventory_inventory_item));
SELECT setval('inventory_inventory_lot_id_seq', (SELECT COALESCE(MAX(id), 1) FROM inventory_inventory_lot));
SELECT setval('inventory_preparation_record_id_seq', (SELECT COALESCE(MAX(id), 1) FROM inventory_preparation_record));
SELECT setval('stability_study_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stability_study));
SELECT setval('stability_storage_condition_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stability_storage_condition));
SELECT setval('stability_time_point_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stability_time_point));
SELECT setval('stability_pull_event_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stability_pull_event));
