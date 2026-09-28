package com.vitalys.modules.approval.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private Long sampleId;
    private String sampleCode;
    private String productName;
    private String batchNumber;
    private String customerName;
    private String reportCode;
    private String version;
    private String status;
    private String conclusion;
    private String notes;
    private String qrCodeData;
    private String generatedBy;
    private OffsetDateTime createdAt;
    private List<ApprovalStepResponse> signatures;
}
