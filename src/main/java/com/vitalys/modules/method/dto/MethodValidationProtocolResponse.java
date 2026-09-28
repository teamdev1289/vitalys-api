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
public class MethodValidationProtocolResponse {

    private Long id;
    private Long methodId;
    private String protocolCode;
    private String title;
    private String validationType;
    private String triggerReason;
    private String status;
    private String approvedBy;
    private OffsetDateTime approvedDate;
    private String description;
    private String conclusion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
