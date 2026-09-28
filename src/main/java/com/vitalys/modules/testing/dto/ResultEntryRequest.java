package com.vitalys.modules.testing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultEntryRequest {

    @NotNull(message = "Test ID is required")
    private Long testId;

    private String analyte;

    private Double value;

    private String textValue;

    private String unit;

    private Long runId;

    /**
     * Raw dynamic form field submissions from FormTemplate
     */
    private Map<String, Object> formData;
}
