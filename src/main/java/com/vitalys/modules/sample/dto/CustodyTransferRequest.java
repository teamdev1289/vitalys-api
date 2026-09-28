package com.vitalys.modules.sample.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyTransferRequest {

    @NotBlank(message = "Recipient username (toUser) is required")
    private String toUser;

    @NotBlank(message = "Destination location is required")
    private String toLocation;

    @NotBlank(message = "Transfer purpose is required")
    private String purpose;

    @NotBlank(message = "Sample condition is required (INTACT, SEAL_BROKEN, CONTAINER_DAMAGED)")
    private String sampleCondition;

    private String storageCondition;
    private String signatureToken;
    private String notes;
}
