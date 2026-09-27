package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Result of validation parameter
 */
@Entity
@Table(name = "method_validation_parameter_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationParameterResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "protocol_id")
    private Long protocolId;

    @Column(name = "parameter_name")
    private String parameterName;

    @Column(name = "acceptance_criteria")
    private String acceptanceCriteria;

    @Column(name = "actual_result")
    private String actualResult;

    @Column(name = "pass_fail")
    private String passFail;


}
