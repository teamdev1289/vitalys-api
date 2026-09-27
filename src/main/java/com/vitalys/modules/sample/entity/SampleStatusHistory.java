package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * State machine log for sample
 */
@Entity
@Table(name = "sample_sample_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SampleStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "from_status")
    private String fromStatus;

    @Column(name = "to_status")
    private String toStatus;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at")
    private OffsetDateTime changedAt;

    @Column(name = "reason")
    private String reason;


}
