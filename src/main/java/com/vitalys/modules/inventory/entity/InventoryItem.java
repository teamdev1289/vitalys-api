package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Inventory item catalog (Chemicals, Solvents, Reference Standards, HPLC Columns)
 */
@Entity
@Table(name = "inventory_inventory_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "category")
    private String category; // REFERENCE_STANDARD, REAGENT_SOLVENT, CHEMICAL, COLUMN, CONSUMABLE

    @Column(name = "cas_number")
    private String casNumber;

    @Column(name = "grade")
    private String grade; // USP Standard, HPLC Grade, AR, etc.

    @Column(name = "storage_condition")
    private String storageCondition;

    @Column(name = "safety_hazard")
    private String safetyHazard; // FLAMMABLE, TOXIC, CORROSIVE, NONE

    @Column(name = "unit")
    private String unit;

    @Column(name = "reorder_level")
    private String reorderLevel;

    @Column(name = "current_stock")
    private Double currentStock;

    @Column(name = "status")
    @Builder.Default
    private String status = "ACTIVE";
}
