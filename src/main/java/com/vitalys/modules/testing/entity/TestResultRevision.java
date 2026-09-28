package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

/**
 * Immutable revision history for test results enforcing 21 CFR Part 11 and ALCOA+ integrity.
 * Every edit to an existing result generates a non-destructive revision record with mandatory justification.
 */
@Entity
@Table(name = "testing_test_result_revision")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultRevision extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @Column(name = "old_value")
    private Double oldValue;

    @Column(name = "new_value")
    private Double newValue;

    @Column(name = "revised_by", nullable = false, length = 100)
    private String revisedBy;

    @Column(name = "revised_at", nullable = false)
    private OffsetDateTime revisedAt;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;
}
