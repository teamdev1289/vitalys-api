package com.vitalys.modules.sdms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeakResponse {
    private Long id;
    private Integer peakNumber;
    private String compoundName;
    private Double retentionTime; // Minutes or Wavelength
    private Double peakArea;
    private Double peakHeight;
    private Double areaPercent;
    private Integer theoreticalPlates;
    private Double tailingFactor;
    private Double resolution;
}
