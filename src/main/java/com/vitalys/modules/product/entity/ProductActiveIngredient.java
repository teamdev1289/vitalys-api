package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Declared strength of product
 */
@Entity
@Table(name = "product_product_active_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductActiveIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "declared_strength")
    private String declaredStrength;

    @Column(name = "unit")
    private String unit;


}
