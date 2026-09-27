package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Actual step execution
 */
@Entity
@Table(name = "testing_test_step_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestStepExecution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "method_step_item_id")
    private Long methodStepItemId;

    @Column(name = "inventory_lot_id")
    private Long inventoryLotId;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "actual_quantity")
    private Double actualQuantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "performed_at")
    private OffsetDateTime performedAt;


}
