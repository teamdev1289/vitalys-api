package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Production & QC testing batch.
 */
@Entity
@Table(name = "product_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "formulation_id")
    private Long formulationId;

    @Column(name = "spec_set_id")
    private Long specSetId;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Column(name = "manufacturing_date")
    private OffsetDateTime manufacturingDate;

    @Column(name = "expiry_date")
    private OffsetDateTime expiryDate;

    @Column(name = "quantity_produced")
    private Double quantityProduced;

    @Column(name = "unit")
    @Builder.Default
    private String unit = "TABLETS";

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "QUARANTINE";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
