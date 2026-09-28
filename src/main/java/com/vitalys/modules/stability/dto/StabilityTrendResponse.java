package com.vitalys.modules.stability.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityTrendResponse {
    private Long studyId;
    private String studyCode;
    private String productName;
    private String batchNumber;
    private Long conditionId;
    private String chamberName;
    private String conditionLabel;

    private Double specMin;
    private Double specMax;

    private List<DataPoint> assayPoints;
    private List<DataPoint> dissolutionPoints;

    // Linear Regression y = slope * x + intercept
    private Double slope;
    private Double intercept;
    private Double rSquared;
    private Double shelfLifeEstimatedMonths;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataPoint {
        private Integer monthOffset;
        private String pointLabel;
        private Double value;
        private String pullDate;
        private String status;
    }
}
