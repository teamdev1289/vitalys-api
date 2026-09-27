package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Log of inventory usage
 */
@Entity
@Table(name = "inventory_inventory_usage_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUsageLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_lot_id")
    private Long inventoryLotId;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "quantity_used")
    private Double quantityUsed;

    @Column(name = "balance_after")
    private String balanceAfter;

    @Column(name = "used_by")
    private String usedBy;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;


}
