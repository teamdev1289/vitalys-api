package com.vitalys.modules.method.service;

import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.entity.MethodStepItem;
import com.vitalys.modules.method.entity.MethodValidationProtocol;
import com.vitalys.modules.method.repository.MethodRepository;
import com.vitalys.modules.method.repository.MethodStepItemRepository;
import com.vitalys.modules.method.repository.MethodValidationProtocolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MethodServiceTest {

    @Mock
    private MethodRepository methodRepository;

    @Mock
    private MethodStepItemRepository stepItemRepository;

    @Mock
    private MethodValidationProtocolRepository validationProtocolRepository;

    @InjectMocks
    private MethodService methodService;

    private Method sampleMethod;
    private MethodStepItem sampleStep;

    @BeforeEach
    void setUp() {
        sampleMethod = Method.builder()
                .id(1L)
                .methodCode("SOP-HPLC-001")
                .name("Paracetamol Assay by HPLC")
                .version("v1.0")
                .category("ASSAY")
                .instrumentType("HPLC")
                .sourceStandard("USP 44")
                .validationStatus("DRAFT")
                .isActive(true)
                .description("Sample assay test method")
                .bodyTemplate("1. Prep sample...")
                .build();

        sampleStep = MethodStepItem.builder()
                .id(101L)
                .methodId(1L)
                .stepKey("STEP_01")
                .label("Weigh 20 tablets")
                .role("ANALYST")
                .expectedQuantity(20.0)
                .unit("TABLETS")
                .orderIndex("1")
                .build();
    }

    @Test
    @DisplayName("getMethods returns list of method responses with step count")
    void getMethods_Success() {
        when(methodRepository.searchMethods(null, null, null)).thenReturn(List.of(sampleMethod));
        when(stepItemRepository.findByMethodIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(sampleStep));

        List<MethodResponse> results = methodService.getMethods(null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMethodCode()).isEqualTo("SOP-HPLC-001");
        assertThat(results.get(0).getStepCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getMethodById returns method detail with steps and protocols")
    void getMethodById_Success() {
        when(methodRepository.findById(1L)).thenReturn(Optional.of(sampleMethod));
        when(stepItemRepository.findByMethodIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(sampleStep));
        when(validationProtocolRepository.findByMethodId(1L)).thenReturn(Collections.emptyList());

        MethodDetailResponse response = methodService.getMethodById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getMethodCode()).isEqualTo("SOP-HPLC-001");
        assertThat(response.getSteps()).hasSize(1);
        assertThat(response.getSteps().get(0).getStepKey()).isEqualTo("STEP_01");
    }

    @Test
    @DisplayName("createMethod throws exception when methodCode already exists")
    void createMethod_DuplicateCode_ThrowsException() {
        MethodCreateRequest request = MethodCreateRequest.builder()
                .methodCode("SOP-HPLC-001")
                .name("Duplicate method")
                .version("v1.0")
                .build();

        when(methodRepository.findByMethodCode("SOP-HPLC-001")).thenReturn(Optional.of(sampleMethod));

        assertThatThrownBy(() -> methodService.createMethod(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");
    }

    @Test
    @DisplayName("createMethod succeeds and persists method and steps")
    void createMethod_Success() {
        MethodStepRequest stepReq = MethodStepRequest.builder()
                .stepKey("STEP_01")
                .label("Weigh 20 tablets")
                .expectedQuantity(20.0)
                .unit("TABLETS")
                .build();

        MethodCreateRequest request = MethodCreateRequest.builder()
                .methodCode("SOP-UV-002")
                .name("UV Assay Method")
                .version("v1.0")
                .category("ASSAY")
                .steps(List.of(stepReq))
                .build();

        when(methodRepository.findByMethodCode("SOP-UV-002")).thenReturn(Optional.empty());
        when(methodRepository.save(any(Method.class))).thenAnswer(invocation -> {
            Method m = invocation.getArgument(0);
            m.setId(2L);
            return m;
        });
        when(stepItemRepository.save(any(MethodStepItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MethodDetailResponse result = methodService.createMethod(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getMethodCode()).isEqualTo("SOP-UV-002");
        assertThat(result.getValidationStatus()).isEqualTo("DRAFT");
        assertThat(result.getSteps()).hasSize(1);
        verify(methodRepository).save(any(Method.class));
        verify(stepItemRepository).save(any(MethodStepItem.class));
    }

    @Test
    @DisplayName("updateMethodStatus validates transitions and updates status")
    void updateMethodStatus_Success() {
        when(methodRepository.findById(1L)).thenReturn(Optional.of(sampleMethod));
        when(methodRepository.save(any(Method.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stepItemRepository.findByMethodIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(sampleStep));

        MethodStatusUpdateRequest updateReq = MethodStatusUpdateRequest.builder()
                .status("VALIDATED")
                .changeReason("Analytical method validation complete")
                .build();

        MethodResponse response = methodService.updateMethodStatus(1L, updateReq);

        assertThat(response.getValidationStatus()).isEqualTo("VALIDATED");
    }

    @Test
    @DisplayName("updateMethodStatus rejects invalid transitions")
    void updateMethodStatus_InvalidTransition_ThrowsException() {
        when(methodRepository.findById(1L)).thenReturn(Optional.of(sampleMethod));

        // Attempting to jump directly from DRAFT to RELEASED without validation is disallowed
        MethodStatusUpdateRequest updateReq = MethodStatusUpdateRequest.builder()
                .status("RELEASED")
                .build();

        assertThatThrownBy(() -> methodService.updateMethodStatus(1L, updateReq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không được phép chuyển đổi trạng thái");
    }
}
