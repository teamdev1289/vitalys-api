package com.vitalys.modules.method.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodDetailResponse {

    private Long id;
    private Long departmentId;
    private String methodCode;
    private String name;
    private String version;
    private String category;
    private String instrumentType;
    private String sourceStandard;
    private String validationStatus;
    private Boolean isActive;
    private OffsetDateTime effectiveDate;
    private OffsetDateTime reviewDueDate;
    private String description;
    private String bodyTemplate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder.Default
    private List<MethodStepResponse> steps = new ArrayList<>();

    @Builder.Default
    private List<MethodValidationProtocolResponse> validationProtocols = new ArrayList<>();
}
