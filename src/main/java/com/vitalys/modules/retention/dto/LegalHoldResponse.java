package com.vitalys.modules.retention.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalHoldResponse {
    private Long id;
    private String holdCode;
    private String caseTitle;
    private String caseReference;
    private String targetModule;
    private Long targetEntityId;
    private String entityCode;
    private String status;
    private String placedBy;
    private OffsetDateTime placedAt;
    private String releasedBy;
    private OffsetDateTime releasedAt;
    private String reason;
}
