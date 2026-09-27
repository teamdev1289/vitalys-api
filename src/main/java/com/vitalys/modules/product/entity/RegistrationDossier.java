package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Registration dossier for circulation
 */
@Entity
@Table(name = "product_registration_dossier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDossier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "formulation_trial_id")
    private Long formulationTrialId;

    @Column(name = "dossier_type")
    private String dossierType;

    @Column(name = "submitted_date")
    private OffsetDateTime submittedDate;

    @Column(name = "status")
    private String status;

    @Column(name = "authority_ref_number")
    private String authorityRefNumber;

    @Column(name = "decision_date")
    private OffsetDateTime decisionDate;


}
