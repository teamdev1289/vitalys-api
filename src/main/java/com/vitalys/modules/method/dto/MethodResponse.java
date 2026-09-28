package com.vitalys.modules.method.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodResponse {

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
    private Integer stepCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
