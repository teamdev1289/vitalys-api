-- =============================================================================
-- Flyway Migration V4: LIMS Modules Schema (Tables & Foreign Keys)
-- =============================================================================

-- =============================================================================
-- 1. CREATE TABLES (Grouped by Module)
-- =============================================================================

-- ── Module: AUDIT ───────────────────────────────────────────────────────────

-- System notifications for users
CREATE TABLE audit_notification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    type VARCHAR(255),
    title VARCHAR(255),
    message VARCHAR(255),
    is_read BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: PRODUCT ───────────────────────────────────────────────────────────

-- Product definition
CREATE TABLE product_product (
    id BIGSERIAL PRIMARY KEY,
    registration_number VARCHAR(255),
    product_name VARCHAR(255),
    dosage_form VARCHAR(255),
    packaging_spec VARCHAR(255),
    shelf_life_months INTEGER,
    registrant VARCHAR(255),
    manufacturer VARCHAR(255),
    country_of_origin VARCHAR(255),
    quality_standard VARCHAR(255),
    product_category VARCHAR(255),
    classification VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Raw materials/active ingredients
CREATE TABLE product_ingredient (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    inn_name VARCHAR(255),
    cas_number VARCHAR(255),
    type VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Declared strength of product
CREATE TABLE product_product_active_ingredient (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    ingredient_id BIGINT,
    declared_strength VARCHAR(255),
    unit VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- R&D project
CREATE TABLE product_rd_project (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT,
    name VARCHAR(255),
    objective TEXT,
    start_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Formulation trial in R&D
CREATE TABLE product_formulation_trial (
    id BIGSERIAL PRIMARY KEY,
    rd_project_id BIGINT,
    product_id BIGINT,
    trial_code VARCHAR(255),
    version VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Components of a trial
CREATE TABLE product_formulation_trial_component (
    id BIGSERIAL PRIMARY KEY,
    trial_id BIGINT,
    ingredient_id BIGINT,
    role VARCHAR(255),
    quantity_per_unit DOUBLE PRECISION,
    unit VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Official formulation
CREATE TABLE product_formulation (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    source_trial_id BIGINT,
    formulation_code VARCHAR(255),
    version VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Components of official formulation
CREATE TABLE product_formulation_component (
    id BIGSERIAL PRIMARY KEY,
    formulation_id BIGINT,
    ingredient_id BIGINT,
    role VARCHAR(255),
    quantity_per_unit DOUBLE PRECISION,
    unit VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Registration dossier for circulation
CREATE TABLE product_registration_dossier (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    formulation_trial_id BIGINT,
    dossier_type VARCHAR(255),
    submitted_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(255),
    authority_ref_number VARCHAR(255),
    decision_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Production batch
CREATE TABLE product_batch (
    id BIGSERIAL PRIMARY KEY,
    formulation_id BIGINT,
    spec_set_id BIGINT,
    batch_number VARCHAR(255),
    manufacturing_date TIMESTAMP WITH TIME ZONE,
    expiry_date TIMESTAMP WITH TIME ZONE,
    quantity_produced DOUBLE PRECISION,
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: PARTNER ───────────────────────────────────────────────────────────

-- Customer details
CREATE TABLE partner_customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Project for customer
CREATE TABLE partner_project (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: SAMPLE ───────────────────────────────────────────────────────────

-- Request for testing
CREATE TABLE sample_test_request (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(255),
    sample_type VARCHAR(255),
    batch_id BIGINT,
    process_stage VARCHAR(255),
    formulation_trial_id BIGINT,
    customer_id BIGINT,
    project_id BIGINT,
    requested_by VARCHAR(255),
    request_date TIMESTAMP WITH TIME ZONE,
    due_date TIMESTAMP WITH TIME ZONE,
    priority VARCHAR(255),
    test_scope VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Sample instance
CREATE TABLE sample_sample (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT,
    department_id BIGINT,
    sample_code VARCHAR(255),
    status VARCHAR(255),
    received_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- State machine log for sample
CREATE TABLE sample_sample_status_history (
    id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT,
    from_status VARCHAR(255),
    to_status VARCHAR(255),
    changed_by VARCHAR(255),
    changed_at TIMESTAMP WITH TIME ZONE,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Attachments for sample
CREATE TABLE sample_sample_attachment (
    id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT,
    filename VARCHAR(255),
    file_url VARCHAR(255),
    mime_type VARCHAR(255),
    uploaded_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: METHOD ───────────────────────────────────────────────────────────

-- Testing method or SOP
CREATE TABLE method_method (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT,
    name VARCHAR(255),
    version VARCHAR(255),
    source_standard VARCHAR(255),
    validation_status VARCHAR(255),
    body_template VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Steps in a method
CREATE TABLE method_method_step_item (
    id BIGSERIAL PRIMARY KEY,
    method_id BIGINT,
    step_key VARCHAR(255),
    label VARCHAR(255),
    role VARCHAR(255),
    expected_item_id BIGINT,
    expected_quantity DOUBLE PRECISION,
    unit VARCHAR(255),
    order_index VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Method validation protocol
CREATE TABLE method_method_validation_protocol (
    id BIGSERIAL PRIMARY KEY,
    method_id BIGINT,
    protocol_code VARCHAR(255),
    validation_type VARCHAR(255),
    trigger_reason VARCHAR(255),
    status VARCHAR(255),
    approved_by VARCHAR(255),
    approved_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Result of validation parameter
CREATE TABLE method_validation_parameter_result (
    id BIGSERIAL PRIMARY KEY,
    protocol_id BIGINT,
    parameter_name VARCHAR(255),
    acceptance_criteria VARCHAR(255),
    actual_result VARCHAR(255),
    pass_fail VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Quality specification set
CREATE TABLE method_specification_set (
    id BIGSERIAL PRIMARY KEY,
    formulation_id BIGINT,
    version VARCHAR(255),
    effective_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(255),
    change_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Specific item in specification
CREATE TABLE method_specification_item (
    id BIGSERIAL PRIMARY KEY,
    spec_set_id BIGINT,
    method_id BIGINT,
    analyte VARCHAR(255),
    min_limit DOUBLE PRECISION,
    max_limit DOUBLE PRECISION,
    unit VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Dynamic form template
CREATE TABLE method_form_template (
    id BIGSERIAL PRIMARY KEY,
    method_id BIGINT,
    version VARCHAR(255),
    status VARCHAR(255),
    schema_name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Fields for dynamic form builder
CREATE TABLE method_form_field (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT,
    field_key VARCHAR(255),
    label VARCHAR(255),
    field_type VARCHAR(255),
    data_binding VARCHAR(255),
    is_required BOOLEAN,
    order_index VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: TESTING ───────────────────────────────────────────────────────────

-- Analytical run serving multiple tests
CREATE TABLE testing_analytical_run (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT,
    run_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Test performed on a sample
CREATE TABLE testing_test (
    id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT,
    method_id BIGINT,
    run_id BIGINT,
    form_template_id BIGINT,
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Actual step execution
CREATE TABLE testing_test_step_execution (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT,
    method_step_item_id BIGINT,
    inventory_lot_id BIGINT,
    instrument_id BIGINT,
    actual_quantity DOUBLE PRECISION,
    unit VARCHAR(255),
    performed_by VARCHAR(255),
    performed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Data submitted for form template
CREATE TABLE testing_form_submission (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT,
    submitted_by VARCHAR(255),
    submitted_at TIMESTAMP WITH TIME ZONE,
    data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Final result compared to spec
CREATE TABLE testing_result (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT,
    analyte VARCHAR(255),
    value DOUBLE PRECISION,
    unit VARCHAR(255),
    pass_fail VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Revision history for test result
CREATE TABLE testing_test_result_revision (
    id BIGSERIAL PRIMARY KEY,
    result_id BIGINT,
    old_value DOUBLE PRECISION,
    new_value DOUBLE PRECISION,
    revised_by VARCHAR(255),
    revised_at TIMESTAMP WITH TIME ZONE,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: APPROVAL ───────────────────────────────────────────────────────────

-- Generic approval workflow step
CREATE TABLE approval_approval_step (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255),
    entity_id BIGINT,
    step_order VARCHAR(255),
    required_role VARCHAR(255),
    status VARCHAR(255),
    actioned_by VARCHAR(255),
    actioned_at TIMESTAMP WITH TIME ZONE,
    comment TEXT,
    e_signature_hash VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Generated result report
CREATE TABLE approval_report (
    id BIGSERIAL PRIMARY KEY,
    sample_id BIGINT,
    version VARCHAR(255),
    status VARCHAR(255),
    file_path VARCHAR(255),
    generated_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: EQUIPMENT ───────────────────────────────────────────────────────────

-- Equipment / Instrument
CREATE TABLE equipment_instrument (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT,
    name VARCHAR(255),
    model VARCHAR(255),
    asset_code VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Calibration history
CREATE TABLE equipment_calibration_record (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT,
    performed_at TIMESTAMP WITH TIME ZONE,
    next_due VARCHAR(255),
    performed_by VARCHAR(255),
    certificate_url VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Maintenance history
CREATE TABLE equipment_maintenance_record (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT,
    performed_at TIMESTAMP WITH TIME ZONE,
    next_due VARCHAR(255),
    performed_by VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ── Module: INVENTORY ───────────────────────────────────────────────────────────

-- Inventory item catalog
CREATE TABLE inventory_inventory_item (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT,
    name VARCHAR(255),
    category VARCHAR(255),
    unit VARCHAR(255),
    reorder_level VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Specific lot of inventory item
CREATE TABLE inventory_inventory_lot (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT,
    lot_number VARCHAR(255),
    expiry_date TIMESTAMP WITH TIME ZONE,
    quantity_remaining DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Log of inventory usage
CREATE TABLE inventory_inventory_usage_log (
    id BIGSERIAL PRIMARY KEY,
    inventory_lot_id BIGINT,
    source_type VARCHAR(255),
    source_id BIGINT,
    quantity_used DOUBLE PRECISION,
    balance_after VARCHAR(255),
    used_by VARCHAR(255),
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Request for supply
CREATE TABLE inventory_supply_request (
    id BIGSERIAL PRIMARY KEY,
    requested_by VARCHAR(255),
    sample_id BIGINT,
    purpose VARCHAR(255),
    request_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Record of preparation
CREATE TABLE inventory_preparation_record (
    id BIGSERIAL PRIMARY KEY,
    result_lot_id BIGINT,
    method_id BIGINT,
    prepared_by VARCHAR(255),
    prepared_at TIMESTAMP WITH TIME ZONE,
    expiry_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Input lot for preparation
CREATE TABLE inventory_preparation_input (
    id BIGSERIAL PRIMARY KEY,
    preparation_id BIGINT,
    source_lot_id BIGINT,
    quantity_used DOUBLE PRECISION,
    unit VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =============================================================================
-- 2. FOREIGN KEY CONSTRAINTS (Grouped by Module)
-- =============================================================================

-- ── Module: AUDIT ───────────────────────────────────────────────────────────

ALTER TABLE audit_notification ADD CONSTRAINT fk_audit_notification_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id);

-- ── Module: PRODUCT ───────────────────────────────────────────────────────────

ALTER TABLE product_product_active_ingredient ADD CONSTRAINT fk_product_product_active_ingredient_product_id FOREIGN KEY (product_id) REFERENCES product_product(id);
ALTER TABLE product_product_active_ingredient ADD CONSTRAINT fk_product_product_active_ingredient_ingredient_id FOREIGN KEY (ingredient_id) REFERENCES product_ingredient(id);
ALTER TABLE product_rd_project ADD CONSTRAINT fk_product_rd_project_department_id FOREIGN KEY (department_id) REFERENCES sys_department(id);
ALTER TABLE product_formulation_trial ADD CONSTRAINT fk_product_formulation_trial_rd_project_id FOREIGN KEY (rd_project_id) REFERENCES product_rd_project(id);
ALTER TABLE product_formulation_trial ADD CONSTRAINT fk_product_formulation_trial_product_id FOREIGN KEY (product_id) REFERENCES product_product(id);
ALTER TABLE product_formulation_trial_component ADD CONSTRAINT fk_product_formulation_trial_component_trial_id FOREIGN KEY (trial_id) REFERENCES product_formulation_trial(id);
ALTER TABLE product_formulation_trial_component ADD CONSTRAINT fk_product_formulation_trial_component_ingredient_id FOREIGN KEY (ingredient_id) REFERENCES product_ingredient(id);
ALTER TABLE product_formulation ADD CONSTRAINT fk_product_formulation_product_id FOREIGN KEY (product_id) REFERENCES product_product(id);
ALTER TABLE product_formulation ADD CONSTRAINT fk_product_formulation_source_trial_id FOREIGN KEY (source_trial_id) REFERENCES product_formulation_trial(id);
ALTER TABLE product_formulation_component ADD CONSTRAINT fk_product_formulation_component_formulation_id FOREIGN KEY (formulation_id) REFERENCES product_formulation(id);
ALTER TABLE product_formulation_component ADD CONSTRAINT fk_product_formulation_component_ingredient_id FOREIGN KEY (ingredient_id) REFERENCES product_ingredient(id);
ALTER TABLE product_registration_dossier ADD CONSTRAINT fk_product_registration_dossier_product_id FOREIGN KEY (product_id) REFERENCES product_product(id);
ALTER TABLE product_registration_dossier ADD CONSTRAINT fk_product_registration_dossier_formulation_trial_id FOREIGN KEY (formulation_trial_id) REFERENCES product_formulation_trial(id);
ALTER TABLE product_batch ADD CONSTRAINT fk_product_batch_formulation_id FOREIGN KEY (formulation_id) REFERENCES product_formulation(id);
ALTER TABLE product_batch ADD CONSTRAINT fk_product_batch_spec_set_id FOREIGN KEY (spec_set_id) REFERENCES method_specification_set(id);

-- ── Module: PARTNER ───────────────────────────────────────────────────────────

ALTER TABLE partner_project ADD CONSTRAINT fk_partner_project_customer_id FOREIGN KEY (customer_id) REFERENCES partner_customer(id);

-- ── Module: SAMPLE ───────────────────────────────────────────────────────────

ALTER TABLE sample_test_request ADD CONSTRAINT fk_sample_test_request_batch_id FOREIGN KEY (batch_id) REFERENCES product_batch(id);
ALTER TABLE sample_test_request ADD CONSTRAINT fk_sample_test_request_formulation_trial_id FOREIGN KEY (formulation_trial_id) REFERENCES product_formulation_trial(id);
ALTER TABLE sample_test_request ADD CONSTRAINT fk_sample_test_request_customer_id FOREIGN KEY (customer_id) REFERENCES partner_customer(id);
ALTER TABLE sample_test_request ADD CONSTRAINT fk_sample_test_request_project_id FOREIGN KEY (project_id) REFERENCES partner_project(id);
ALTER TABLE sample_sample ADD CONSTRAINT fk_sample_sample_request_id FOREIGN KEY (request_id) REFERENCES sample_test_request(id);
ALTER TABLE sample_sample ADD CONSTRAINT fk_sample_sample_department_id FOREIGN KEY (department_id) REFERENCES sys_department(id);
ALTER TABLE sample_sample_status_history ADD CONSTRAINT fk_sample_sample_status_history_sample_id FOREIGN KEY (sample_id) REFERENCES sample_sample(id);
ALTER TABLE sample_sample_attachment ADD CONSTRAINT fk_sample_sample_attachment_sample_id FOREIGN KEY (sample_id) REFERENCES sample_sample(id);

-- ── Module: METHOD ───────────────────────────────────────────────────────────

ALTER TABLE method_method ADD CONSTRAINT fk_method_method_department_id FOREIGN KEY (department_id) REFERENCES sys_department(id);
ALTER TABLE method_method_step_item ADD CONSTRAINT fk_method_method_step_item_method_id FOREIGN KEY (method_id) REFERENCES method_method(id);
ALTER TABLE method_method_step_item ADD CONSTRAINT fk_method_method_step_item_expected_item_id FOREIGN KEY (expected_item_id) REFERENCES inventory_inventory_item(id);
ALTER TABLE method_method_validation_protocol ADD CONSTRAINT fk_method_method_validation_protocol_method_id FOREIGN KEY (method_id) REFERENCES method_method(id);
ALTER TABLE method_validation_parameter_result ADD CONSTRAINT fk_method_validation_parameter_result_protocol_id FOREIGN KEY (protocol_id) REFERENCES method_method_validation_protocol(id);
ALTER TABLE method_specification_set ADD CONSTRAINT fk_method_specification_set_formulation_id FOREIGN KEY (formulation_id) REFERENCES product_formulation(id);
ALTER TABLE method_specification_item ADD CONSTRAINT fk_method_specification_item_spec_set_id FOREIGN KEY (spec_set_id) REFERENCES method_specification_set(id);
ALTER TABLE method_specification_item ADD CONSTRAINT fk_method_specification_item_method_id FOREIGN KEY (method_id) REFERENCES method_method(id);
ALTER TABLE method_form_template ADD CONSTRAINT fk_method_form_template_method_id FOREIGN KEY (method_id) REFERENCES method_method(id);
ALTER TABLE method_form_field ADD CONSTRAINT fk_method_form_field_template_id FOREIGN KEY (template_id) REFERENCES method_form_template(id);

-- ── Module: TESTING ───────────────────────────────────────────────────────────

ALTER TABLE testing_analytical_run ADD CONSTRAINT fk_testing_analytical_run_instrument_id FOREIGN KEY (instrument_id) REFERENCES equipment_instrument(id);
ALTER TABLE testing_test ADD CONSTRAINT fk_testing_test_sample_id FOREIGN KEY (sample_id) REFERENCES sample_sample(id);
ALTER TABLE testing_test ADD CONSTRAINT fk_testing_test_method_id FOREIGN KEY (method_id) REFERENCES method_method(id);
ALTER TABLE testing_test ADD CONSTRAINT fk_testing_test_run_id FOREIGN KEY (run_id) REFERENCES testing_analytical_run(id);
ALTER TABLE testing_test ADD CONSTRAINT fk_testing_test_form_template_id FOREIGN KEY (form_template_id) REFERENCES method_form_template(id);
ALTER TABLE testing_test_step_execution ADD CONSTRAINT fk_testing_test_step_execution_test_id FOREIGN KEY (test_id) REFERENCES testing_test(id);
ALTER TABLE testing_test_step_execution ADD CONSTRAINT fk_testing_test_step_execution_method_step_item_id FOREIGN KEY (method_step_item_id) REFERENCES method_method_step_item(id);
ALTER TABLE testing_test_step_execution ADD CONSTRAINT fk_testing_test_step_execution_inventory_lot_id FOREIGN KEY (inventory_lot_id) REFERENCES inventory_inventory_lot(id);
ALTER TABLE testing_test_step_execution ADD CONSTRAINT fk_testing_test_step_execution_instrument_id FOREIGN KEY (instrument_id) REFERENCES equipment_instrument(id);
ALTER TABLE testing_form_submission ADD CONSTRAINT fk_testing_form_submission_test_id FOREIGN KEY (test_id) REFERENCES testing_test(id);
ALTER TABLE testing_result ADD CONSTRAINT fk_testing_result_test_id FOREIGN KEY (test_id) REFERENCES testing_test(id);
ALTER TABLE testing_test_result_revision ADD CONSTRAINT fk_testing_test_result_revision_result_id FOREIGN KEY (result_id) REFERENCES testing_result(id);

-- ── Module: APPROVAL ───────────────────────────────────────────────────────────

ALTER TABLE approval_report ADD CONSTRAINT fk_approval_report_sample_id FOREIGN KEY (sample_id) REFERENCES sample_sample(id);

-- ── Module: EQUIPMENT ───────────────────────────────────────────────────────────

ALTER TABLE equipment_instrument ADD CONSTRAINT fk_equipment_instrument_department_id FOREIGN KEY (department_id) REFERENCES sys_department(id);
ALTER TABLE equipment_calibration_record ADD CONSTRAINT fk_equipment_calibration_record_instrument_id FOREIGN KEY (instrument_id) REFERENCES equipment_instrument(id);
ALTER TABLE equipment_maintenance_record ADD CONSTRAINT fk_equipment_maintenance_record_instrument_id FOREIGN KEY (instrument_id) REFERENCES equipment_instrument(id);

-- ── Module: INVENTORY ───────────────────────────────────────────────────────────

ALTER TABLE inventory_inventory_item ADD CONSTRAINT fk_inventory_inventory_item_department_id FOREIGN KEY (department_id) REFERENCES sys_department(id);
ALTER TABLE inventory_inventory_lot ADD CONSTRAINT fk_inventory_inventory_lot_inventory_item_id FOREIGN KEY (inventory_item_id) REFERENCES inventory_inventory_item(id);
ALTER TABLE inventory_inventory_usage_log ADD CONSTRAINT fk_inventory_inventory_usage_log_inventory_lot_id FOREIGN KEY (inventory_lot_id) REFERENCES inventory_inventory_lot(id);
ALTER TABLE inventory_supply_request ADD CONSTRAINT fk_inventory_supply_request_sample_id FOREIGN KEY (sample_id) REFERENCES sample_sample(id);
ALTER TABLE inventory_preparation_record ADD CONSTRAINT fk_inventory_preparation_record_result_lot_id FOREIGN KEY (result_lot_id) REFERENCES inventory_inventory_lot(id);
ALTER TABLE inventory_preparation_record ADD CONSTRAINT fk_inventory_preparation_record_method_id FOREIGN KEY (method_id) REFERENCES method_method(id);
ALTER TABLE inventory_preparation_input ADD CONSTRAINT fk_inventory_preparation_input_preparation_id FOREIGN KEY (preparation_id) REFERENCES inventory_preparation_record(id);
ALTER TABLE inventory_preparation_input ADD CONSTRAINT fk_inventory_preparation_input_source_lot_id FOREIGN KEY (source_lot_id) REFERENCES inventory_inventory_lot(id);

