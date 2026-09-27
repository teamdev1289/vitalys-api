package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Raw materials/active ingredients
 */
@Entity
@Table(name = "product_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "inn_name")
    private String innName;

    @Column(name = "cas_number")
    private String casNumber;

    @Column(name = "type")
    private String type;


}
