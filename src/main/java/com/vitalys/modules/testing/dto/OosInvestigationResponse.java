package com.vitalys.modules.testing.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OosInvestigationResponse {
    private Long id;
    private String investigationCode;
    private Long resultId;
    private Double resultValue;
    private String resultTextValue;
    private Double specMin;
    private Double specMax;
    private String specTarget;
    private String unit;
    private Long testId;
    private String testCode;
    private Long sampleId;
    private String sampleCode;
    private String productName;
    private String batchNumber;
    private String phase;
    private String rootCauseCategory;
    private String immediateAction;
    private String investigationFindings;
    private String investigatedBy;
    private OffsetDateTime investigatedAt;
    private String supervisorReviewedBy;
    private String supervisorComments;
    private Boolean retestApproved;
    private String conclusion;
    private String status;
    private OffsetDateTime createdAt;
}
