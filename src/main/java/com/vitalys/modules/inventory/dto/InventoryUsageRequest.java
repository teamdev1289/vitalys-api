package com.vitalys.modules.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUsageRequest {
    @NotNull(message = "Inventory lot ID is required")
    private Long lotId;

    @NotNull(message = "Quantity used is required")
    @Positive(message = "Quantity must be positive")
    private Double quantityUsed;

    private String sourceType; // TEST_EXECUTION, PREPARATION, DISPOSAL
    private Long sourceId;     // testId or preparationId
    private String reason;
}
