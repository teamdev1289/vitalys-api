package com.vitalys.modules.sdms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataFileLinkRequest {
    private Long sampleId;
    private Long testId;
    private Long runId;
    private Long instrumentId;
}
