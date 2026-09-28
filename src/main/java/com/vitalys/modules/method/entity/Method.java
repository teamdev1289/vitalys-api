package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Testing method or SOP
 */
@Entity
@Table(name = "method_method")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Method extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "source_standard")
    private String sourceStandard;

    @Column(name = "validation_status")
    private String validationStatus;

    @Column(name = "body_template", columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "method_code", unique = true)
    private String methodCode;

    @Column(name = "category")
    private String category;

    @Column(name = "instrument_type")
    private String instrumentType;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "effective_date")
    private OffsetDateTime effectiveDate;

    @Column(name = "review_due_date")
    private OffsetDateTime reviewDueDate;
}
