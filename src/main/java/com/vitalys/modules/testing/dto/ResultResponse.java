package com.vitalys.modules.testing.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultResponse {
    private Long id;
    private Long testId;
    private String analyte;
    private Double value;
    private String textValue;
    private String unit;
    private Double specMin;
    private Double specMax;
    private String specTarget;
    private String passFail;
    private Boolean isOos;
    private Long oosInvestigationId;
    private String enteredBy;
    private OffsetDateTime enteredAt;
    private List<TestResultRevisionResponse> revisions;
}
