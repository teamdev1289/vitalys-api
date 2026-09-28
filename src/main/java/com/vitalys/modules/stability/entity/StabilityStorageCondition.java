package com.vitalys.modules.stability.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Storage condition / Climatic chamber for stability study
 */
@Entity
@Table(name = "stability_storage_condition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StabilityStorageCondition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Long studyId;

    @Column(name = "chamber_name", nullable = false)
    private String chamberName;

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(name = "temperature_tolerance")
    @Builder.Default
    private Double temperatureTolerance = 2.0;

    @Column(name = "relative_humidity")
    private Double relativeHumidity;

    @Column(name = "humidity_tolerance")
    @Builder.Default
    private Double humidityTolerance = 5.0;

    @Column(name = "light_condition")
    @Builder.Default
    private String lightCondition = "DARK";

    @Column(name = "shelf_location")
    private String shelfLocation;
}
