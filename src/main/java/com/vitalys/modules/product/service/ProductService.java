package com.vitalys.modules.product.service;

import com.vitalys.modules.product.dto.*;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing pharmaceutical product master records and manufacturing/trial batches.
 * All mutations are audited for GMP and GxP compliance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;

    // ── Product Operations ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String search, String status, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(search, status, pageable);

        List<Long> productIds = page.getContent().stream().map(Product::getId).toList();
        Map<Long, Integer> batchCountMap = productIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> batchRepository.findByProductId(id).size()
                ));

        return page.map(p -> toProductResponse(p, batchCountMap.getOrDefault(p.getId(), 0)));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductOrThrow(id);
        int batchCount = batchRepository.findByProductId(id).size();
        return toProductResponse(product, batchCount);
    }

    @Auditable(module = "PRODUCT", entity = "Product")
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists: " + request.getProductCode());
        }

        Product product = Product.builder()
                .productCode(request.getProductCode())
                .registrationNumber(request.getRegistrationNumber())
                .productName(request.getProductName())
                .dosageForm(request.getDosageForm())
                .packagingSpec(request.getPackagingSpec())
                .shelfLifeMonths(request.getShelfLifeMonths())
                .registrant(request.getRegistrant())
                .manufacturer(request.getManufacturer())
                .countryOfOrigin(request.getCountryOfOrigin())
                .qualityStandard(request.getQualityStandard())
                .productCategory(request.getProductCategory())
                .classification(request.getClassification())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .description(request.getDescription())
                .build();

        Product saved = productRepository.save(product);
        log.info("Registered product ID {}: {} [{}]", saved.getId(), saved.getProductName(), saved.getProductCode());
        return toProductResponse(saved, 0);
    }

    @Auditable(module = "PRODUCT", entity = "Product")
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findProductOrThrow(id);

        if (productRepository.existsByProductCodeAndIdNot(request.getProductCode(), id)) {
            throw new IllegalArgumentException("Product code already in use by another drug: " + request.getProductCode());
        }

        product.setProductCode(request.getProductCode());
        product.setRegistrationNumber(request.getRegistrationNumber());
        product.setProductName(request.getProductName());
        product.setDosageForm(request.getDosageForm());
        product.setPackagingSpec(request.getPackagingSpec());
        product.setShelfLifeMonths(request.getShelfLifeMonths());
        product.setRegistrant(request.getRegistrant());
        product.setManufacturer(request.getManufacturer());
        product.setCountryOfOrigin(request.getCountryOfOrigin());
        product.setQualityStandard(request.getQualityStandard());
        product.setProductCategory(request.getProductCategory());
        product.setClassification(request.getClassification());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        product.setDescription(request.getDescription());

        Product updated = productRepository.save(product);
        int batchCount = batchRepository.findByProductId(id).size();
        log.info("Updated product ID {}: {} [{}]", updated.getId(), updated.getProductName(), updated.getProductCode());
        return toProductResponse(updated, batchCount);
    }

    @Auditable(module = "PRODUCT", entity = "Product")
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductOrThrow(id);
        List<Batch> batches = batchRepository.findByProductId(id);
        if (!batches.isEmpty()) {
            throw new IllegalStateException("Cannot delete product with existing batches. Mark as DISCONTINUED instead.");
        }
        productRepository.delete(product);
        log.info("Deleted product ID {}: {}", id, product.getProductName());
    }

    // ── Batch Operations ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<BatchResponse> getBatches(String search, Long productId, String status, Pageable pageable) {
        Page<Batch> page = batchRepository.searchBatches(search, productId, status, pageable);

        Map<Long, Product> productMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        return page.map(b -> toBatchResponse(b, productMap.get(b.getProductId())));
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatchById(Long id) {
        Batch batch = findBatchOrThrow(id);
        Product product = batch.getProductId() != null
                ? productRepository.findById(batch.getProductId()).orElse(null)
                : null;
        return toBatchResponse(batch, product);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByProduct(Long productId) {
        Product product = findProductOrThrow(productId);
        return batchRepository.findByProductId(productId).stream()
                .map(b -> toBatchResponse(b, product))
                .collect(Collectors.toList());
    }

    @Auditable(module = "PRODUCT", entity = "Batch")
    @Transactional
    public BatchResponse createBatch(BatchCreateRequest request) {
        Product product = findProductOrThrow(request.getProductId());

        if (batchRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new IllegalArgumentException("Batch number already exists: " + request.getBatchNumber());
        }

        Batch batch = Batch.builder()
                .productId(product.getId())
                .formulationId(request.getFormulationId())
                .specSetId(request.getSpecSetId())
                .batchNumber(request.getBatchNumber())
                .manufacturingDate(request.getManufacturingDate())
                .expiryDate(request.getExpiryDate())
                .quantityProduced(request.getQuantityProduced())
                .unit(request.getUnit() != null ? request.getUnit() : "TABLETS")
                .status(request.getStatus() != null ? request.getStatus() : "QUARANTINE")
                .notes(request.getNotes())
                .build();

        Batch saved = batchRepository.save(batch);
        log.info("Created batch ID {}: {} for product {}", saved.getId(), saved.getBatchNumber(), product.getProductName());
        return toBatchResponse(saved, product);
    }

    @Auditable(module = "PRODUCT", entity = "Batch")
    @Transactional
    public BatchResponse updateBatch(Long id, BatchUpdateRequest request) {
        Batch batch = findBatchOrThrow(id);
        Product product = findProductOrThrow(request.getProductId());

        if (batchRepository.existsByBatchNumberAndIdNot(request.getBatchNumber(), id)) {
            throw new IllegalArgumentException("Batch number already in use: " + request.getBatchNumber());
        }

        batch.setProductId(product.getId());
        batch.setFormulationId(request.getFormulationId());
        batch.setSpecSetId(request.getSpecSetId());
        batch.setBatchNumber(request.getBatchNumber());
        batch.setManufacturingDate(request.getManufacturingDate());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setQuantityProduced(request.getQuantityProduced());
        if (request.getUnit() != null) {
            batch.setUnit(request.getUnit());
        }
        if (request.getStatus() != null) {
            batch.setStatus(request.getStatus());
        }
        batch.setNotes(request.getNotes());

        Batch updated = batchRepository.save(batch);
        log.info("Updated batch ID {}: {}", updated.getId(), updated.getBatchNumber());
        return toBatchResponse(updated, product);
    }

    @Auditable(module = "PRODUCT", entity = "Batch")
    @Transactional
    public BatchResponse updateBatchStatus(Long id, String status) {
        Batch batch = findBatchOrThrow(id);
        batch.setStatus(status);
        Batch updated = batchRepository.save(batch);
        Product product = batch.getProductId() != null
                ? productRepository.findById(batch.getProductId()).orElse(null)
                : null;
        log.info("Updated batch ID {} status to {}", id, status);
        return toBatchResponse(updated, product);
    }

    @Auditable(module = "PRODUCT", entity = "Batch")
    @Transactional
    public void deleteBatch(Long id) {
        Batch batch = findBatchOrThrow(id);
        batchRepository.delete(batch);
        log.info("Deleted batch ID {}: {}", id, batch.getBatchNumber());
    }

    // ── Helper Mappers ────────────────────────────────────────────────────────

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + id));
    }

    private Batch findBatchOrThrow(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found with ID: " + id));
    }

    private ProductResponse toProductResponse(Product p, int activeBatchCount) {
        return ProductResponse.builder()
                .id(p.getId())
                .productCode(p.getProductCode())
                .registrationNumber(p.getRegistrationNumber())
                .productName(p.getProductName())
                .dosageForm(p.getDosageForm())
                .packagingSpec(p.getPackagingSpec())
                .shelfLifeMonths(p.getShelfLifeMonths())
                .registrant(p.getRegistrant())
                .manufacturer(p.getManufacturer())
                .countryOfOrigin(p.getCountryOfOrigin())
                .qualityStandard(p.getQualityStandard())
                .productCategory(p.getProductCategory())
                .classification(p.getClassification())
                .status(p.getStatus())
                .description(p.getDescription())
                .activeBatchCount(activeBatchCount)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private BatchResponse toBatchResponse(Batch b, Product p) {
        boolean expired = b.getExpiryDate() != null && b.getExpiryDate().isBefore(OffsetDateTime.now());

        return BatchResponse.builder()
                .id(b.getId())
                .productId(b.getProductId())
                .productCode(p != null ? p.getProductCode() : null)
                .productName(p != null ? p.getProductName() : null)
                .formulationId(b.getFormulationId())
                .specSetId(b.getSpecSetId())
                .batchNumber(b.getBatchNumber())
                .manufacturingDate(b.getManufacturingDate())
                .expiryDate(b.getExpiryDate())
                .quantityProduced(b.getQuantityProduced())
                .unit(b.getUnit())
                .status(b.getStatus())
                .notes(b.getNotes())
                .isExpired(expired)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
