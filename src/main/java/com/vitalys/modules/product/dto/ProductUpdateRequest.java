package com.vitalys.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "Product code is required")
    @Size(max = 100, message = "Code must not exceed 100 characters")
    private String productCode;

    @Size(max = 255)
    private String registrationNumber;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String productName;

    @Size(max = 255)
    private String dosageForm;

    @Size(max = 255)
    private String packagingSpec;

    private Integer shelfLifeMonths;

    @Size(max = 255)
    private String registrant;

    @Size(max = 255)
    private String manufacturer;

    @Size(max = 255)
    private String countryOfOrigin;

    @Size(max = 255)
    private String qualityStandard;

    @Size(max = 255)
    private String productCategory;

    @Size(max = 255)
    private String classification;

    private String status;

    private String description;
}
