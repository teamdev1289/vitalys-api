package com.vitalys.modules.inventory.service;

import com.vitalys.modules.inventory.dto.*;
import com.vitalys.modules.inventory.entity.*;
import com.vitalys.modules.inventory.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private InventoryLotRepository lotRepository;

    @Mock
    private InventoryUsageLogRepository usageLogRepository;

    @Mock
    private PreparationRecordRepository preparationRecordRepository;

    @Mock
    private PreparationInputRepository preparationInputRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem sampleItem;
    private InventoryLot sampleLot;

    @BeforeEach
    void setUp() {
        sampleItem = InventoryItem.builder()
                .id(1L)
                .name("Methanol HPLC Grade")
                .code("SOLV-MEOH-001")
                .category("REAGENT_SOLVENT")
                .unit("mL")
                .currentStock(50000.0)
                .status("ACTIVE")
                .build();

        sampleLot = InventoryLot.builder()
                .id(10L)
                .inventoryItemId(1L)
                .lotNumber("MEOH-2026A")
                .initialQuantity(10000.0)
                .quantityRemaining(5000.0)
                .unit("mL")
                .expiryDate(OffsetDateTime.now().plusMonths(12))
                .status("AVAILABLE")
                .build();
    }

    @Test
    void testCreateInventoryItem() {
        when(itemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> {
            InventoryItem item = invocation.getArgument(0);
            item.setId(2L);
            return item;
        });

        InventoryItemCreateRequest req = InventoryItemCreateRequest.builder()
                .name("Acetonitrile HPLC")
                .category("REAGENT_SOLVENT")
                .casNumber("75-05-8")
                .unit("mL")
                .currentStock(20000.0)
                .build();

        InventoryItemResponse res = inventoryService.createItem(req);

        assertNotNull(res);
        assertEquals("Acetonitrile HPLC", res.getName());
        assertEquals("ACTIVE", res.getStatus());
        verify(itemRepository, times(1)).save(any(InventoryItem.class));
    }

    @Test
    void testDeductLotUsage_Success() {
        when(lotRepository.findById(10L)).thenReturn(Optional.of(sampleLot));
        when(lotRepository.save(any(InventoryLot.class))).thenReturn(sampleLot);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        InventoryUsageRequest req = InventoryUsageRequest.builder()
                .lotId(10L)
                .quantityUsed(500.0)
                .sourceType("TEST_EXECUTION")
                .sourceId(101L)
                .reason("Pha động sắc ký")
                .build();

        InventoryLotResponse res = inventoryService.deductUsage(req, "analyst01");

        assertNotNull(res);
        assertEquals(4500.0, sampleLot.getQuantityRemaining());
        assertEquals(49500.0, sampleItem.getCurrentStock());
        verify(usageLogRepository, times(1)).save(any(InventoryUsageLog.class));
    }

    @Test
    void testDeductLotUsage_InsufficientQuantity() {
        when(lotRepository.findById(10L)).thenReturn(Optional.of(sampleLot));

        InventoryUsageRequest req = InventoryUsageRequest.builder()
                .lotId(10L)
                .quantityUsed(6000.0) // Exceeds 5000.0 remaining
                .build();

        assertThrows(IllegalArgumentException.class, () -> inventoryService.deductUsage(req, "analyst01"));
        verify(usageLogRepository, never()).save(any());
    }
}
