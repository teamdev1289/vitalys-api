package com.vitalys.modules.equipment.dto;

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
public class MaintenanceRecordResponse {

    private Long id;
    private Long instrumentId;
    private String instrumentName;
    private String instrumentAssetCode;
    private OffsetDateTime performedAt;
    private String nextDue;
    private String performedBy;
    private String description;
    private OffsetDateTime createdAt;
}
