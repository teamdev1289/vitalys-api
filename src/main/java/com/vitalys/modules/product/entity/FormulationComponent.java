package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Components of official formulation
 */
@Entity
@Table(name = "product_formulation_component")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulationComponent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "formulation_id")
    private Long formulationId;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "role")
    private String role;

    @Column(name = "quantity_per_unit")
    private Double quantityPerUnit;

    @Column(name = "unit")
    private String unit;


}
