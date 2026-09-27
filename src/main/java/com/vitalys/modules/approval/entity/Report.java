package com.vitalys.modules.approval.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Generated result report
 */
@Entity
@Table(name = "approval_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "version")
    private String version;

    @Column(name = "status")
    private String status;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "generated_by")
    private String generatedBy;


}
