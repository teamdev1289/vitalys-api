package com.vitalys.modules.inventory.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLotResponse {
    private Long id;
    private Long inventoryItemId;
    private String itemName;
    private String itemCode;
    private String lotNumber;
    private String manufacturer;
    private OffsetDateTime expiryDate;
    private OffsetDateTime receivedDate;
    private Double initialQuantity;
    private Double quantityRemaining;
    private String unit;
    private Boolean coaAvailable;
    private String status;
    private Boolean isExpired;
    private Boolean isExpiringSoon; // within 30 days
}
