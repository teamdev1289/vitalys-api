package com.vitalys.modules.sdms.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Analytical peak integration result extracted from a chromatogram or spectrum file.
 */
@Entity
@Table(name = "sdms_chromatogram_peak")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChromatogramPeak extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_file_id", nullable = false)
    private Long dataFileId;

    @Column(name = "peak_number", nullable = false)
    private Integer peakNumber;

    @Column(name = "compound_name", length = 150)
    private String compoundName;

    @Column(name = "retention_time", nullable = false)
    private Double retentionTime; // Retention Time (tR) in minutes or Lambda in nm

    @Column(name = "peak_area", nullable = false)
    private Double peakArea;

    @Column(name = "peak_height", nullable = false)
    private Double peakHeight;

    @Column(name = "area_percent")
    private Double areaPercent;

    @Column(name = "theoretical_plates")
    private Integer theoreticalPlates;

    @Column(name = "tailing_factor")
    private Double tailingFactor;

    @Column(name = "resolution")
    private Double resolution;
}
