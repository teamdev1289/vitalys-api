-- =============================================================================
-- Flyway Migration V8: Vitalys - Method, Specification & Dynamic Form Enhancements
-- =============================================================================

-- ── 1. Enhance method_method table ───────────────────────────────────────────
ALTER TABLE method_method
    ALTER COLUMN body_template TYPE TEXT,
    ADD COLUMN IF NOT EXISTS method_code VARCHAR(100) UNIQUE,
    ADD COLUMN IF NOT EXISTS category VARCHAR(100) DEFAULT 'ASSAY',
    ADD COLUMN IF NOT EXISTS instrument_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS effective_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS review_due_date TIMESTAMP WITH TIME ZONE;

-- ── 2. Enhance method_method_step_item table ─────────────────────────────────
ALTER TABLE method_method_step_item
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS instruction_notes TEXT;

-- ── 3. Enhance method_method_validation_protocol table ────────────────────────
ALTER TABLE method_method_validation_protocol
    ALTER COLUMN trigger_reason TYPE TEXT,
    ADD COLUMN IF NOT EXISTS title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS conclusion TEXT;

-- ── 4. Enhance method_specification_set table ────────────────────────────────
ALTER TABLE method_specification_set
    ALTER COLUMN change_reason TYPE TEXT,
    ADD COLUMN IF NOT EXISTS spec_code VARCHAR(100) UNIQUE,
    ADD COLUMN IF NOT EXISTS name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS product_id BIGINT,
    ADD COLUMN IF NOT EXISTS market VARCHAR(50) DEFAULT 'GLOBAL',
    ADD COLUMN IF NOT EXISTS expiry_date TIMESTAMP WITH TIME ZONE;

-- ── 5. Enhance method_specification_item table ───────────────────────────────
ALTER TABLE method_specification_item
    ADD COLUMN IF NOT EXISTS parameter_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS comparison_operator VARCHAR(50) DEFAULT 'BETWEEN',
    ADD COLUMN IF NOT EXISTS text_acceptance_criteria TEXT;

-- ── 6. Enhance method_form_template table ────────────────────────────────────
ALTER TABLE method_form_template
    ADD COLUMN IF NOT EXISTS title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS description TEXT;

-- ── 7. Enhance method_form_field table ───────────────────────────────────────
ALTER TABLE method_form_field
    ADD COLUMN IF NOT EXISTS options_json TEXT,
    ADD COLUMN IF NOT EXISTS default_value VARCHAR(255),
    ADD COLUMN IF NOT EXISTS unit VARCHAR(50),
    ADD COLUMN IF NOT EXISTS formula_expression VARCHAR(500),
    ADD COLUMN IF NOT EXISTS validation_rules TEXT;

-- ── 8. Add Method & Specification Permissions to sys_permission ───────────────
INSERT INTO sys_permission (module, screen, action, description) VALUES
    ('METHOD', 'SOP',      'READ',     'View standard operating procedures and testing methods'),
    ('METHOD', 'SOP',      'CREATE',   'Author new testing method SOP and step definitions'),
    ('METHOD', 'SOP',      'UPDATE',   'Revise and update method parameters and SOP body'),
    ('METHOD', 'SOP',      'VALIDATE', 'Approve, release or deprecate testing method version'),
    ('METHOD', 'SOP',      'DELETE',   'Archive or delete testing method drafts'),
    ('METHOD', 'SPEC',     'READ',     'View product release and stability specifications'),
    ('METHOD', 'SPEC',     'CREATE',   'Define acceptance criteria and specification sets'),
    ('METHOD', 'SPEC',     'UPDATE',   'Modify specification set limits and market requirements'),
    ('METHOD', 'SPEC',     'DELETE',   'Archive specification sets'),
    ('METHOD', 'FORM',     'READ',     'View dynamic experiment form templates'),
    ('METHOD', 'FORM',     'CREATE',   'Design and build dynamic test calculation forms'),
    ('METHOD', 'FORM',     'UPDATE',   'Configure form fields, bindings and formulas')
ON CONFLICT (module, screen, action) DO NOTHING;

-- ── 9. Assign permissions to roles ───────────────────────────────────────────
-- IT_ADMIN gets all METHOD permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'IT_ADMIN'
  AND  p.module = 'METHOD'
ON CONFLICT DO NOTHING;

-- MANAGER gets full METHOD permissions
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'MANAGER'
  AND  p.module = 'METHOD'
ON CONFLICT DO NOTHING;

-- LAB_ADMIN & SUPERVISOR get READ, CREATE, UPDATE, VALIDATE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code IN ('LAB_ADMIN', 'SUPERVISOR')
  AND  p.module = 'METHOD'
  AND  p.action IN ('READ', 'CREATE', 'UPDATE', 'VALIDATE')
