package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Record of preparation
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

    @Column(name = "result_lot_id")
    private Long resultLotId;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "prepared_by")
    private String preparedBy;

    @Column(name = "prepared_at")
    private OffsetDateTime preparedAt;

    @Column(name = "expiry_at")
    private OffsetDateTime expiryAt;

    @Column(name = "status")
    private String status;


}
