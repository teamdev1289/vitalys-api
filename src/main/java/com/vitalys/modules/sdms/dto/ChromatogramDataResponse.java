package com.vitalys.modules.sdms.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Time-series data points and peak annotations payload for InSpector 2D interactive viewer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChromatogramDataResponse {
    private Long fileId;
    private String fileCode;
    private String originalFilename;
    private String fileType;
    private String xLabel; // e.g. "Thời gian lưu (Phút) / Retention Time (min)"
    private String yLabel; // e.g. "Cường độ tín hiệu / Signal Intensity (mAU)"
    private String signalUnit; // mAU, Absorbance, mV
    private List<List<Double>> datapoints; // [ [time1, intensity1], [time2, intensity2], ... ]
    private List<PeakResponse> peaks;
    private Map<String, String> metadata;
}
