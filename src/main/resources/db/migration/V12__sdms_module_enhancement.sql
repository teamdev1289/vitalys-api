-- =============================================================================
-- Migration V12: SDMS (Scientific Data Management System) & InSpector Viewer
-- Vitalys LIMS / SDMS / ELN Platform - Phase 6
-- =============================================================================

-- 1. Create Scientific Data File Catalog
CREATE TABLE IF NOT EXISTS sdms_data_file (
    id BIGSERIAL PRIMARY KEY,
    file_code VARCHAR(100) UNIQUE NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL, -- CHROMATOGRAM_HPLC, CHROMATOGRAM_GC, SPECTRUM_UV_VIS, SPECTRUM_FTIR, RAW_EXPORT
    file_size BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64) NOT NULL, -- 21 CFR Part 11 tamper-evident raw file checksum
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'CAPTURED', -- CAPTURED, INDEXED, ARCHIVED, LOCKED
    instrument_id BIGINT REFERENCES equipment_instrument(id) ON DELETE SET NULL,
    run_id BIGINT REFERENCES testing_analytical_run(id) ON DELETE SET NULL,
    sample_id BIGINT REFERENCES sample_sample(id) ON DELETE SET NULL,
    test_id BIGINT REFERENCES testing_test(id) ON DELETE SET NULL,
    uploaded_by VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Create SDMS Metadata Key-Value Table
CREATE TABLE IF NOT EXISTS sdms_data_file_metadata (
    id BIGSERIAL PRIMARY KEY,
    data_file_id BIGINT NOT NULL REFERENCES sdms_data_file(id) ON DELETE CASCADE,
    meta_key VARCHAR(100) NOT NULL,
    meta_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_sdms_metadata UNIQUE (data_file_id, meta_key)
);

