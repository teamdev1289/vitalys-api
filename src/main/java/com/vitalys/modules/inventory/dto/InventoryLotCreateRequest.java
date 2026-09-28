package com.vitalys.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLotCreateRequest {
    @NotNull(message = "Inventory item ID is required")
    private Long inventoryItemId;

    @NotBlank(message = "Lot number is required")
    private String lotNumber;

    private String manufacturer;
    private OffsetDateTime expiryDate;
    private Double quantity;
    private String unit;
    private Boolean coaAvailable;
}
