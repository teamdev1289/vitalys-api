package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Official formulation
 */
@Entity
@Table(name = "product_formulation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formulation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "source_trial_id")
    private Long sourceTrialId;

    @Column(name = "formulation_code")
    private String formulationCode;

    @Column(name = "version")
    private String version;

    @Column(name = "status")
    private String status;


}
