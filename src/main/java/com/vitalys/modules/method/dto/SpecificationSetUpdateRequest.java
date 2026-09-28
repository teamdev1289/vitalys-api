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
public class SpecificationSetUpdateRequest {

    @NotBlank(message = "Tên bộ tiêu chuẩn không được để trống")
    private String name;

    @NotBlank(message = "Phiên bản không được để trống")
    private String version;

    private String market;
    private String status; // DRAFT, ACTIVE, OBSOLETE
    private String changeReason;
    private OffsetDateTime effectiveDate;
    private OffsetDateTime expiryDate;
}
