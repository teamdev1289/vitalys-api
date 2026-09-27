package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Quality specification set
 */
@Entity
@Table(name = "method_specification_set")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationSet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formulation_id")
    private Long formulationId;

    @Column(name = "version")
    private String version;

    @Column(name = "effective_date")
    private OffsetDateTime effectiveDate;

    @Column(name = "status")
    private String status;

    @Column(name = "change_reason")
    private String changeReason;


}
