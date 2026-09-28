package com.vitalys.modules.integration.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "integration_inbound_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_order_id", nullable = false, length = 100)
    private String externalOrderId;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem; // e.g. SAP_S4HANA, WERUM_MES, ORACLE_ERP

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "order_type", nullable = false, length = 50)
    private String orderType; // RELEASE, RAW_MATERIAL, STABILITY, IN_PROCESS

    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "NORMAL";

    @Column(name = "requested_tests", columnDefinition = "TEXT")
    private String requestedTests;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSED, FAILED, COMPLETED

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "test_request_id")
    private Long testRequestId;

    @Column(name = "received_at", nullable = false)
    @Builder.Default
    private OffsetDateTime receivedAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
