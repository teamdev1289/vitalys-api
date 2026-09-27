package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Product definition
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

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "product_name")
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

    @Column(name = "status")
    private String status;


}
