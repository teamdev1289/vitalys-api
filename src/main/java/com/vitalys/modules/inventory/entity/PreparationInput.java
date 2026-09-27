package com.vitalys.modules.inventory.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Input lot for preparation
 */
@Entity
@Table(name = "inventory_preparation_input")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationInput extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "preparation_id")
    private Long preparationId;

    @Column(name = "source_lot_id")
    private Long sourceLotId;

    @Column(name = "quantity_used")
    private Double quantityUsed;

    @Column(name = "unit")
    private String unit;


}
