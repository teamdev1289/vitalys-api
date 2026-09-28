package com.vitalys.modules.stability.dto;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityStudyResponse {
    private Long id;
    private String studyCode;
    private String studyTitle;
    private Long productId;
    private String productCode;
    private String productName;
    private Long batchId;
    private String batchNumber;
    private String studyType;
    private String protocolNumber;
    private Integer durationMonths;
    private OffsetDateTime startDate;
    private String status;
    private String createdBy;
    private String notes;
    private OffsetDateTime createdAt;

    private List<StorageConditionDto> conditions;
    private List<TimePointDto> timePoints;
    private Integer totalPullEvents;
    private Integer completedPullEvents;
    private Integer pendingPullEvents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StorageConditionDto {
        private Long id;
        private String chamberName;
        private Double temperatureCelsius;
        private Double temperatureTolerance;
        private Double relativeHumidity;
        private Double humidityTolerance;
        private String lightCondition;
        private String shelfLocation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimePointDto {
        private Long id;
        private String pointLabel;
        private Integer monthOffset;
        private Integer toleranceDays;
        private String testRegime;
    }
}
