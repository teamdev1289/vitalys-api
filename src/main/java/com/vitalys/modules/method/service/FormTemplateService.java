package com.vitalys.modules.method.service;

import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.entity.FormField;
import com.vitalys.modules.method.entity.FormTemplate;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.repository.FormFieldRepository;
import com.vitalys.modules.method.repository.FormTemplateRepository;
import com.vitalys.modules.method.repository.MethodRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing dynamic experiment form templates and schema definitions
 * for test data entry and automatic formula evaluations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormTemplateService {

    private final FormTemplateRepository templateRepository;
    private final FormFieldRepository fieldRepository;
    private final MethodRepository methodRepository;

    // ── Query Methods ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FormTemplateResponse> getTemplates(Long methodId) {
        List<FormTemplate> templates = (methodId != null)
                ? templateRepository.findByMethodId(methodId)
                : templateRepository.findAll();

        Map<Long, Method> methodMap = methodRepository.findAllById(
                templates.stream().map(FormTemplate::getMethodId).filter(id -> id != null).toList()
        ).stream().collect(Collectors.toMap(Method::getId, m -> m));

        return templates.stream().map(t -> {
            int count = fieldRepository.findByTemplateIdOrderByOrderIndexAsc(t.getId()).size();
            Method m = t.getMethodId() != null ? methodMap.get(t.getMethodId()) : null;
            return toResponse(t, count, m);
        }).toList();
    }

    @Transactional(readOnly = true)
    public FormTemplateResponse getTemplateById(Long id) {
        FormTemplate template = findTemplateOrThrow(id);
        Method method = template.getMethodId() != null ? methodRepository.findById(template.getMethodId()).orElse(null) : null;
        List<FormField> fields = fieldRepository.findByTemplateIdOrderByOrderIndexAsc(id);

        FormTemplateResponse response = toResponse(template, fields.size(), method);
        response.setFields(fields.stream().map(this::toFieldResponse).toList());
        return response;
    }

    @Transactional(readOnly = true)
    public FormTemplateResponse getActiveTemplateByMethodId(Long methodId) {
        FormTemplate template = templateRepository.findByMethodIdAndStatus(methodId, "RELEASED")
                .or(() -> templateRepository.findByMethodId(methodId).stream().findFirst())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biểu mẫu động cho phương pháp thử ID: " + methodId));

        return getTemplateById(template.getId());
    }

    // ── Mutation Methods ─────────────────────────────────────────────────────

    @Auditable(module = "METHOD", entity = "FormTemplate")
    @Transactional
    public FormTemplateResponse createTemplate(FormTemplateCreateRequest request) {
        if (templateRepository.findBySchemaName(request.getSchemaName()).isPresent()) {
            throw new IllegalArgumentException("Mã schema '" + request.getSchemaName() + "' đã tồn tại");
        }

        Method method = methodRepository.findById(request.getMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phương pháp thử ID: " + request.getMethodId()));

        FormTemplate template = FormTemplate.builder()
                .methodId(method.getId())
                .schemaName(request.getSchemaName().trim().toUpperCase())
                .title(request.getTitle().trim())
                .version(request.getVersion().trim())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT")
                .description(request.getDescription())
                .build();

        FormTemplate saved = templateRepository.save(template);
        log.info("Created dynamic form template: {} ({})", saved.getSchemaName(), saved.getTitle());

        List<FormFieldResponse> fieldResponses = new ArrayList<>();
        if (request.getFields() != null && !request.getFields().isEmpty()) {
            int idx = 1;
            for (FormFieldRequest fieldReq : request.getFields()) {
                FormField field = FormField.builder()
                        .templateId(saved.getId())
                        .fieldKey(fieldReq.getFieldKey())
                        .label(fieldReq.getLabel())
                        .fieldType(fieldReq.getFieldType())
                        .dataBinding(fieldReq.getDataBinding())
                        .isRequired(fieldReq.getIsRequired() != null ? fieldReq.getIsRequired() : false)
                        .orderIndex(fieldReq.getOrderIndex() != null ? fieldReq.getOrderIndex() : String.valueOf(idx++))
                        .optionsJson(fieldReq.getOptionsJson())
                        .defaultValue(fieldReq.getDefaultValue())
                        .unit(fieldReq.getUnit())
                        .formulaExpression(fieldReq.getFormulaExpression())
                        .validationRules(fieldReq.getValidationRules())
                        .build();

                FormField savedField = fieldRepository.save(field);
                fieldResponses.add(toFieldResponse(savedField));
            }
        }

        FormTemplateResponse response = toResponse(saved, fieldResponses.size(), method);
        response.setFields(fieldResponses);
        return response;
    }

    @Auditable(module = "METHOD", entity = "FormTemplate")
    @Transactional
    public FormTemplateResponse updateTemplate(Long id, FormTemplateUpdateRequest request) {
        FormTemplate template = findTemplateOrThrow(id);

        template.setTitle(request.getTitle().trim());
        template.setVersion(request.getVersion().trim());
        if (request.getStatus() != null) template.setStatus(request.getStatus().toUpperCase());
        if (request.getDescription() != null) template.setDescription(request.getDescription());

        FormTemplate updated = templateRepository.save(template);
        Method method = updated.getMethodId() != null ? methodRepository.findById(updated.getMethodId()).orElse(null) : null;
        int count = fieldRepository.findByTemplateIdOrderByOrderIndexAsc(id).size();

        return toResponse(updated, count, method);
    }

    @Auditable(module = "METHOD", entity = "FormField")
    @Transactional
    public List<FormFieldResponse> updateTemplateFields(Long templateId, List<FormFieldRequest> fields) {
        findTemplateOrThrow(templateId);
        fieldRepository.deleteByTemplateId(templateId);

        List<FormFieldResponse> results = new ArrayList<>();
        int idx = 1;
        for (FormFieldRequest fieldReq : fields) {
            FormField field = FormField.builder()
                    .templateId(templateId)
                    .fieldKey(fieldReq.getFieldKey())
                    .label(fieldReq.getLabel())
                    .fieldType(fieldReq.getFieldType())
                    .dataBinding(fieldReq.getDataBinding())
                    .isRequired(fieldReq.getIsRequired() != null ? fieldReq.getIsRequired() : false)
                    .orderIndex(fieldReq.getOrderIndex() != null ? fieldReq.getOrderIndex() : String.valueOf(idx++))
                    .optionsJson(fieldReq.getOptionsJson())
                    .defaultValue(fieldReq.getDefaultValue())
                    .unit(fieldReq.getUnit())
                    .formulaExpression(fieldReq.getFormulaExpression())
                    .validationRules(fieldReq.getValidationRules())
                    .build();

            FormField saved = fieldRepository.save(field);
            results.add(toFieldResponse(saved));
        }

        return results;
    }

    @Auditable(module = "METHOD", entity = "FormTemplate")
    @Transactional
    public void deleteTemplate(Long id) {
        FormTemplate template = findTemplateOrThrow(id);
        fieldRepository.deleteByTemplateId(id);
        templateRepository.delete(template);
        log.info("Deleted dynamic form template: {}", template.getSchemaName());
    }

    // ── Helper Mappers ───────────────────────────────────────────────────────

    private FormTemplate findTemplateOrThrow(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biểu mẫu động với ID: " + id));
    }

    private FormTemplateResponse toResponse(FormTemplate t, int fieldCount, Method m) {
        return FormTemplateResponse.builder()
                .id(t.getId())
                .methodId(t.getMethodId())
                .methodCode(m != null ? m.getMethodCode() : null)
                .methodName(m != null ? m.getName() : null)
                .schemaName(t.getSchemaName())
                .title(t.getTitle())
                .version(t.getVersion())
                .status(t.getStatus())
                .description(t.getDescription())
                .fieldCount(fieldCount)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private FormFieldResponse toFieldResponse(FormField f) {
        return FormFieldResponse.builder()
                .id(f.getId())
                .templateId(f.getTemplateId())
                .fieldKey(f.getFieldKey())
                .label(f.getLabel())
                .fieldType(f.getFieldType())
                .dataBinding(f.getDataBinding())
                .isRequired(f.getIsRequired())
                .orderIndex(f.getOrderIndex())
                .optionsJson(f.getOptionsJson())
                .defaultValue(f.getDefaultValue())
                .unit(f.getUnit())
                .formulaExpression(f.getFormulaExpression())
                .validationRules(f.getValidationRules())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
