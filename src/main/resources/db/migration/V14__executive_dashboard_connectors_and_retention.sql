-- V14__executive_dashboard_connectors_and_retention.sql
-- Executive Dashboards, ERP Connectors & 21 CFR Part 11 Data Retention / Legal Hold

-- ── 1. Inbound ERP / SAP Order Intake Table ─────────────────────────────────
CREATE TABLE IF NOT EXISTS integration_inbound_order (
    id BIGSERIAL PRIMARY KEY,
    external_order_id VARCHAR(100) NOT NULL,
    source_system VARCHAR(50) NOT NULL DEFAULT 'SAP_ERP',
    batch_number VARCHAR(100),
    product_code VARCHAR(50),
    order_type VARCHAR(50) NOT NULL DEFAULT 'RELEASE',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    requested_tests TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    sample_id BIGINT,
    test_request_id BIGINT,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inbound_order_status ON integration_inbound_order(status);
CREATE INDEX IF NOT EXISTS idx_inbound_order_ext_id ON integration_inbound_order(external_order_id);

-- ── 2. Outbound Webhook Delivery Table ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS integration_webhook_outbound (
    id BIGSERIAL PRIMARY KEY,
    target_system VARCHAR(50) NOT NULL DEFAULT 'SAP_ERP',
    webhook_url VARCHAR(500) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    response_code INT DEFAULT 200,
    response_body TEXT,
    retry_count INT DEFAULT 0,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_webhook_event ON integration_webhook_outbound(event_type);
CREATE INDEX IF NOT EXISTS idx_webhook_status ON integration_webhook_outbound(delivery_status);

-- ── 3. Data Retention Policy Table ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS retention_policy (
    id BIGSERIAL PRIMARY KEY,
    module_name VARCHAR(50) UNIQUE NOT NULL,
    category_title VARCHAR(150) NOT NULL,
    retention_years INT NOT NULL,
    is_permanent BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(255),
    auto_archive BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ── 4. Legal Hold Record Table (21 CFR Part 11 FDA Inspection Lock) ─────────
CREATE TABLE IF NOT EXISTS legal_hold_record (
    id BIGSERIAL PRIMARY KEY,
    hold_code VARCHAR(50) UNIQUE NOT NULL,
    case_title VARCHAR(255) NOT NULL,
    case_reference VARCHAR(100) NOT NULL,
    target_module VARCHAR(50) NOT NULL,
    target_entity_id BIGINT NOT NULL,
    entity_code VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    placed_by VARCHAR(100) NOT NULL,
    placed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_by VARCHAR(100),
    released_at TIMESTAMPTZ,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_legal_hold_target ON legal_hold_record(target_module, target_entity_id);
CREATE INDEX IF NOT EXISTS idx_legal_hold_status ON legal_hold_record(status);

-- ── 5. System Permissions for Phase 8 ───────────────────────────────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('DASHBOARD', 'EXECUTIVE', 'READ', 'Xem dashboard điều hành toàn diện phòng lab'),
    ('INTEGRATION', 'ERP', 'READ', 'Xem danh sách lệnh kiểm nghiệm từ ERP/SAP'),
    ('INTEGRATION', 'ERP', 'SYNC', 'Đồng bộ kết nối ERP inbound & webhook outbound'),
    ('RETENTION', 'POLICY', 'READ', 'Xem chính sách lưu trữ dữ liệu điện tử'),
    ('RETENTION', 'POLICY', 'UPDATE', 'Cập nhật thời hạn lưu trữ dữ liệu phòng lab'),
    ('RETENTION', 'HOLD', 'MANAGE', 'Thiết lập hoặc gỡ cờ Legal Hold phục vụ thanh tra FDA')
ON CONFLICT DO NOTHING;

-- Map permissions to IT_ADMIN, LAB_ADMIN, SUPERVISOR
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code IN ('IT_ADMIN', 'LAB_ADMIN')
  AND p.module IN ('DASHBOARD', 'INTEGRATION', 'RETENTION')
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'SUPERVISOR'
  AND p.module IN ('DASHBOARD', 'INTEGRATION')
ON CONFLICT DO NOTHING;

-- ── 6. Seed Realistic Retention Policies ────────────────────────────────────
INSERT INTO retention_policy (module_name, category_title, retention_years, is_permanent, description, auto_archive) VALUES
    ('AUDIT_TRAIL', 'Hồ sơ Nhật ký Kiểm toán ALCOA+ (sys_audit_trail)', 99, TRUE, 'Lưu trữ vĩnh viễn theo 21 CFR Part 11, tuyệt đối không được xóa', FALSE),
    ('SDMS_RAW_FILES', 'Tệp Dữ Liệu Sắc Ký / Quang Phổ Thô (sdms_data_file)', 10, FALSE, 'Lưu trữ tối thiểu 10 năm sau khi hết hạn lô sản phẩm', TRUE),
    ('TESTING_RESULTS', 'Kết Quả & Phiếu Kiểm Nghiệm Hóa Lý (testing_test)', 7, FALSE, 'Lưu trữ 7 năm theo quy định Dược Điển Việt Nam và GMP-WHO', TRUE),
    ('STABILITY_STUDIES', 'Hồ Sơ Nghiên Cứu Độ Ổn Định Dài Hạn (stability_study)', 15, FALSE, 'Lưu trữ tối thiểu 15 năm phục vụ thẩm định hồ sơ đăng ký thuốc', TRUE),
    ('COA_REPORTS', 'Phiếu Kiểm Nghiệm COA & Chữ Ký Điện Tử (report)', 10, FALSE, 'Lưu trữ bản ký số điện tử 10 năm phục vụ truy xuất nguồn gốc', TRUE)
ON CONFLICT (module_name) DO NOTHING;

-- ── 7. Seed Active Legal Hold Record (FDA Audit Simulation) ──────────────────
INSERT INTO legal_hold_record (hold_code, case_title, case_reference, target_module, target_entity_id, entity_code, status, placed_by, reason) VALUES
    ('LH-2026-FDA-001', 'Thanh tra Định kỳ FDA CGMP - Kiểm tra hồ sơ lô viên nén Paracetamol 500mg', 'FDA-483-2026-INSP', 'BATCH', 1, 'BATCH-260101', 'ACTIVE', 'labadmin', 'Khóa bảo vệ toàn diện dữ liệu: cấm xóa/hủy mẫu, kết quả phân tích, tệp SDMS và hồ sơ độ ổn định liên quan đến lô 260101 cho đến khi kết thúc thanh tra FDA.')
ON CONFLICT (hold_code) DO NOTHING;

-- ── 8. Seed Realistic Inbound Orders from ERP / SAP ─────────────────────────
INSERT INTO integration_inbound_order (external_order_id, source_system, batch_number, product_code, order_type, priority, requested_tests, status, sample_id, test_request_id, notes) VALUES
    ('SAP-QM-20260928-01', 'SAP_S4HANA', 'BATCH-260101', 'PRD-PARA-500', 'RELEASE', 'HIGH', 'Hàm lượng Paracetamol HPLC, Độ hòa tan, Tạp chất A', 'COMPLETED', 1, 4, 'Lệnh xuất xưởng thành phẩm từ SAP QM module - Đã hoàn thành kiểm nghiệm'),
    ('SAP-QM-20260928-02', 'SAP_S4HANA', 'BATCH-260102', 'PRD-PARA-500', 'RELEASE', 'NORMAL', 'Định tính, Hàm lượng, Độ rã, Độ đồng đều khối lượng', 'PENDING', NULL, NULL, 'Lệnh kiểm nghiệm mới tiếp nhận từ dây chuyền đóng gói số 2'),
    ('MES-ORD-20260928-03', 'WERUM_PAS_X', 'BATCH-260103', 'PRD-PARA-500', 'IN_PROCESS', 'HIGH', 'Kiểm soát bán thành phẩm trong quá trình dập viên', 'PENDING', NULL, NULL, 'Lệnh kiểm nghiệm IPC (In-Process Control) bán thành phẩm cốm dập')
ON CONFLICT DO NOTHING;

-- ── 9. Seed Outbound Webhook Delivery History ───────────────────────────────
INSERT INTO integration_webhook_outbound (target_system, webhook_url, event_type, payload, delivery_status, response_code, response_body) VALUES
    ('SAP_S4HANA', 'https://erp.vitalys-pharma.com/api/v1/qm/inspection-lot/results', 'SAMPLE_RELEASED',
     '{"externalOrderId":"SAP-QM-20260928-01","batchNumber":"BATCH-260101","productCode":"PRD-PARA-500","decision":"RELEASED","coaNumber":"COA-2026-0001","assayResult":100.2,"releasedBy":"supervisor","releasedAt":"2026-09-28T10:00:00Z"}'::jsonb,
     'SUCCESS', 200, '{"status":"SUCCESS","sapInspectionLot":"10000054231","updated":true}'),
    ('WERUM_MES', 'https://mes.vitalys-pharma.com/webhook/lims/ipc-decision', 'SAMPLE_RELEASED',
     '{"batchNumber":"BATCH-260101","ipcStep":"TABLET_COMPRESSION","status":"PASS"}'::jsonb,
     'SUCCESS', 200, '{"ack":true}');
