package com.vitalys.modules.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long id;
    private String productCode;
    private String registrationNumber;
    private String productName;
    private String dosageForm;
    private String packagingSpec;
    private Integer shelfLifeMonths;
    private String registrant;
    private String manufacturer;
    private String countryOfOrigin;
    private String qualityStandard;
    private String productCategory;
    private String classification;
    private String status;
    private String description;
    private Integer activeBatchCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
