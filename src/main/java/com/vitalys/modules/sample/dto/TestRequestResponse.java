package com.vitalys.modules.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequestResponse {

    private Long id;
    private String requestCode;
    private String sourceType;
    private String sampleType;

    private Long productId;
    private String productCode;
    private String productName;

    private Long batchId;
    private String batchNumber;

    private String processStage;
    private Long formulationTrialId;

    private Long customerId;
    private String customerName;

    private Long projectId;
    private String projectCode;

    private Long specSetId;
    private String specCode;
    private String specSetName;

    private String requestedBy;
    private OffsetDateTime requestDate;
    private OffsetDateTime dueDate;
    private String priority;
    private String testScope;
    private String status;

    private int sampleCount;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
