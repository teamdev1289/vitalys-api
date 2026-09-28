package com.vitalys.modules.stability.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityPullActionRequest {
    private String pulledBy;
    private Double assayResult;
    private Double dissolutionResult;
    private String notes;
    private Boolean createTestRequest;
}
