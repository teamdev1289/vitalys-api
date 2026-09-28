package com.vitalys.modules.equipment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstrumentResponse {

    private Long id;
    private Long departmentId;
    private String departmentName;
    private String name;
    private String model;
    private String assetCode;
    private String serialNumber;
    private String manufacturer;
    private String location;
    private String status;
    private LocalDate purchaseDate;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Information about the latest calibration and upcoming due date
     */
    private OffsetDateTime lastCalibrationDate;
    private String nextCalibrationDue;
}
