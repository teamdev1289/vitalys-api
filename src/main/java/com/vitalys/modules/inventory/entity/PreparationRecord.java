package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Record of chemical/reagent solution preparation
 */
@Entity
@Table(name = "inventory_preparation_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solution_name", nullable = false)
    private String solutionName;

    @Column(name = "result_lot_id")
    private Long resultLotId;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "sop_reference")
    private String sopReference;

    @Column(name = "prepared_by")
    private String preparedBy;

    @Column(name = "prepared_at")
    private OffsetDateTime preparedAt;

    @Column(name = "expiry_at")
    private OffsetDateTime expiryAt;

    @Column(name = "target_volume")
    private Double targetVolume;

    @Column(name = "unit")
    private String unit;

    @Column(name = "status")
    @Builder.Default
    private String status = "APPROVED"; // DRAFT, APPROVED, EXPIRED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
