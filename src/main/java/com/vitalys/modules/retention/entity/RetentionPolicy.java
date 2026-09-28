package com.vitalys.modules.retention.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "retention_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetentionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_name", unique = true, nullable = false, length = 50)
    private String moduleName; // AUDIT_TRAIL, SDMS_RAW_FILES, TESTING_RESULTS, STABILITY_STUDIES, COA_REPORTS

    @Column(name = "category_title", nullable = false, length = 150)
    private String categoryTitle;

    @Column(name = "retention_years", nullable = false)
    private Integer retentionYears;

    @Column(name = "is_permanent", nullable = false)
    @Builder.Default
    private Boolean isPermanent = false;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "auto_archive", nullable = false)
    @Builder.Default
    private Boolean autoArchive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
