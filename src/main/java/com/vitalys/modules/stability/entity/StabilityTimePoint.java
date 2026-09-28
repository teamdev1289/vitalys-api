package com.vitalys.modules.stability.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Sampling time point milestones (0M, 3M, 6M, 9M, 12M, 24M, 36M)
 */
@Entity
@Table(name = "stability_time_point")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityTimePoint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Long studyId;

    @Column(name = "point_label", nullable = false)
    private String pointLabel;

    @Column(name = "month_offset", nullable = false)
    private Integer monthOffset;

    @Column(name = "tolerance_days")
    @Builder.Default
    private Integer toleranceDays = 7;

    @Column(name = "test_regime")
    private String testRegime;
}
