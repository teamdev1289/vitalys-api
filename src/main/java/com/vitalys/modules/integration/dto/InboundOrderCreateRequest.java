package com.vitalys.modules.integration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundOrderCreateRequest {

    @NotBlank(message = "External Order ID is required")
    private String externalOrderId;

    private String sourceSystem; // e.g. SAP_S4HANA

    private String batchNumber;

    private String productCode;

    private String orderType; // RELEASE, RAW_MATERIAL, STABILITY, IN_PROCESS

    private String priority; // NORMAL, HIGH, URGENT

    private String requestedTests;

    private String notes;
}
