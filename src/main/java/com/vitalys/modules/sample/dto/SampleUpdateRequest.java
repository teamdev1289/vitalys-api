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
public class SampleUpdateRequest {

    private Long departmentId;
    private Long specSetId;
    private String storageCondition;
    private String currentLocation;
    private String assignedTo;
    private Double quantity;
    private String unit;
    private OffsetDateTime samplingDate;
    private String samplingLocation;
    private String notes;
}
