package com.vitalys.modules.sample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequestCreateRequest {

    @NotBlank(message = "Source type is required (BATCH, CUSTOMER, FORMULATION_TRIAL, STABILITY)")
    private String sourceType;

    @NotBlank(message = "Sample type is required (FINISHED_PRODUCT, RAW_MATERIAL, IN_PROCESS, STABILITY)")
    private String sampleType;

    private Long productId;
    private Long batchId;
    private String processStage;
    private Long formulationTrialId;
    private Long customerId;
    private Long projectId;
    private Long specSetId;

    @NotNull(message = "Due date is required")
    private OffsetDateTime dueDate;

    @NotBlank(message = "Priority is required (NORMAL, URGENT, EMERGENCY)")
    private String priority;

    @NotBlank(message = "Test scope is required (RELEASE_TESTING, STABILITY_PULL_TESTING, FULL_COMPENDIAL)")
    private String testScope;

    private String notes;
}
