package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Test performed on a sample
 */
@Entity
@Table(name = "testing_test")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "run_id")
    private Long runId;

    @Column(name = "form_template_id")
    private Long formTemplateId;

    @Column(name = "status")
    private String status;


}
