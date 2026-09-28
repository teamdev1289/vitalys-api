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
public class SampleStatusChangeRequest {

    @NotBlank(message = "Target status is required")
    private String targetStatus;

    @NotBlank(message = "GxP Reason for status change is mandatory")
    private String reason;
}
