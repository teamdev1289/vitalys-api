package com.vitalys.modules.executive.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveDashboardMetrics {

    // ── 1. Compliance KPIs (Tuân thủ & Chất lượng) ──────────────────────────
    private Long totalSamplesCount;
    private Long totalTestsCount;
    private Long totalCoasSigned;
    private Long oosCount;
    private Double oosRatePercent;
    private Double firstTimeRightPercent; // First Time Right (FTR)
    private Double averageTatDays;         // Turnaround Time (TAT)
    private Long activeLegalHoldsCount;

    // ── 2. Operational KPIs (Hiệu suất vận hành phòng Lab) ──────────────────
    private Long totalInstruments;
    private Long instrumentsInService;
    private Double instrumentUtilizationRate;
    private Long expiringChemicalsCount;
    private Long activeStabilityStudiesCount;
    private Long workloadBacklogCount;

    // ── 3. Chart Series Collections ─────────────────────────────────────────
    private List<MonthlyThroughputDto> monthlyThroughput;
    private List<OosCategoryBreakdownDto> oosCategories;
    private List<InstrumentStatusDistributionDto> instrumentStatusDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyThroughputDto {
        private String monthLabel;
        private Long samplesReceived;
        private Long testsCompleted;
        private Long coasIssued;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OosCategoryBreakdownDto {
        private String category;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstrumentStatusDistributionDto {
        private String status;
        private Long count;
        private String label;
    }
}
