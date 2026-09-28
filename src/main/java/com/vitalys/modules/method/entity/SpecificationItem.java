package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Specific item in specification
 */
@Entity
@Table(name = "method_specification_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spec_set_id")
    private Long specSetId;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "analyte")
    private String analyte;

    @Column(name = "min_limit")
    private Double minLimit;

    @Column(name = "max_limit")
    private Double maxLimit;

    @Column(name = "unit")
    private String unit;

    @Column(name = "parameter_name")
    private String parameterName;

    @Column(name = "comparison_operator")
    private String comparisonOperator;

    @Column(name = "text_acceptance_criteria", columnDefinition = "TEXT")
    private String textAcceptanceCriteria;
}