ON CONFLICT DO NOTHING;

-- OPERATOR gets READ
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'OPERATOR'
  AND  p.module = 'METHOD'
  AND  p.action = 'READ'
ON CONFLICT DO NOTHING;

-- GUEST gets READ
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   sys_role r, sys_permission p
WHERE  r.code = 'GUEST'
  AND  p.module = 'METHOD'
  AND  p.action = 'READ'
ON CONFLICT DO NOTHING;

-- ── 10. Seed Sample SOP Testing Methods ────────────────────────────────────────
INSERT INTO method_method (method_code, name, version, category, instrument_type, source_standard, validation_status, is_active, description, body_template)
VALUES
    ('SOP-HPLC-001', 'Xác định hàm lượng Paracetamol bằng phương pháp HPLC', 'v2.1', 'ASSAY', 'HPLC', 'USP 44-NF 39 / Dược điển Việt Nam V', 'VALIDATED', true,
     'Quy trình chuẩn định lượng hoạt chất Paracetamol trong chế phẩm viên nén bằng sắc ký lỏng hiệu năng cao pha đảo (RP-HPLC) với đầu dò UV/PDA tại bước sóng 243 nm.',
     '# SOP-HPLC-001: ĐỊNH LƯỢNG PARACETAMOL\n\n1. Điều kiện sắc ký:\n- Cột: C18 (250 x 4.6 mm, 5 µm)\n- Pha động: Nước : Methanol (3:1 v/v)\n- Tốc độ dòng: 1.0 mL/phút\n- Bước sóng phát hiện: 243 nm\n- Thể tích tiêm: 10 µL\n- Nhiệt độ cột: 30°C\n\n2. Pha dung dịch chuẩn:\n- Cân chính xác khoảng 50 mg Paracetamol chuẩn...\n\n3. Pha dung dịch thử:\n- Cân 20 viên, tính khối lượng trung bình...'),

    ('SOP-DIS-001', 'Thử độ hòa tan viên nén Paracetamol 500mg', 'v1.0', 'DISSOLUTION', 'DISSOLUTION_TESTER', 'USP <711> Apparatus 2', 'VALIDATED', true,
     'Quy trình kiểm tra độ giải phóng hoạt chất Paracetamol từ dạng thuốc rắn phân liều theo thời gian ở điều kiện mô phỏng dạ dày (pH 5.8 Phosphate buffer, 37 ± 0.5°C, 50 rpm, 45 phút).',
     '# SOP-DIS-001: THỬ ĐỘ HÒA TAN\n\n1. Thiết bị: Thiết bị thử độ hòa tan cánh khuấy (Apparatus 2)\n2. Môi trường: Đệm phosphat pH 5.8 (900 mL)\n3. Tốc độ quay: 50 rpm\n4. Thời gian: 45 phút\n5. Tiêu chuẩn chấp nhận: Q >= 80% sau 45 phút.'),

    ('SOP-UV-001', 'Định tính và độ đồng đều hàm lượng Amoxicillin 500mg', 'v1.2', 'IDENTIFICATION', 'UV_VIS', 'BP 2023 / Dược điển Việt Nam V', 'VALIDATED', true,
     'Phương pháp quang phổ hấp thụ tử ngoại (UV-Vis) xác định đỉnh hấp thụ cực đại ở 272 nm và đánh giá độ đồng đều khối lượng / hàm lượng của viên nang Amoxicillin.',
     '# SOP-UV-001: ĐỊNH TÍNH VÀ ĐỒNG ĐỀU HÀM LƯỢNG AMOXICILLIN\n\n1. Máy quang phổ UV-Vis quét phổ từ 200 - 400 nm.\n2. Cực đại hấp thụ tại bước sóng 272 ± 2 nm.\n3. Tiêu chuẩn: Phù hợp phổ chuẩn đối chiếu.')
ON CONFLICT (method_code) DO NOTHING;

-- ── 11. Seed Method Steps for SOP-HPLC-001 ──────────────────────────────────────
INSERT INTO method_method_step_item (method_id, step_key, label, role, expected_quantity, unit, order_index, description, instruction_notes)
SELECT m.id, 'STEP_01', 'Cân khối lượng trung bình 20 viên', 'ANALYST', 20.0, 'TABLETS', '1',
       'Cân chính xác khối lượng 20 viên nén trên cân phân tích 4 số lẻ và nghiền mịn thành bột đồng nhất.',
       'Sử dụng cân phân tích Mettler Toledo XPE205 đã hiệu chuẩn.'
FROM method_method m WHERE m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

