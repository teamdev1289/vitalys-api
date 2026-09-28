package com.vitalys.modules.method.service;

import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.entity.FormField;
import com.vitalys.modules.method.entity.FormTemplate;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.repository.FormFieldRepository;
import com.vitalys.modules.method.repository.FormTemplateRepository;
import com.vitalys.modules.method.repository.MethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormTemplateServiceTest {

    @Mock
    private FormTemplateRepository templateRepository;

    @Mock
    private FormFieldRepository fieldRepository;

    @Mock
    private MethodRepository methodRepository;

    @InjectMocks
    private FormTemplateService formTemplateService;

    private Method sampleMethod;
    private FormTemplate sampleTemplate;
    private FormField sampleField;

    @BeforeEach
    void setUp() {
        sampleMethod = Method.builder()
                .id(1L)
                .methodCode("SOP-HPLC-001")
                .name("Paracetamol Assay by HPLC")
                .build();

        sampleTemplate = FormTemplate.builder()
                .id(10L)
                .methodId(1L)
                .schemaName("FORM_HPLC_V1")
                .title("HPLC Assay Sheet")
                .version("v1.0")
                .status("RELEASED")
                .build();

        sampleField = FormField.builder()
                .id(100L)
                .templateId(10L)
                .fieldKey("w_sample")
                .label("Sample weight (mg)")
                .fieldType("NUMBER")
                .isRequired(true)
                .orderIndex("1")
                .build();
    }

    @Test
    @DisplayName("getTemplates returns list with field count and method details")
    void getTemplates_Success() {
        when(templateRepository.findByMethodId(1L)).thenReturn(List.of(sampleTemplate));
        when(methodRepository.findAllById(List.of(1L))).thenReturn(List.of(sampleMethod));
        when(fieldRepository.findByTemplateIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(sampleField));

        List<FormTemplateResponse> results = formTemplateService.getTemplates(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSchemaName()).isEqualTo("FORM_HPLC_V1");
        assertThat(results.get(0).getFieldCount()).isEqualTo(1);
        assertThat(results.get(0).getMethodCode()).isEqualTo("SOP-HPLC-001");
    }

    @Test
    @DisplayName("getTemplateById returns template and list of fields")
    void getTemplateById_Success() {
        when(templateRepository.findById(10L)).thenReturn(Optional.of(sampleTemplate));
        when(methodRepository.findById(1L)).thenReturn(Optional.of(sampleMethod));
        when(fieldRepository.findByTemplateIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(sampleField));

        FormTemplateResponse response = formTemplateService.getTemplateById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getSchemaName()).isEqualTo("FORM_HPLC_V1");
        assertThat(response.getFields()).hasSize(1);
        assertThat(response.getFields().get(0).getFieldKey()).isEqualTo("w_sample");
    }

    @Test
    @DisplayName("createTemplate succeeds and creates template and fields")
    void createTemplate_Success() {
        FormFieldRequest fieldReq = FormFieldRequest.builder()
                .fieldKey("w_sample")
                .label("Sample weight (mg)")
                .fieldType("NUMBER")
                .isRequired(true)
                .build();

        FormTemplateCreateRequest request = FormTemplateCreateRequest.builder()
                .methodId(1L)
                .schemaName("FORM_NEW_V1")
                .title("New Form Template")
                .version("v1.0")
                .fields(List.of(fieldReq))
                .build();

        when(templateRepository.findBySchemaName("FORM_NEW_V1")).thenReturn(Optional.empty());
        when(methodRepository.findById(1L)).thenReturn(Optional.of(sampleMethod));
        when(templateRepository.save(any(FormTemplate.class))).thenAnswer(invocation -> {
            FormTemplate t = invocation.getArgument(0);
            t.setId(11L);
            return t;
        });
        when(fieldRepository.save(any(FormField.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FormTemplateResponse result = formTemplateService.createTemplate(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getSchemaName()).isEqualTo("FORM_NEW_V1");
        assertThat(result.getFields()).hasSize(1);
        verify(templateRepository).save(any(FormTemplate.class));
        verify(fieldRepository).save(any(FormField.class));
    }

    @Test
    @DisplayName("createTemplate throws exception when schemaName already exists")
    void createTemplate_DuplicateSchema_ThrowsException() {
        FormTemplateCreateRequest request = FormTemplateCreateRequest.builder()
                .methodId(1L)
                .schemaName("FORM_HPLC_V1")
                .title("Duplicate")
                .version("v1.0")
                .build();

        when(templateRepository.findBySchemaName("FORM_HPLC_V1")).thenReturn(Optional.of(sampleTemplate));

        assertThatThrownBy(() -> formTemplateService.createTemplate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");
    }
}
