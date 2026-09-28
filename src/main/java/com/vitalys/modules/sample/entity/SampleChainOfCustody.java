package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Immutable Chain of Custody (CoC) audit log tracking every handover,
 * storage transfer, sample condition, and recipient verification.
 */
@Entity
@Table(name = "sample_chain_of_custody")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SampleChainOfCustody extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id", nullable = false)
    private Long sampleId;

    @Column(name = "from_user", length = 100)
    private String fromUser;

    @Column(name = "to_user", nullable = false, length = 100)
    private String toUser;

    @Column(name = "from_location", length = 255)
    private String fromLocation;

    @Column(name = "to_location", nullable = false, length = 255)
    private String toLocation;

    @Column(name = "transferred_at", nullable = false)
    private OffsetDateTime transferredAt;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Column(name = "sample_condition", length = 100)
    private String sampleCondition; // INTACT, SEAL_BROKEN, CONTAINER_DAMAGED, TEMP_EXCURSION

    @Column(name = "storage_condition", length = 100)
    private String storageCondition;

    @Column(name = "signature_token", length = 255)
    private String signatureToken;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
