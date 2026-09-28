package com.vitalys.modules.testing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAssignmentRequest {

    @NotNull(message = "Sample ID is required")
    private Long sampleId;

    @NotNull(message = "Method (SOP) ID is required")
    private Long methodId;

    private Long specItemId;

    private Long formTemplateId;

    @NotBlank(message = "Assigned analyst username is required")
    private String assignedTo;

    @Builder.Default
    private String priority = "NORMAL";

    private OffsetDateTime dueDate;

    private String notes;
}
