package com.vitalys.modules.equipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationRecordRequest {

    @NotNull(message = "Instrument ID is required")
    private Long instrumentId;

    @NotNull(message = "Performed at timestamp is required")
    private OffsetDateTime performedAt;

    @NotBlank(message = "Next due date/specification is required")
    @Size(max = 255)
    private String nextDue;

    @NotBlank(message = "Performed by technician/specialist name is required")
    @Size(max = 255)
    private String performedBy;

    @Size(max = 255)
    private String certificateUrl;

    private String notes;
}
