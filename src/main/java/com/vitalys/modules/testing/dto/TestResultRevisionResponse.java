package com.vitalys.modules.testing.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultRevisionResponse {
    private Long id;
    private Long resultId;
    private Double oldValue;
    private Double newValue;
    private String revisedBy;
    private OffsetDateTime revisedAt;
    private String reason;
}
