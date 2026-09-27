package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Sample instance
 */
@Entity
@Table(name = "sample_sample")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "sample_code")
    private String sampleCode;

    @Column(name = "status")
    private String status;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;


}
