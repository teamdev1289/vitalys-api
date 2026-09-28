package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Pharmaceutical Product definition.
 */
@Entity
@Table(name = "product_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true)
    private String productCode;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "dosage_form")
    private String dosageForm;

    @Column(name = "packaging_spec")
    private String packagingSpec;

    @Column(name = "shelf_life_months")
    private Integer shelfLifeMonths;

    @Column(name = "registrant")
    private String registrant;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "quality_standard")
    private String qualityStandard;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "classification")
    private String classification;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
