package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Fields for dynamic form builder
 */
@Entity
@Table(name = "method_form_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormField extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "field_key")
    private String fieldKey;

    @Column(name = "label")
    private String label;

    @Column(name = "field_type")
    private String fieldType;

    @Column(name = "data_binding")
    private String dataBinding;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "order_index")
    private String orderIndex;


}
