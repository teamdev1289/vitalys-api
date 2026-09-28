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
public class SampleStatusHistoryResponse {

    private Long id;
    private Long sampleId;
    private String fromStatus;
    private String toStatus;
    private String changedBy;
    private OffsetDateTime changedAt;
    private String reason;
}
