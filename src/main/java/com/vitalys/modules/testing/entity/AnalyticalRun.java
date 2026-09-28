package com.vitalys.modules.testing.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Analytical run sequence serving multiple tests on a specific laboratory instrument.
 */
@Entity
@Table(name = "testing_analytical_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticalRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_code", unique = true, length = 100)
    private String runCode;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "run_date")
    private OffsetDateTime runDate;

    @Column(name = "analyst", length = 100)
    private String analyst;

    @Column(name = "status", length = 50)
    private String status; // PLANNED, IN_PROGRESS, COMPLETED, ABORTED

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
