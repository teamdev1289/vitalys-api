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
public class SampleResponse {

    private Long id;
    private Long requestId;
    private String requestCode;
    private String productName;
    private String batchNumber;

    private Long departmentId;
    private String departmentName;

    private Long specSetId;
    private String specCode;
    private String specSetName;

    private String sampleCode;
    private String barcode;
    private String status;
    private String storageCondition;
    private String currentLocation;

    private Double quantity;
    private String unit;

    private String receivedBy;
    private String assignedTo;
    private OffsetDateTime receivedAt;
    private OffsetDateTime samplingDate;
    private String samplingLocation;

    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
