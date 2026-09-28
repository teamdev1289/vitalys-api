package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Specific lot of inventory item with expiry and remaining stock
 */
@Entity
@Table(name = "inventory_inventory_lot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "lot_number", nullable = false)
    private String lotNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "expiry_date")
    private OffsetDateTime expiryDate;

    @Column(name = "received_date")
    private OffsetDateTime receivedDate;

    @Column(name = "initial_quantity")
    private Double initialQuantity;

    @Column(name = "quantity_remaining")
    private Double quantityRemaining;

    @Column(name = "unit")
    private String unit;

    @Column(name = "coa_available")
    @Builder.Default
    private Boolean coaAvailable = true;

    @Column(name = "status")
    @Builder.Default
    private String status = "AVAILABLE"; // AVAILABLE, LOW_STOCK, EXPIRED, DEPLETED
}
