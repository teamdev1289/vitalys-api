package com.vitalys.modules.method.service;

import com.vitalys.modules.method.dto.*;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.entity.SpecificationItem;
import com.vitalys.modules.method.entity.SpecificationSet;
import com.vitalys.modules.method.repository.MethodRepository;
import com.vitalys.modules.method.repository.SpecificationItemRepository;
import com.vitalys.modules.method.repository.SpecificationSetRepository;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.ProductRepository;
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
class SpecificationServiceTest {

    @Mock
    private SpecificationSetRepository specSetRepository;

    @Mock
    private SpecificationItemRepository specItemRepository;

    @Mock
    private MethodRepository methodRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SpecificationService specificationService;

    private Product sampleProduct;
    private SpecificationSet sampleSpecSet;
    private SpecificationItem sampleItem;
    private Method sampleMethod;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(10L)
                .productCode("PROD-PARA-500")
                .productName("Paracetamol 500mg Tablets")
                .build();

        sampleSpecSet = SpecificationSet.builder()
                .id(1L)
                .specCode("SPEC-PARA-500-VN")
                .name("Release Spec Paracetamol VN")
                .productId(10L)
                .version("v1.0")
                .market("VN")
                .status("ACTIVE")
                .build();

        sampleMethod = Method.builder()
                .id(100L)
                .methodCode("SOP-HPLC-001")
                .name("Paracetamol Assay")
                .build();

        sampleItem = SpecificationItem.builder()
                .id(501L)
                .specSetId(1L)
                .methodId(100L)
                .analyte("Paracetamol")
                .parameterName("Assay")
                .minLimit(95.0)
                .maxLimit(105.0)
                .unit("%")
                .comparisonOperator("BETWEEN")
                .build();
    }

    @Test
    @DisplayName("getSpecificationSets returns enriched list of specification sets")
    void getSpecificationSets_Success() {
        when(specSetRepository.findByProductIdAndStatus(10L, "ACTIVE")).thenReturn(List.of(sampleSpecSet));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(sampleProduct));
        when(specItemRepository.findBySpecSetId(1L)).thenReturn(List.of(sampleItem));

        List<SpecificationSetResponse> results = specificationService.getSpecificationSets(10L, "VN", "ACTIVE");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSpecCode()).isEqualTo("SPEC-PARA-500-VN");
        assertThat(results.get(0).getItemCount()).isEqualTo(1);
        assertThat(results.get(0).getProductName()).isEqualTo("Paracetamol 500mg Tablets");
    }

    @Test
    @DisplayName("getActiveSpecification returns active spec set matching product and market")
    void getActiveSpecification_Success() {
        when(specSetRepository.findByProductIdAndStatus(10L, "ACTIVE")).thenReturn(List.of(sampleSpecSet));
        when(specSetRepository.findById(1L)).thenReturn(Optional.of(sampleSpecSet));
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(specItemRepository.findBySpecSetId(1L)).thenReturn(List.of(sampleItem));
        when(methodRepository.findAllById(List.of(100L))).thenReturn(List.of(sampleMethod));

        SpecificationSetDetailResponse response = specificationService.getActiveSpecification(10L, "VN");

        assertThat(response).isNotNull();
        assertThat(response.getSpecCode()).isEqualTo("SPEC-PARA-500-VN");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getMethodCode()).isEqualTo("SOP-HPLC-001");
        assertThat(response.getItems().get(0).getMinLimit()).isEqualTo(95.0);
    }

    @Test
    @DisplayName("createSpecificationSet succeeds and persists set and items")
    void createSpecificationSet_Success() {
        SpecificationItemRequest itemReq = SpecificationItemRequest.builder()
                .parameterName("Assay")
                .methodId(100L)
                .minLimit(98.0)
                .maxLimit(102.0)
                .unit("%")
                .comparisonOperator("BETWEEN")
                .build();

        SpecificationSetCreateRequest request = SpecificationSetCreateRequest.builder()
                .specCode("SPEC-PARA-500-US")
                .name("Release Spec Paracetamol US")
                .productId(10L)
                .version("v1.0")
                .market("US")
                .items(List.of(itemReq))
                .build();

        when(specSetRepository.findBySpecCode("SPEC-PARA-500-US")).thenReturn(Optional.empty());
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(specSetRepository.save(any(SpecificationSet.class))).thenAnswer(invocation -> {
            SpecificationSet s = invocation.getArgument(0);
            s.setId(2L);
            return s;
        });
        when(specItemRepository.save(any(SpecificationItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(methodRepository.findById(100L)).thenReturn(Optional.of(sampleMethod));

        SpecificationSetDetailResponse result = specificationService.createSpecificationSet(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getSpecCode()).isEqualTo("SPEC-PARA-500-US");
        assertThat(result.getItems()).hasSize(1);
        verify(specSetRepository).save(any(SpecificationSet.class));
        verify(specItemRepository).save(any(SpecificationItem.class));
    }

    @Test
    @DisplayName("createSpecificationSet throws exception if specCode already exists")
    void createSpecificationSet_DuplicateCode_ThrowsException() {
        SpecificationSetCreateRequest request = SpecificationSetCreateRequest.builder()
                .specCode("SPEC-PARA-500-VN")
                .name("Duplicate Spec")
                .productId(10L)
                .version("v1.0")
                .build();

        when(specSetRepository.findBySpecCode("SPEC-PARA-500-VN")).thenReturn(Optional.of(sampleSpecSet));

        assertThatThrownBy(() -> specificationService.createSpecificationSet(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");
    }
}
