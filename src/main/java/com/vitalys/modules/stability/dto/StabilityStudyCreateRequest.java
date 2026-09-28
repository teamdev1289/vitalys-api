package com.vitalys.modules.stability.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityStudyCreateRequest {
    @NotBlank(message = "Study title is required")
    private String studyTitle;

    private Long productId;
    private Long batchId;

    @NotBlank(message = "Study type is required (LONG_TERM, ACCELERATED, INTERMEDIATE)")
    private String studyType;

    private String protocolNumber;

    @NotNull(message = "Duration in months is required")
    private Integer durationMonths;

    @NotNull(message = "Start date is required")
    private OffsetDateTime startDate;

    private String notes;

    @NotEmpty(message = "At least one storage condition is required")
    private List<StorageConditionCreateDto> conditions;

    @NotEmpty(message = "At least one sampling time point is required")
    private List<TimePointCreateDto> timePoints;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StorageConditionCreateDto {
        @NotBlank(message = "Chamber name is required")
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
    public static class TimePointCreateDto {
        @NotBlank(message = "Point label is required (e.g. 0M, 3M, 6M)")
        private String pointLabel;
        private Integer monthOffset;
        private Integer toleranceDays;
        private String testRegime;
    }
}
