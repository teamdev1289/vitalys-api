package com.vitalys.modules.retention.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicyUpdateRequest {

    @NotNull(message = "Retention years is required")
    @Min(value = 1, message = "Retention years must be at least 1")
    private Integer retentionYears;

    private Boolean isPermanent;

    private String description;

    private Boolean autoArchive;
}
