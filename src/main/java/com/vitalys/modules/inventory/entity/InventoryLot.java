package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Specific lot of inventory item
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

    @Column(name = "inventory_item_id")
    private Long inventoryItemId;

    @Column(name = "lot_number")
    private String lotNumber;

    @Column(name = "expiry_date")
    private OffsetDateTime expiryDate;

    @Column(name = "quantity_remaining")
    private Double quantityRemaining;


}
