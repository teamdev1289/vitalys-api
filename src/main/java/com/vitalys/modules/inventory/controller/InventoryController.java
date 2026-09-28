package com.vitalys.modules.inventory.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.inventory.dto.*;
import com.vitalys.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ── Inventory Items Catalog ────────────────────────────────────────────────

    @GetMapping("/items")
    @PreAuthorize("hasAuthority('INVENTORY:ITEM:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<Page<InventoryItemResponse>>> searchItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InventoryItemResponse> res = inventoryService.searchItems(category, status, search, pageable);
        return ResponseEntity.ok(ResponseDto.ok(res));
    }

    @GetMapping("/items/{id}")
    @PreAuthorize("hasAuthority('INVENTORY:ITEM:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<InventoryItemResponse>> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(inventoryService.getItemById(id)));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('INVENTORY:ITEM:CREATE') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<InventoryItemResponse>> createItem(
            @Valid @RequestBody InventoryItemCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created(inventoryService.createItem(req)));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('INVENTORY:ITEM:UPDATE') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<InventoryItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemCreateRequest req) {
        return ResponseEntity.ok(ResponseDto.ok(inventoryService.updateItem(id, req)));
    }

    // ── Inventory Lots & Stock Deduction ──────────────────────────────────────

    @GetMapping("/lots")
    @PreAuthorize("hasAuthority('INVENTORY:LOT:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<Page<InventoryLotResponse>>> searchLots(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(inventoryService.searchLots(itemId, status, search, pageable)));
    }

    @GetMapping("/lots/{id}")
    @PreAuthorize("hasAuthority('INVENTORY:LOT:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<InventoryLotResponse>> getLotById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseDto.ok(inventoryService.getLotById(id)));
    }

    @PostMapping("/lots")
    @PreAuthorize("hasAuthority('INVENTORY:LOT:CREATE') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<InventoryLotResponse>> createLot(
            @Valid @RequestBody InventoryLotCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created(inventoryService.createLot(req)));
    }

    @PostMapping("/lots/deduct")
    @PreAuthorize("hasAuthority('INVENTORY:LOT:DEDUCT') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<InventoryLotResponse>> deductLotUsage(
            @Valid @RequestBody InventoryUsageRequest req,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "operator";
        return ResponseEntity.ok(ResponseDto.ok("Đã trừ tồn kho thành công", inventoryService.deductUsage(req, username)));
    }

    @GetMapping("/lots/expiring")
    @PreAuthorize("hasAuthority('INVENTORY:LOT:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<List<InventoryLotResponse>>> getExpiringLots(
            @RequestParam(defaultValue = "30") int daysAhead) {
        return ResponseEntity.ok(ResponseDto.ok(inventoryService.getExpiringLots(daysAhead)));
    }

    // ── Solution Preparation Records ──────────────────────────────────────────

    @GetMapping("/preparations")
    @PreAuthorize("hasAuthority('INVENTORY:PREP:READ') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<Page<PreparationRecordResponse>>> searchPreparations(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ok(inventoryService.searchPreparationRecords(search, pageable)));
    }

    @PostMapping("/preparations")
    @PreAuthorize("hasAuthority('INVENTORY:PREP:CREATE') or hasRole('IT_ADMIN')")
    public ResponseEntity<ResponseDto<PreparationRecordResponse>> createPreparation(
            @Valid @RequestBody PreparationRecordCreateRequest req,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "operator";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Đã tạo biên bản pha chế dung dịch", inventoryService.createPreparationRecord(req, username)));
    }
}
