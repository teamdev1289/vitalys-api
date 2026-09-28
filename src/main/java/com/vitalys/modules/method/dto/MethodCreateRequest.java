package com.vitalys.modules.method.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodCreateRequest {

    @NotBlank(message = "Mã phương pháp thử không được để trống")
    @Size(max = 100, message = "Mã phương pháp thử tối đa 100 ký tự")
    private String methodCode;

    @NotBlank(message = "Tên phương pháp thử / SOP không được để trống")
    @Size(max = 255, message = "Tên phương pháp tối đa 255 ký tự")
    private String name;

    @NotBlank(message = "Phiên bản SOP không được để trống")
    @Size(max = 50, message = "Phiên bản tối đa 50 ký tự")
    private String version;

    private String category;
    private String instrumentType;
    private String sourceStandard;
    private String description;
    private String bodyTemplate;
    private Long departmentId;
    private OffsetDateTime effectiveDate;
    private OffsetDateTime reviewDueDate;

    private List<MethodStepRequest> steps;
}
