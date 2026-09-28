package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Analytical result recorded against an individual test.
 * Supports quantitative (value, unit) and qualitative (textValue) results,
 * compared to specification thresholds with automatic Out of Specification (OOS) detection.
 */
@Entity
@Table(name = "testing_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private Long testId;

    @Column(name = "analyte")
    private String analyte;

    @Column(name = "value")
    private Double value;

    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "spec_min")
    private Double specMin;

    @Column(name = "spec_max")
    private Double specMax;

    @Column(name = "spec_target")
    private String specTarget;

    @Column(name = "pass_fail", length = 50)
    private String passFail; // PASS, FAIL, PENDING

    @Column(name = "is_oos")
    private Boolean isOos;

    @Column(name = "oos_investigation_id")
    private Long oosInvestigationId;

    @Column(name = "entered_by", length = 100)
    private String enteredBy;

    @Column(name = "entered_at")
    private OffsetDateTime enteredAt;
}
