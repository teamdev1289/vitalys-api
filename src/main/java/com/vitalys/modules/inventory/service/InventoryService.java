package com.vitalys.modules.inventory.service;

import com.vitalys.modules.inventory.dto.*;
import com.vitalys.modules.inventory.entity.*;
import com.vitalys.modules.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryLotRepository lotRepository;
    private final InventoryUsageLogRepository usageLogRepository;
    private final PreparationRecordRepository preparationRecordRepository;
    private final PreparationInputRepository preparationInputRepository;

    // ── Item Catalog Management ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> searchItems(String category, String status, String search, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        return itemRepository.searchItems(cleanSearch, cleanCategory, cleanStatus, pageable)
                .map(this::mapToItemResponse);
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getItemById(Long id) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa chất / vật tư với ID: " + id));
        return mapToItemResponse(item);
    }

    @Transactional
    public InventoryItemResponse createItem(InventoryItemCreateRequest req) {
        String code = req.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        InventoryItem item = InventoryItem.builder()
                .name(req.getName())
                .code(code)
                .category(req.getCategory() != null ? req.getCategory() : "CHEMICAL")
                .casNumber(req.getCasNumber())
                .grade(req.getGrade())
                .storageCondition(req.getStorageCondition())
                .safetyHazard(req.getSafetyHazard() != null ? req.getSafetyHazard() : "NONE")
                .unit(req.getUnit() != null ? req.getUnit() : "g")
                .reorderLevel(req.getReorderLevel() != null ? req.getReorderLevel() : "100")
                .currentStock(req.getCurrentStock() != null ? req.getCurrentStock() : 0.0)
                .status("ACTIVE")
                .build();

        item = itemRepository.save(item);
        log.info("Created Inventory Item [{}] {}", item.getCode(), item.getName());
        return mapToItemResponse(item);
    }

    @Transactional
    public InventoryItemResponse updateItem(Long id, InventoryItemCreateRequest req) {
        InventoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy vật tư ID: " + id));

        item.setName(req.getName());
        if (req.getCode() != null) item.setCode(req.getCode());
        if (req.getCategory() != null) item.setCategory(req.getCategory());
        if (req.getCasNumber() != null) item.setCasNumber(req.getCasNumber());
        if (req.getGrade() != null) item.setGrade(req.getGrade());
        if (req.getStorageCondition() != null) item.setStorageCondition(req.getStorageCondition());
        if (req.getSafetyHazard() != null) item.setSafetyHazard(req.getSafetyHazard());
        if (req.getUnit() != null) item.setUnit(req.getUnit());
        if (req.getReorderLevel() != null) item.setReorderLevel(req.getReorderLevel());

        item = itemRepository.save(item);
        return mapToItemResponse(item);
    }

    // ── Lot Tracking & Expiry Alerts ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<InventoryLotResponse> searchLots(Long itemId, String status, String search, Pageable pageable) {
        return lotRepository.searchLots(itemId, status, search, pageable)
                .map(this::mapToLotResponse);
    }

    @Transactional(readOnly = true)
    public InventoryLotResponse getLotById(Long id) {
        InventoryLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lô hóa chất ID: " + id));
        return mapToLotResponse(lot);
    }

    @Transactional
    public InventoryLotResponse createLot(InventoryLotCreateRequest req) {
        InventoryItem item = itemRepository.findById(req.getInventoryItemId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục hóa chất ID: " + req.getInventoryItemId()));

        Double qty = req.getQuantity() != null ? req.getQuantity() : 0.0;

        InventoryLot lot = InventoryLot.builder()
                .inventoryItemId(item.getId())
                .lotNumber(req.getLotNumber())
                .manufacturer(req.getManufacturer())
                .expiryDate(req.getExpiryDate())
                .receivedDate(OffsetDateTime.now())
                .initialQuantity(qty)
                .quantityRemaining(qty)
                .unit(req.getUnit() != null ? req.getUnit() : item.getUnit())
                .coaAvailable(req.getCoaAvailable() != null ? req.getCoaAvailable() : true)
                .status("AVAILABLE")
                .build();

        lot = lotRepository.save(lot);

        // Update item total current stock
        Double current = item.getCurrentStock() != null ? item.getCurrentStock() : 0.0;
        item.setCurrentStock(current + qty);
        itemRepository.save(item);

        log.info("Registered Inventory Lot [{}] for Item [{}] with qty {}", lot.getLotNumber(), item.getCode(), qty);
        return mapToLotResponse(lot);
    }

    @Transactional
    public InventoryLotResponse deductUsage(InventoryUsageRequest req, String username) {
        InventoryLot lot = lotRepository.findById(req.getLotId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lô ID: " + req.getLotId()));

        if (lot.getQuantityRemaining() == null || lot.getQuantityRemaining() < req.getQuantityUsed()) {
            throw new IllegalArgumentException(String.format(
                    "Lượng tồn kho của lô [%s] không đủ (Còn: %s, Yêu cầu trừ: %s)",
                    lot.getLotNumber(), lot.getQuantityRemaining(), req.getQuantityUsed()));
        }

        // Deduct quantity
        double remaining = lot.getQuantityRemaining() - req.getQuantityUsed();
        lot.setQuantityRemaining(remaining);

        if (remaining <= 0) {
            lot.setStatus("DEPLETED");
        } else if (lot.getInitialQuantity() != null && remaining < (lot.getInitialQuantity() * 0.1)) {
            lot.setStatus("LOW_STOCK");
        }
        lotRepository.save(lot);

        // Update item total stock
        InventoryItem item = itemRepository.findById(lot.getInventoryItemId()).orElse(null);
        if (item != null) {
            double itemStock = item.getCurrentStock() != null ? item.getCurrentStock() : 0.0;
            item.setCurrentStock(Math.max(0.0, itemStock - req.getQuantityUsed()));
            itemRepository.save(item);
        }

        // Write audit log
        InventoryUsageLog usageLog = InventoryUsageLog.builder()
                .inventoryLotId(lot.getId())
                .sourceType(req.getSourceType() != null ? req.getSourceType() : "TEST_EXECUTION")
                .sourceId(req.getSourceId())
                .quantityUsed(req.getQuantityUsed())
                .balanceAfter(String.valueOf(remaining))
                .usedBy(username != null ? username : "analyst")
                .usedAt(OffsetDateTime.now())
                .build();
        usageLogRepository.save(usageLog);

        log.info("Deducted {} {} from lot [{}]. Remaining: {}",
                req.getQuantityUsed(), lot.getUnit(), lot.getLotNumber(), remaining);

        return mapToLotResponse(lot);
    }

    @Transactional(readOnly = true)
    public List<InventoryLotResponse> getExpiringLots(int daysAhead) {
        OffsetDateTime threshold = OffsetDateTime.now().plusDays(daysAhead);
        return lotRepository.findLotsExpiringBefore(threshold).stream()
                .map(this::mapToLotResponse)
                .collect(Collectors.toList());
    }

    // ── Solution Preparation Records ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<PreparationRecordResponse> searchPreparationRecords(String search, Pageable pageable) {
        return preparationRecordRepository.searchRecords(search, pageable)
                .map(this::mapToPreparationResponse);
    }

    @Transactional
    public PreparationRecordResponse createPreparationRecord(PreparationRecordCreateRequest req, String username) {
        // Validate and deduct input lots
        if (req.getInputs() == null || req.getInputs().isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một hóa chất / chất chuẩn để pha chế dung dịch.");
        }

        PreparationRecord record = PreparationRecord.builder()
                .solutionName(req.getSolutionName())
                .methodId(req.getMethodId())
                .sopReference(req.getSopReference())
                .preparedBy(username != null ? username : "operator")
                .preparedAt(OffsetDateTime.now())
                .expiryAt(req.getExpiryAt() != null ? req.getExpiryAt() : OffsetDateTime.now().plusDays(7))
                .targetVolume(req.getTargetVolume())
                .unit(req.getUnit() != null ? req.getUnit() : "mL")
                .status("APPROVED")
                .notes(req.getNotes())
                .build();

        record = preparationRecordRepository.save(record);

        // Process inputs
        for (PreparationRecordCreateRequest.PreparationInputCreateDto inputDto : req.getInputs()) {
            InventoryLot sourceLot = lotRepository.findById(inputDto.getSourceLotId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lô hóa chất gốc ID: " + inputDto.getSourceLotId()));

            // Deduct from source lot
            deductUsage(InventoryUsageRequest.builder()
                    .lotId(sourceLot.getId())
                    .quantityUsed(inputDto.getQuantityUsed())
                    .sourceType("PREPARATION")
                    .sourceId(record.getId())
                    .reason("Pha chế: " + record.getSolutionName())
                    .build(), username);

            PreparationInput input = PreparationInput.builder()
                    .preparationId(record.getId())
                    .sourceLotId(sourceLot.getId())
                    .quantityUsed(inputDto.getQuantityUsed())
                    .unit(inputDto.getUnit() != null ? inputDto.getUnit() : sourceLot.getUnit())
                    .build();
            preparationInputRepository.save(input);
        }

        log.info("Created Preparation Record [{}] for solution '{}' by {}", record.getId(), record.getSolutionName(), username);
        return mapToPreparationResponse(record);
    }

    // ── Mapping Helpers ───────────────────────────────────────────────────────

    private InventoryItemResponse mapToItemResponse(InventoryItem i) {
        return InventoryItemResponse.builder()
                .id(i.getId())
                .departmentId(i.getDepartmentId())
                .name(i.getName())
                .code(i.getCode())
                .category(i.getCategory())
                .casNumber(i.getCasNumber())
                .grade(i.getGrade())
                .storageCondition(i.getStorageCondition())
                .safetyHazard(i.getSafetyHazard())
                .unit(i.getUnit())
                .reorderLevel(i.getReorderLevel())
                .currentStock(i.getCurrentStock())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .build();
    }

    private InventoryLotResponse mapToLotResponse(InventoryLot l) {
        InventoryItem item = itemRepository.findById(l.getInventoryItemId()).orElse(null);
        OffsetDateTime now = OffsetDateTime.now();
        boolean isExpired = l.getExpiryDate() != null && l.getExpiryDate().isBefore(now);
        boolean isExpiringSoon = l.getExpiryDate() != null && !isExpired && l.getExpiryDate().isBefore(now.plusDays(30));

        return InventoryLotResponse.builder()
                .id(l.getId())
                .inventoryItemId(l.getInventoryItemId())
                .itemName(item != null ? item.getName() : "Unknown")
                .itemCode(item != null ? item.getCode() : "N/A")
                .lotNumber(l.getLotNumber())
                .manufacturer(l.getManufacturer())
                .expiryDate(l.getExpiryDate())
                .receivedDate(l.getReceivedDate())
                .initialQuantity(l.getInitialQuantity())
                .quantityRemaining(l.getQuantityRemaining())
                .unit(l.getUnit())
                .coaAvailable(l.getCoaAvailable())
                .status(isExpired ? "EXPIRED" : l.getStatus())
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .build();
    }

    private PreparationRecordResponse mapToPreparationResponse(PreparationRecord p) {
        List<PreparationInput> inputs = preparationInputRepository.findByPreparationId(p.getId());
        List<PreparationRecordResponse.PreparationInputDto> inputDtos = inputs.stream()
                .map(in -> {
                    InventoryLot lot = lotRepository.findById(in.getSourceLotId()).orElse(null);
                    InventoryItem item = lot != null ? itemRepository.findById(lot.getInventoryItemId()).orElse(null) : null;
                    return PreparationRecordResponse.PreparationInputDto.builder()
                            .id(in.getId())
                            .sourceLotId(in.getSourceLotId())
                            .sourceLotNumber(lot != null ? lot.getLotNumber() : "N/A")
                            .itemName(item != null ? item.getName() : "N/A")
                            .quantityUsed(in.getQuantityUsed())
                            .unit(in.getUnit())
                            .build();
                })
                .collect(Collectors.toList());

        return PreparationRecordResponse.builder()
                .id(p.getId())
                .solutionName(p.getSolutionName())
                .resultLotId(p.getResultLotId())
                .sopReference(p.getSopReference())
                .preparedBy(p.getPreparedBy())
                .preparedAt(p.getPreparedAt())
                .expiryAt(p.getExpiryAt())
                .targetVolume(p.getTargetVolume())
                .unit(p.getUnit())
                .status(p.getStatus())
                .notes(p.getNotes())
                .inputs(inputDtos)
                .build();
    }
}