INSERT INTO method_method_step_item (method_id, step_key, label, role, expected_quantity, unit, order_index, description, instruction_notes)
SELECT m.id, 'STEP_02', 'Cân lượng bột viên tương đương 50mg Paracetamol', 'ANALYST', 50.0, 'MG', '2',
       'Cân chính xác một lượng bột viên tương đương khoảng 50 mg Paracetamol vào bình định mức 100 mL.',
       'Ghi lại khối lượng cân thực tế vào form điện tử.'
FROM method_method m WHERE m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

INSERT INTO method_method_step_item (method_id, step_key, label, role, expected_quantity, unit, order_index, description, instruction_notes)
SELECT m.id, 'STEP_03', 'Hòa tan, siêu âm và định mức', 'ANALYST', 100.0, 'ML', '3',
       'Thêm 60 mL pha động, siêu âm 15 phút, để nguội về nhiệt độ phòng và thêm pha động đến vạch.',
       'Lọc qua màng lọc PTFE 0.45 µm trước khi nạp vào lọ vial HPLC.'
FROM method_method m WHERE m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

INSERT INTO method_method_step_item (method_id, step_key, label, role, expected_quantity, unit, order_index, description, instruction_notes)
SELECT m.id, 'STEP_04', 'Tiêm sắc ký và tính toán kết quả', 'ANALYST', 6.0, 'INJECTIONS', '4',
       'Tiêm 6 lần mẫu chuẩn (RSD diện tích pic <= 1.0%) sau đó tiêm lặp lại 2 lần mẫu thử.',
       'Hệ thống sắc ký phải đạt độ thích hợp hệ thống (SST) trước khi tiêm mẫu thử.'
FROM method_method m WHERE m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

-- ── 12. Seed Validation Protocol for SOP-HPLC-001 ──────────────────────────────
INSERT INTO method_method_validation_protocol (method_id, protocol_code, title, validation_type, trigger_reason, status, approved_by, approved_date, description, conclusion)
SELECT m.id, 'VAL-2026-HPLC-01', 'Thẩm định quy trình định lượng Paracetamol bằng HPLC', 'FULL_VALIDATION', 'Thẩm định ban đầu theo hướng dẫn ICH Q2(R1)', 'APPROVED', 'Trần Thị Thu Thảo', NOW(),
       'Đề cương thẩm định phương pháp phân tích kiểm tra các chỉ tiêu: Độ đặc hiệu, Tính tuyến tính (50% - 150%), Độ chính xác (Độ lặp lại & Độ chụm trung gian), Độ đúng (Tỷ lệ thu hồi 98-102%).',
       'Quy trình phân tích đạt tất cả các tiêu chí chấp nhận theo khuyến cáo của ICH Q2(R1) và Dược điển Việt Nam V. Phương pháp có giá trị sử dụng cho kiểm nghiệm thường quy.'
FROM method_method m WHERE m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

-- ── 13. Seed Specification Sets & Items ─────────────────────────────────────────
-- Spec Set for Paracetamol 500mg - Vietnam Market
INSERT INTO method_specification_set (spec_code, name, product_id, version, market, status, change_reason, effective_date)
SELECT 'SPEC-PARA-500-VN', 'Tiêu chuẩn xuất xưởng Viên nén Paracetamol 500mg (Dược điển VN V)', p.id, 'v1.0', 'VN', 'ACTIVE', 'Ban hành tiêu chuẩn cơ sở cập nhật DĐVN V', NOW()
FROM product_product p WHERE p.product_code = 'PRD-PARA-500'
ON CONFLICT (spec_code) DO NOTHING;

-- Spec Items for SPEC-PARA-500-VN
INSERT INTO method_specification_item (spec_set_id, method_id, analyte, parameter_name, min_limit, max_limit, unit, comparison_operator, text_acceptance_criteria)
SELECT s.id, m.id, 'Paracetamol', 'Hàm lượng hoạt chất (Assay)', 95.0, 105.0, '%', 'BETWEEN', 'Hàm lượng Paracetamol phải từ 95.0% đến 105.0% so với hàm lượng ghi trên nhãn.'
FROM method_specification_set s, method_method m
WHERE s.spec_code = 'SPEC-PARA-500-VN' AND m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

INSERT INTO method_specification_item (spec_set_id, method_id, analyte, parameter_name, min_limit, max_limit, unit, comparison_operator, text_acceptance_criteria)
SELECT s.id, m.id, 'Paracetamol', 'Độ hòa tan sau 45 phút (Dissolution)', 80.0, NULL, '%', 'GTE', 'Không được ít hơn 80% (Q) lượng Paracetamol so với lượng ghi trên nhãn được hòa tan sau 45 phút.'
FROM method_specification_set s, method_method m
WHERE s.spec_code = 'SPEC-PARA-500-VN' AND m.method_code = 'SOP-DIS-001'
ON CONFLICT DO NOTHING;

