package com.vitalys.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemCreateRequest {
    @NotBlank(message = "Item name is required")
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
}
