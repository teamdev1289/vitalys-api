package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Request for supply
 */
@Entity
@Table(name = "inventory_supply_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "request_date")
    private OffsetDateTime requestDate;

    @Column(name = "status")
    private String status;


}
