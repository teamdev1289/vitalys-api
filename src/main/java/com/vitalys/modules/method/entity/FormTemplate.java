package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Dynamic form template
 */
@Entity
@Table(name = "method_form_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "version")
    private String version;

    @Column(name = "status")
    private String status;

    @Column(name = "schema_name")
    private String schemaName;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
