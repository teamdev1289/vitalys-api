package com.vitalys.modules.sample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleAccessionRequest {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    private Long departmentId;
    private Long specSetId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    @NotBlank(message = "Unit is required (TABLETS, CAPSULES, AMPOULES, ML, GRAMS)")
    private String unit;

    @NotBlank(message = "Storage condition is required (AMBIENT_15_25C, COLD_2_8C, FREEZER_MINUS_20C)")
    private String storageCondition;

    @NotBlank(message = "Initial location is required (e.g., Sample Accessioning Bench, Cold Room 1)")
    private String currentLocation;

    private OffsetDateTime samplingDate;
    private String samplingLocation;
    private String assignedTo;
    private String notes;

    /**
     * How many physical sample containers/units to log in (default 1).
     */
    @Builder.Default
    private int containerCount = 1;
}
