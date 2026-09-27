package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Formulation trial in R&D
 */
@Entity
@Table(name = "product_formulation_trial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulationTrial extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rd_project_id")
    private Long rdProjectId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "trial_code")
    private String trialCode;

    @Column(name = "version")
    private String version;

    @Column(name = "status")
    private String status;


}
