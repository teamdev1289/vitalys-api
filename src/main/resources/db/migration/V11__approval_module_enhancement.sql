-- =============================================================================
-- Flyway Migration V11: Multi-stage Approval Workflow, 21 CFR Part 11 Electronic Signatures, and COA Reports
-- =============================================================================

-- ── 1. Enhance approval_approval_step table ──────────────────────────────────
ALTER TABLE approval_approval_step 
    ADD COLUMN IF NOT EXISTS step_number INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS step_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS meaning VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_approval_step_entity ON approval_approval_step (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_approval_step_status ON approval_approval_step (status);

-- ── 2. Enhance approval_report table ─────────────────────────────────────────
ALTER TABLE approval_report
    ADD COLUMN IF NOT EXISTS report_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS conclusion VARCHAR(255),
    ADD COLUMN IF NOT EXISTS qr_code_data TEXT,
    ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_approval_report_code ON approval_report (report_code);
CREATE INDEX IF NOT EXISTS idx_approval_report_sample_id ON approval_report (sample_id);

-- ── 3. Add Permissions for Approval & Reporting ──────────────────────────────
INSERT INTO sys_permission (module, screen, action, description)
VALUES
    ('APPROVAL', 'WORKFLOW', 'READ', 'Xem quy trình phê duyệt nhiều cấp'),
    ('APPROVAL', 'SIGN', 'EXECUTE', 'Ký duyệt điện tử 21 CFR Part 11'),
    ('APPROVAL', 'REPORT', 'READ', 'Xem và tải Phiếu kiểm nghiệm COA'),
    ('APPROVAL', 'REPORT', 'GENERATE', 'Kết xuất Phiếu kiểm nghiệm COA PDF chính thức')
ON CONFLICT (module, screen, action) DO NOTHING;

-- Grant permissions to IT_ADMIN
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'IT_ADMIN'
  AND p.module = 'APPROVAL'
ON CONFLICT DO NOTHING;

-- Grant permissions to MANAGER and LAB_ADMIN
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code IN ('MANAGER', 'LAB_ADMIN')
  AND p.module = 'APPROVAL'
ON CONFLICT DO NOTHING;

-- Grant permissions to SUPERVISOR
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'SUPERVISOR'
  AND p.module = 'APPROVAL'
  AND p.action IN ('READ', 'EXECUTE', 'GENERATE')
ON CONFLICT DO NOTHING;

-- Grant permissions to OPERATOR (Analyst Authoring & Reading)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.code = 'OPERATOR'
  AND p.module = 'APPROVAL'
  AND p.action IN ('READ', 'EXECUTE')
ON CONFLICT DO NOTHING;

-- ── 4. Seed 3-Stage Approval Steps for Sample SMP-20260901-0001 (Paracetamol Tablets) ─────────
DO $$
DECLARE
    v_sample_id BIGINT;
BEGIN
    SELECT id INTO v_sample_id FROM sample_sample WHERE sample_code = 'SMP-20260901-0001';

    IF v_sample_id IS NOT NULL THEN
        -- Step 1: Analyst Verification (Author) - COMPLETED
        INSERT INTO approval_approval_step (
            entity_type, entity_id, step_number, step_order, step_name, required_role,
            status, actioned_by, actioned_at, meaning, comment, e_signature_hash, created_at, updated_at
        ) VALUES (
            'SAMPLE', v_sample_id, 1, '1', 'Kiểm nghiệm viên xác nhận kết quả (Analyst Verification)', 'OPERATOR',
            'APPROVED', 'operator', CURRENT_TIMESTAMP - INTERVAL '2 hours',
            'Tôi xác nhận đã hoàn tất thử nghiệm và các kết quả phân tích là trung thực, chính xác (Author)',
            'Hoàn tất phân tích chỉ tiêu Hàm lượng và Độ hòa tan theo đúng SOP-HPLC-001.',
            'a8f9c1b4e2d3e5f67890123456789abcdef0123456789abcdef0123456789abcd',
            CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'
        );

        -- Step 2: Supervisor Review (Reviewer) - PENDING
        INSERT INTO approval_approval_step (
            entity_type, entity_id, step_number, step_order, step_name, required_role,
            status, actioned_by, actioned_at, meaning, comment, e_signature_hash, created_at, updated_at
        ) VALUES (
            'SAMPLE', v_sample_id, 2, '2', 'Soát xét kỹ thuật phòng kiểm nghiệm (Supervisor Technical Review)', 'SUPERVISOR',
            'PENDING', NULL, NULL,
            'Tôi đã soát xét dữ liệu thô, nhật ký thiết bị và xác nhận quy trình tuân thủ GLP (Reviewer)',
            NULL, NULL,
            CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'
        );

        -- Step 3: QA Approver (Final Release) - PENDING
        INSERT INTO approval_approval_step (
            entity_type, entity_id, step_number, step_order, step_name, required_role,
            status, actioned_by, actioned_at, meaning, comment, e_signature_hash, created_at, updated_at
        ) VALUES (
            'SAMPLE', v_sample_id, 3, '3', 'Phê duyệt xuất xưởng & Ban hành COA (QA Head Approval)', 'LAB_ADMIN',
            'PENDING', NULL, NULL,
            'Tôi phê duyệt phát hành Phiếu kiểm nghiệm (COA) và giải phóng lô hàng (Approver)',
            NULL, NULL,
            CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'
        );

        -- Seed initial draft Report
        INSERT INTO approval_report (
            sample_id, report_code, version, status, conclusion, generated_by, notes, created_at, updated_at
        ) VALUES (
            v_sample_id, 'COA-20260901-0001', '1.0', 'DRAFT',
            'Mẫu thử đạt yêu cầu theo Tiêu chuẩn cơ sở (Dược điển Việt Nam V)',
            'operator', 'Bản dự thảo Phiếu kiểm nghiệm đang chờ soát xét 3 cấp.',
            CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'
        );
    END IF;
END $$;

