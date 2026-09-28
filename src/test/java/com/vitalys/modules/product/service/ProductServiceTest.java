package com.vitalys.modules.product.service;

import com.vitalys.modules.product.dto.*;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private Batch sampleBatch;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .productCode("PRD-PARA-500")
                .productName("Paracetamol 500mg Tablets")
                .dosageForm("Tablet")
                .qualityStandard("USP 43")
                .status("ACTIVE")
                .build();

        sampleBatch = Batch.builder()
                .id(10L)
                .productId(1L)
                .batchNumber("BATCH-260101")
                .manufacturingDate(OffsetDateTime.now().minusMonths(2))
                .expiryDate(OffsetDateTime.now().plusYears(2))
                .quantityProduced(500000.0)
                .unit("TABLETS")
                .status("RELEASED")
                .build();
    }

    @Test
    @DisplayName("createProduct - creates product successfully")
    void testCreateProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .productCode("PRD-PARA-500")
                .productName("Paracetamol 500mg Tablets")
                .dosageForm("Tablet")
                .build();

        when(productRepository.existsByProductCode("PRD-PARA-500")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getProductCode()).isEqualTo("PRD-PARA-500");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct - duplicate code throws error")
    void testCreateProduct_DuplicateCode() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .productCode("PRD-PARA-500")
                .productName("Another Paracetamol")
                .build();

        when(productRepository.existsByProductCode("PRD-PARA-500")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product code already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBatch - creates batch for existing product")
    void testCreateBatch_Success() {
        BatchCreateRequest request = BatchCreateRequest.builder()
                .productId(1L)
                .batchNumber("BATCH-260101")
                .manufacturingDate(OffsetDateTime.now())
                .expiryDate(OffsetDateTime.now().plusYears(2))
                .quantityProduced(500000.0)
                .unit("TABLETS")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(batchRepository.existsByBatchNumber("BATCH-260101")).thenReturn(false);
        when(batchRepository.save(any(Batch.class))).thenReturn(sampleBatch);

        BatchResponse response = productService.createBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getBatchNumber()).isEqualTo("BATCH-260101");
        assertThat(response.getProductCode()).isEqualTo("PRD-PARA-500");
        verify(batchRepository).save(any(Batch.class));
    }

    @Test
    @DisplayName("getProducts - returns paginated responses with batch count")
    void testGetProducts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);

        when(productRepository.searchProducts(null, null, pageable)).thenReturn(page);
        when(batchRepository.findByProductId(1L)).thenReturn(List.of(sampleBatch));

        Page<ProductResponse> result = productService.getProducts(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActiveBatchCount()).isEqualTo(1);
    }
}
