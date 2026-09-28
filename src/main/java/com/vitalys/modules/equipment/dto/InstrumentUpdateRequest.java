package com.vitalys.modules.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentUpdateRequest {

    private Long departmentId;

    @NotBlank(message = "Instrument name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Model must not exceed 255 characters")
    private String model;

    @NotBlank(message = "Asset code is required")
    @Size(max = 255, message = "Asset code must not exceed 255 characters")
    private String assetCode;

    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    private String serialNumber;

    @Size(max = 150, message = "Manufacturer must not exceed 150 characters")
    private String manufacturer;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    private String status;

    private LocalDate purchaseDate;

    private String notes;
}
