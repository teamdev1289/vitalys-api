package com.vitalys.modules.testing.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponse {
    private Long id;
    private String testCode;
    private Long sampleId;
    private String sampleCode;
    private String barcode;
    private String productName;
    private String batchNumber;
    private Long methodId;
    private String methodCode;
    private String methodName;
    private Long specItemId;
    private String specParameter;
    private Double specMin;
    private Double specMax;
    private String specTarget;
    private String specUnit;
    private Long runId;
    private String runCode;
    private Long formTemplateId;
    private String assignedTo;
    private String priority;
    private OffsetDateTime dueDate;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private String status;
    private String notes;
    private List<ResultResponse> results;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
