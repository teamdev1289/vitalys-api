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
 * Service managing quality specification sets and acceptance criteria
 * for pharmaceutical product batches across different regulatory markets (US, EU, VN).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificationService {

    private final SpecificationSetRepository specSetRepository;
    private final SpecificationItemRepository specItemRepository;
    private final MethodRepository methodRepository;
    private final ProductRepository productRepository;

    // ── Query Methods ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SpecificationSetResponse> getSpecificationSets(Long productId, String market, String status) {
        List<SpecificationSet> list;
        if (productId != null && status != null) {
            list = specSetRepository.findByProductIdAndStatus(productId, status);
        } else if (productId != null) {
            list = specSetRepository.findByProductId(productId);
        } else if (status != null) {
            list = specSetRepository.findByStatus(status);
        } else {
            list = specSetRepository.findAll();
        }

        if (market != null && !market.isBlank()) {
            list = list.stream()
                    .filter(s -> market.equalsIgnoreCase(s.getMarket()) || "GLOBAL".equalsIgnoreCase(s.getMarket()))
                    .toList();
        }

        Map<Long, Product> productMap = productRepository.findAllById(
                list.stream().map(SpecificationSet::getProductId).filter(id -> id != null).toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        return list.stream().map(s -> {
            int count = specItemRepository.findBySpecSetId(s.getId()).size();
            Product p = s.getProductId() != null ? productMap.get(s.getProductId()) : null;
            return toResponse(s, count, p);
        }).toList();
    }

    @Transactional(readOnly = true)
    public SpecificationSetDetailResponse getSpecificationSetById(Long id) {
        SpecificationSet set = findSetOrThrow(id);
        Product product = set.getProductId() != null ? productRepository.findById(set.getProductId()).orElse(null) : null;
        List<SpecificationItem> items = specItemRepository.findBySpecSetId(id);

        Map<Long, Method> methodMap = methodRepository.findAllById(
                items.stream().map(SpecificationItem::getMethodId).filter(mid -> mid != null).toList()
        ).stream().collect(Collectors.toMap(Method::getId, m -> m));

        List<SpecificationItemResponse> itemResponses = items.stream()
                .map(item -> toItemResponse(item, item.getMethodId() != null ? methodMap.get(item.getMethodId()) : null))
                .toList();

        return toDetailResponse(set, product, itemResponses);
    }

    @Transactional(readOnly = true)
    public SpecificationSetDetailResponse getActiveSpecification(Long productId, String market) {
        List<SpecificationSet> sets = specSetRepository.findByProductIdAndStatus(productId, "ACTIVE");
        if (sets.isEmpty()) {
            throw new EntityNotFoundException("Không tìm thấy bộ tiêu chuẩn ACTIVE cho sản phẩm ID: " + productId);
        }

        SpecificationSet matched = null;
        if (market != null && !market.isBlank()) {
            matched = sets.stream()
                    .filter(s -> market.equalsIgnoreCase(s.getMarket()))
                    .findFirst()
                    .orElse(null);
        }
        if (matched == null) {
            matched = sets.stream()
                    .filter(s -> "GLOBAL".equalsIgnoreCase(s.getMarket()))
                    .findFirst()
                    .orElse(sets.get(0));
        }

        return getSpecificationSetById(matched.getId());
    }

    // ── Mutation Methods ─────────────────────────────────────────────────────

    @Auditable(module = "SPECIFICATION", entity = "SpecificationSet")
    @Transactional
    public SpecificationSetDetailResponse createSpecificationSet(SpecificationSetCreateRequest request) {
        if (specSetRepository.findBySpecCode(request.getSpecCode()).isPresent()) {
            throw new IllegalArgumentException("Mã bộ tiêu chuẩn '" + request.getSpecCode() + "' đã tồn tại");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm ID: " + request.getProductId()));

        SpecificationSet specSet = SpecificationSet.builder()
                .specCode(request.getSpecCode().trim().toUpperCase())
                .name(request.getName().trim())
                .productId(product.getId())
                .formulationId(request.getFormulationId())
                .version(request.getVersion().trim())
                .market(request.getMarket() != null ? request.getMarket().toUpperCase() : "GLOBAL")
                .status("ACTIVE")
                .changeReason(request.getChangeReason())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .build();

        SpecificationSet saved = specSetRepository.save(specSet);
        log.info("Created quality specification set: {} for product {}", saved.getSpecCode(), product.getProductName());

        List<SpecificationItemResponse> itemResponses = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (SpecificationItemRequest itemReq : request.getItems()) {
                SpecificationItem item = SpecificationItem.builder()
                        .specSetId(saved.getId())
                        .methodId(itemReq.getMethodId())
                        .analyte(itemReq.getAnalyte())
                        .parameterName(itemReq.getParameterName())
                        .minLimit(itemReq.getMinLimit())
                        .maxLimit(itemReq.getMaxLimit())
                        .unit(itemReq.getUnit())
                        .comparisonOperator(itemReq.getComparisonOperator() != null ? itemReq.getComparisonOperator() : "BETWEEN")
                        .textAcceptanceCriteria(itemReq.getTextAcceptanceCriteria())
                        .build();

                SpecificationItem savedItem = specItemRepository.save(item);
                Method method = item.getMethodId() != null ? methodRepository.findById(item.getMethodId()).orElse(null) : null;
                itemResponses.add(toItemResponse(savedItem, method));
            }
        }

        return toDetailResponse(saved, product, itemResponses);
    }

    @Auditable(module = "SPECIFICATION", entity = "SpecificationSet")
    @Transactional
    public SpecificationSetResponse updateSpecificationSet(Long id, SpecificationSetUpdateRequest request) {
        SpecificationSet set = findSetOrThrow(id);

        set.setName(request.getName().trim());
        set.setVersion(request.getVersion().trim());
        if (request.getMarket() != null) set.setMarket(request.getMarket().toUpperCase());
        if (request.getStatus() != null) set.setStatus(request.getStatus().toUpperCase());
        if (request.getChangeReason() != null) set.setChangeReason(request.getChangeReason());
        if (request.getEffectiveDate() != null) set.setEffectiveDate(request.getEffectiveDate());
        if (request.getExpiryDate() != null) set.setExpiryDate(request.getExpiryDate());

        SpecificationSet updated = specSetRepository.save(set);
        int count = specItemRepository.findBySpecSetId(id).size();
        Product product = set.getProductId() != null ? productRepository.findById(set.getProductId()).orElse(null) : null;

        return toResponse(updated, count, product);
    }

    @Auditable(module = "SPECIFICATION", entity = "SpecificationItem")
    @Transactional
    public List<SpecificationItemResponse> updateSpecificationItems(Long specSetId, List<SpecificationItemRequest> items) {
        findSetOrThrow(specSetId);
        specItemRepository.deleteBySpecSetId(specSetId);

        List<SpecificationItemResponse> results = new ArrayList<>();
        for (SpecificationItemRequest itemReq : items) {
            SpecificationItem item = SpecificationItem.builder()
                    .specSetId(specSetId)
                    .methodId(itemReq.getMethodId())
                    .analyte(itemReq.getAnalyte())
                    .parameterName(itemReq.getParameterName())
                    .minLimit(itemReq.getMinLimit())
                    .maxLimit(itemReq.getMaxLimit())
                    .unit(itemReq.getUnit())
                    .comparisonOperator(itemReq.getComparisonOperator() != null ? itemReq.getComparisonOperator() : "BETWEEN")
                    .textAcceptanceCriteria(itemReq.getTextAcceptanceCriteria())
                    .build();

            SpecificationItem saved = specItemRepository.save(item);
            Method method = saved.getMethodId() != null ? methodRepository.findById(saved.getMethodId()).orElse(null) : null;
            results.add(toItemResponse(saved, method));
        }

        return results;
    }

    @Auditable(module = "SPECIFICATION", entity = "SpecificationSet")
    @Transactional
    public void deleteSpecificationSet(Long id) {
        SpecificationSet set = findSetOrThrow(id);
        if ("ACTIVE".equalsIgnoreCase(set.getStatus())) {
            set.setStatus("OBSOLETE");
            specSetRepository.save(set);
            log.warn("Active specification set {} marked OBSOLETE instead of deleted", set.getSpecCode());
            return;
        }

        specItemRepository.deleteBySpecSetId(id);
        specSetRepository.delete(set);
        log.info("Deleted specification set: {}", set.getSpecCode());
    }

    // ── Helper Mappers ───────────────────────────────────────────────────────

    private SpecificationSet findSetOrThrow(Long id) {
        return specSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bộ tiêu chuẩn với ID: " + id));
    }

    private SpecificationSetResponse toResponse(SpecificationSet s, int itemCount, Product p) {
        return SpecificationSetResponse.builder()
                .id(s.getId())
                .specCode(s.getSpecCode())
                .name(s.getName())
                .productId(s.getProductId())
                .productCode(p != null ? p.getProductCode() : null)
                .productName(p != null ? p.getProductName() : null)
                .formulationId(s.getFormulationId())
                .version(s.getVersion())
                .market(s.getMarket())
                .status(s.getStatus())
                .changeReason(s.getChangeReason())
                .effectiveDate(s.getEffectiveDate())
                .expiryDate(s.getExpiryDate())
                .itemCount(itemCount)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private SpecificationSetDetailResponse toDetailResponse(SpecificationSet s, Product p, List<SpecificationItemResponse> items) {
        return SpecificationSetDetailResponse.builder()
                .id(s.getId())
                .specCode(s.getSpecCode())
                .name(s.getName())
                .productId(s.getProductId())
                .productCode(p != null ? p.getProductCode() : null)
                .productName(p != null ? p.getProductName() : null)
                .formulationId(s.getFormulationId())
                .version(s.getVersion())
                .market(s.getMarket())
                .status(s.getStatus())
                .changeReason(s.getChangeReason())
                .effectiveDate(s.getEffectiveDate())
                .expiryDate(s.getExpiryDate())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .items(items)
                .build();
    }

    private SpecificationItemResponse toItemResponse(SpecificationItem item, Method method) {
        return SpecificationItemResponse.builder()
                .id(item.getId())
                .specSetId(item.getSpecSetId())
                .methodId(item.getMethodId())
                .methodCode(method != null ? method.getMethodCode() : null)
                .methodName(method != null ? method.getName() : null)
                .analyte(item.getAnalyte())
                .parameterName(item.getParameterName())
                .minLimit(item.getMinLimit())
                .maxLimit(item.getMaxLimit())
                .unit(item.getUnit())
                .comparisonOperator(item.getComparisonOperator())
                .textAcceptanceCriteria(item.getTextAcceptanceCriteria())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