-- 3. Create Chromatogram / Spectrum Peak Integration Table
CREATE TABLE IF NOT EXISTS sdms_chromatogram_peak (
    id BIGSERIAL PRIMARY KEY,
    data_file_id BIGINT NOT NULL REFERENCES sdms_data_file(id) ON DELETE CASCADE,
    peak_number INT NOT NULL,
    compound_name VARCHAR(150),
    retention_time DOUBLE PRECISION NOT NULL, -- Retention time (tR) in minutes or Wavelength in nm
    peak_area DOUBLE PRECISION NOT NULL,
    peak_height DOUBLE PRECISION NOT NULL,
    area_percent DOUBLE PRECISION,
    theoretical_plates INT,
    tailing_factor DOUBLE PRECISION,
    resolution DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Create Indexes for High-Throughput Search
CREATE INDEX IF NOT EXISTS idx_sdms_file_code ON sdms_data_file(file_code);
CREATE INDEX IF NOT EXISTS idx_sdms_file_type ON sdms_data_file(file_type);
CREATE INDEX IF NOT EXISTS idx_sdms_instrument_id ON sdms_data_file(instrument_id);
CREATE INDEX IF NOT EXISTS idx_sdms_sample_id ON sdms_data_file(sample_id);
CREATE INDEX IF NOT EXISTS idx_sdms_test_id ON sdms_data_file(test_id);
CREATE INDEX IF NOT EXISTS idx_sdms_checksum ON sdms_data_file(checksum_sha256);
CREATE INDEX IF NOT EXISTS idx_sdms_meta_file ON sdms_data_file_metadata(data_file_id);
CREATE INDEX IF NOT EXISTS idx_sdms_peak_file ON sdms_chromatogram_peak(data_file_id);

-- 5. Add SDMS Permissions
INSERT INTO sys_permission (module, screen, action, description)
VALUES
    ('SDMS', 'DATA', 'READ', 'Xem và tra cứu dữ liệu khoa học SDMS & sắc ký đồ'),
    ('SDMS', 'DATA', 'UPLOAD', 'Tải lên dữ liệu thô từ máy phân tích (HPLC, GC, UV-Vis)'),
    ('SDMS', 'DATA', 'DELETE', 'Xóa hoặc hủy lưu trữ file dữ liệu SDMS'),
    ('SDMS', 'LINK', 'ATTACH', 'Liên kết dữ liệu sắc ký đồ với phép thử và mẫu kiểm nghiệm')
ON CONFLICT (module, screen, action) DO NOTHING;

-- Map SDMS permissions to IT_ADMIN, LAB_ADMIN, SUPERVISOR, OPERATOR
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code IN ('IT_ADMIN', 'LAB_ADMIN')
  AND p.module = 'SDMS'
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'SUPERVISOR'
  AND p.module = 'SDMS'
  AND p.action IN ('READ', 'UPLOAD', 'ATTACH')
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'OPERATOR'
  AND p.module = 'SDMS'
  AND p.action IN ('READ', 'UPLOAD', 'ATTACH')
ON CONFLICT DO NOTHING;

-- 6. Seed Realistic Scientific Data Files (HPLC Chromatogram & UV-Vis Spectrum)
INSERT INTO sdms_data_file (
    id, file_code, original_filename, file_type, file_size, checksum_sha256,
    storage_path, mime_type, status, instrument_id, run_id, sample_id, test_id,
    uploaded_by, notes, created_at, updated_at
)
VALUES
(
    1,
    'SDMS-20260901-HPLC-001',
    'Paracetamol_Assay_Batch260101_Run01.csv',
    'CHROMATOGRAM_HPLC',
    1048576,
    '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
    '/data/sdms/hplc/2026/09/Paracetamol_Assay_Batch260101_Run01.csv',
    'text/csv',
    'INDEXED',
    1, -- HPLC Agilent 1260
    1, -- Analytical Run 1
    4, -- Sample SMP-20260901-0001
    1, -- Test 1 (Hàm lượng Paracetamol)
    'operator',
    'Dữ liệu sắc ký đồ HPLC phân tích định lượng Paracetamol theo DĐVN V. Đỉnh pic sắc nét, độ phân giải cao.',
    NOW() - INTERVAL '4 hours',
    NOW() - INTERVAL '4 hours'
),
(
    2,
    'SDMS-20260901-UV-001',
    'Paracetamol_Identification_UV_Spectrum.csv',
    'SPECTRUM_UV_VIS',
    524288,
    '8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4',
    '/data/sdms/spectroscopy/2026/09/Paracetamol_Identification_UV_Spectrum.csv',
    'text/csv',
    'INDEXED',
    2, -- UV-Vis Shimadzu
    1,
    4,
    2,
    'operator',
    'Phổ hấp thụ tử ngoại UV-Vis quét từ 200 - 400 nm. Đỉnh hấp thụ cực đại tại 257 nm phù hợp chuẩn đối chiếu.',
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '3 hours'
)
ON CONFLICT (file_code) DO NOTHING;

-- Seed Metadata for File 1 (HPLC)
INSERT INTO sdms_data_file_metadata (data_file_id, meta_key, meta_value, created_at, updated_at)
VALUES
    (1, 'INSTRUMENT_MODEL', 'Agilent 1260 Infinity II Quaternary LC', NOW(), NOW()),
    (1, 'DETECTOR', 'Variable Wavelength Detector (VWD) @ 243 nm', NOW(), NOW()),
    (1, 'COLUMN', 'Zorbax Eclipse Plus C18 (4.6 x 150 mm, 5 µm)', NOW(), NOW()),
    (1, 'FLOW_RATE', '1.0 mL/min', NOW(), NOW()),
    (1, 'MOBILE_PHASE', 'Methanol : Nước (15 : 85 v/v)', NOW(), NOW()),
    (1, 'INJECTION_VOLUME', '10.0 µL', NOW(), NOW()),
    (1, 'COLUMN_TEMPERATURE', '30.0 °C', NOW(), NOW()),
    (1, 'ACQUISITION_DATE', '2026-09-01T10:15:00Z', NOW(), NOW()),
    (1, 'SOFTWARE_VERSION', 'OpenLab CDS ChemStation v2.6', NOW(), NOW())
ON CONFLICT (data_file_id, meta_key) DO NOTHING;

-- Seed Metadata for File 2 (UV-Vis)
INSERT INTO sdms_data_file_metadata (data_file_id, meta_key, meta_value, created_at, updated_at)
VALUES
    (2, 'INSTRUMENT_MODEL', 'Shimadzu UV-1900i Double-Beam Spectrophotometer', NOW(), NOW()),
    (2, 'SCAN_RANGE', '200 nm - 400 nm', NOW(), NOW()),
    (2, 'SCAN_SPEED', 'Fast (1000 nm/min)', NOW(), NOW()),
    (2, 'SOLVENT', 'Dung dịch Natri Hydroxyd 0.1 M', NOW(), NOW()),
    (2, 'LAMBDA_MAX', '257.0 nm (A = 0.715)', NOW(), NOW()),
    (2, 'SOFTWARE_VERSION', 'UVProbe v2.70', NOW(), NOW())
ON CONFLICT (data_file_id, meta_key) DO NOTHING;

-- Seed Peaks for File 1 (HPLC Peaks)
INSERT INTO sdms_chromatogram_peak (
    data_file_id, peak_number, compound_name, retention_time,
    peak_area, peak_height, area_percent, theoretical_plates,
    tailing_factor, resolution, created_at, updated_at
)
VALUES
    (1, 1, '4-Aminophenol (Tạp chất A)', 1.85, 1850.5, 195.4, 0.12, 3850, 1.12, 0.0, NOW(), NOW()),
    (1, 2, 'Paracetamol (Hoạt chất chính)', 3.42, 1542380.0, 98450.0, 99.88, 7240, 1.04, 4.85, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Reset sequence
SELECT setval('sdms_data_file_id_seq', (SELECT MAX(id) FROM sdms_data_file));
