package com.vitalys.modules.inventory.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponse {
    private Long id;
    private Long departmentId;
    private String name;
    private String code;
    private String category;
    private String casNumber;
    private String grade;
    private String storageCondition;
    private String safetyHazard;
    private String unit;
    private String reorderLevel;
    private Double currentStock;
    private String status;
    private OffsetDateTime createdAt;
}
