package com.vitalys.modules.method.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodValidationProtocolRequest {

    @NotBlank(message = "Mã đề cương thẩm định không được để trống")
    private String protocolCode;

    @NotBlank(message = "Tiêu đề đề cương không được để trống")
    private String title;

    private String validationType;
    private String triggerReason;
    private String status;
    private String approvedBy;
    private OffsetDateTime approvedDate;
    private String description;
    private String conclusion;
}
