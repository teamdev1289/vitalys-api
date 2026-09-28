package com.vitalys.modules.sdms.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataFileResponse {
    private Long id;
    private String fileCode;
    private String originalFilename;
    private String fileType;
    private Long fileSize;
    private String checksumSha256;
    private String status;
    private String mimeType;
    private Long instrumentId;
    private String instrumentName;
    private Long runId;
    private Long sampleId;
    private String sampleCode;
    private Long testId;
    private String testCode;
    private String uploadedBy;
    private String notes;
    private OffsetDateTime createdAt;
    private Map<String, String> metadata;
    private List<PeakResponse> peaks;
}
