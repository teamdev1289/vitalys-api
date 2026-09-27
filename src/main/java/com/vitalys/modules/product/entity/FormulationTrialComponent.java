package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Components of a trial
 */
@Entity
@Table(name = "product_formulation_trial_component")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulationTrialComponent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trial_id")
    private Long trialId;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "role")
    private String role;

    @Column(name = "quantity_per_unit")
    private Double quantityPerUnit;

    @Column(name = "unit")
    private String unit;


}
