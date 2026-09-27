package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Final result compared to spec
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

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "analyte")
    private String analyte;

    @Column(name = "value")
    private Double value;

    @Column(name = "unit")
    private String unit;

    @Column(name = "pass_fail")
    private String passFail;


}
