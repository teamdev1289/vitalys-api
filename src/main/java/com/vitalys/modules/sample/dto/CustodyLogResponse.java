package com.vitalys.modules.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyLogResponse {

    private Long id;
    private Long sampleId;
    private String sampleCode;
    private String fromUser;
    private String toUser;
    private String fromLocation;
    private String toLocation;
    private OffsetDateTime transferredAt;
    private String purpose;
    private String sampleCondition;
    private String storageCondition;
    private String signatureToken;
    private String notes;
}
