package com.vitalys.modules.retention.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalHoldCreateRequest {

    @NotBlank(message = "Case title is required")
    private String caseTitle;

    @NotBlank(message = "Case reference is required (e.g. FDA-483-2026)")
    private String caseReference;

    @NotBlank(message = "Target module is required (e.g. BATCH, SAMPLE, REPORT, SDMS)")
    private String targetModule;

    @NotNull(message = "Target entity ID is required")
    private Long targetEntityId;

    private String entityCode;

    private String placedBy;

    @NotBlank(message = "Reason for legal hold is required")
    private String reason;
}
