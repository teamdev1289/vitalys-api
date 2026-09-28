package com.vitalys.modules.stability.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityPullEventResponse {
    private Long id;
    private Long studyId;
    private String studyCode;
    private String studyTitle;
    private Long timePointId;
    private String pointLabel;
    private Integer monthOffset;
    private Long storageConditionId;
    private String chamberName;
    private Double temperatureCelsius;
    private Double relativeHumidity;
    private LocalDate scheduledDate;
    private LocalDate windowStart;
    private LocalDate windowEnd;
    private OffsetDateTime pullDate;
    private String pulledBy;
    private Long sampleId;
    private String sampleCode;
    private Long testRequestId;
    private String status;
    private Double assayResult;
    private Double dissolutionResult;
    private String notes;
    private Boolean isOverdue;
    private Boolean isDueToday;
}