INSERT INTO method_specification_item (spec_set_id, method_id, analyte, parameter_name, min_limit, max_limit, unit, comparison_operator, text_acceptance_criteria)
SELECT s.id, NULL, 'Appearance', 'Tính chất / Cảm quan viên', NULL, NULL, NULL, 'TEXT_MATCH', 'Viên nén tròn màu trắng, hai mặt phẳng, cạnh và thành viên lành lặn, không sứt mẻ.'
FROM method_specification_set s
WHERE s.spec_code = 'SPEC-PARA-500-VN'
ON CONFLICT DO NOTHING;

INSERT INTO method_specification_item (spec_set_id, method_id, analyte, parameter_name, min_limit, max_limit, unit, comparison_operator, text_acceptance_criteria)
SELECT s.id, NULL, '4-Aminophenol', 'Tạp chất liên quan (4-Aminophenol)', NULL, 0.005, '%', 'LTE', 'Hàm lượng 4-Aminophenol không được vượt quá 0.005% (50 ppm).'
FROM method_specification_set s
WHERE s.spec_code = 'SPEC-PARA-500-VN'
ON CONFLICT DO NOTHING;

-- Spec Set for Paracetamol 500mg - US Market (USP 44)
INSERT INTO method_specification_set (spec_code, name, product_id, version, market, status, change_reason, effective_date)
SELECT 'SPEC-PARA-500-US', 'Release Specification Acetaminophen Tablets 500mg (USP 44)', p.id, 'v1.0', 'US', 'ACTIVE', 'Adopt USP 44 monograph standards for export batches', NOW()
FROM product_product p WHERE p.product_code = 'PRD-PARA-500'
ON CONFLICT (spec_code) DO NOTHING;

INSERT INTO method_specification_item (spec_set_id, method_id, analyte, parameter_name, min_limit, max_limit, unit, comparison_operator, text_acceptance_criteria)
SELECT s.id, m.id, 'Acetaminophen', 'Assay (Acetaminophen Content)', 98.0, 102.0, '%', 'BETWEEN', 'Contains not less than 98.0 percent and not more than 102.0 percent of acetaminophen.'
FROM method_specification_set s, method_method m
WHERE s.spec_code = 'SPEC-PARA-500-US' AND m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

-- ── 14. Seed Dynamic Form Template & Fields for SOP-HPLC-001 ───────────────────
INSERT INTO method_form_template (method_id, schema_name, title, version, status, description)
SELECT m.id, 'FORM_HPLC_ASSAY_V1', 'Phiếu ghi kết quả định lượng HPLC Paracetamol', 'v1.0', 'RELEASED',
       'Biểu mẫu điện tử tự động tính toán hàm lượng hoạt chất từ diện tích pic HPLC và khối lượng cân.'
FROM method_method m WHERE m.method_code = 'SOP-HPLC-001'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'w_20_tablets', 'Khối lượng 20 viên (g)', 'NUMBER', 'raw.w20', true, '1', 'g', '11.8500', NULL, '{"min": 10.0, "max": 15.0}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'w_sample', 'Khối lượng bột mẫu thử (mg)', 'NUMBER', 'raw.wsample', true, '2', 'mg', '59.25', NULL, '{"min": 45.0, "max": 75.0}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'w_std', 'Khối lượng chuẩn đối chiếu (mg)', 'NUMBER', 'raw.wstd', true, '3', 'mg', '50.10', NULL, '{"min": 45.0, "max": 55.0}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'std_purity', 'Hàm lượng chuẩn khai báo (%)', 'NUMBER', 'raw.purity', true, '4', '%', '99.8', NULL, '{"min": 98.0, "max": 101.0}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'area_std_avg', 'Diện tích pic chuẩn trung bình (A_std)', 'NUMBER', 'raw.astd', true, '5', 'mAU*s', '1425600', NULL, '{"min": 1000000}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'area_smp_avg', 'Diện tích pic mẫu thử trung bình (A_smp)', 'NUMBER', 'raw.asmp', true, '6', 'mAU*s', '1428900', NULL, '{"min": 1000000}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;

INSERT INTO method_form_field (template_id, field_key, label, field_type, data_binding, is_required, order_index, unit, default_value, formula_expression, validation_rules)
SELECT t.id, 'calculated_assay', 'Hàm lượng tính toán (%)', 'FORMULA', 'result.assay', false, '7', '%', NULL,
       '(area_smp_avg / area_std_avg) * (w_std / w_sample) * (w_20_tablets / 20 / 0.500) * (std_purity / 100) * 100',
       '{"min": 90.0, "max": 110.0}'
FROM method_form_template t WHERE t.schema_name = 'FORM_HPLC_ASSAY_V1'
ON CONFLICT DO NOTHING;
