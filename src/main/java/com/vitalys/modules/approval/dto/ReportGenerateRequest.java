package com.vitalys.modules.approval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportGenerateRequest {

    @NotNull(message = "Sample ID is required to generate Certificate of Analysis (COA)")
    private Long sampleId;

    private String conclusion; // Defaults to "ĐẠT TIÊU CHUẨN DƯỢC ĐIỂN VIỆT NAM V" if empty

    private String notes;
}
