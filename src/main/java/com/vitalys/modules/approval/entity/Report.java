package com.vitalys.modules.approval.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Official Certificate of Analysis (COA) Report Entity.
 */
@Entity
@Table(name = "approval_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id", nullable = false)
    private Long sampleId;

    @Column(name = "report_code", unique = true)
    private String reportCode; // e.g. COA-20260901-0001

    @Column(name = "version")
    private String version; // e.g. "1.0"

    @Column(name = "status")
    private String status; // DRAFT, APPROVED, SUPERSEDED, REVOKED

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "conclusion")
    private String conclusion; // "ĐẠT TIÊU CHUẨN DƯỢC ĐIỂN VIỆT NAM V"

    @Column(name = "qr_code_data", columnDefinition = "TEXT")
    private String qrCodeData;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "generated_by")
    private String generatedBy;
}
