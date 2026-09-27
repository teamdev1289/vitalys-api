package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Steps in a method
 */
@Entity
@Table(name = "method_method_step_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MethodStepItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "step_key")
    private String stepKey;

    @Column(name = "label")
    private String label;

    @Column(name = "role")
    private String role;

    @Column(name = "expected_item_id")
    private Long expectedItemId;

    @Column(name = "expected_quantity")
    private Double expectedQuantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "order_index")
    private String orderIndex;


}
