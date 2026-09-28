package com.vitalys.modules.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderResponse {
    private Long id;
    private String externalOrderId;
    private String sourceSystem;
    private String batchNumber;
    private String productCode;
    private String orderType;
    private String priority;
    private String requestedTests;
    private String status;
    private Long sampleId;
    private Long testRequestId;
    private OffsetDateTime receivedAt;
    private OffsetDateTime processedAt;
    private String notes;
    private OffsetDateTime createdAt;
}
