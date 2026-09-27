package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Revision history for test result
 */
@Entity
@Table(name = "testing_test_result_revision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultRevision extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "old_value")
    private Double oldValue;

    @Column(name = "new_value")
    private Double newValue;

    @Column(name = "revised_by")
    private String revisedBy;

    @Column(name = "revised_at")
    private OffsetDateTime revisedAt;

    @Column(name = "reason")
    private String reason;


}
