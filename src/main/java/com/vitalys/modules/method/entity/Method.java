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

    @Column(name = "body_template")
    private String bodyTemplate;


}
