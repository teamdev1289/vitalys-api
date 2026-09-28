package com.vitalys.modules.testing.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticalRunResponse {
    private Long id;
    private String runCode;
    private String name;
    private Long instrumentId;
    private String instrumentName;
    private String instrumentAssetCode;
    private OffsetDateTime runDate;
    private String analyst;
    private String status;
    private Integer batchSize;
    private String notes;
    private OffsetDateTime createdAt;
}
