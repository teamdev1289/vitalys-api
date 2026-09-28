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
public class BatchResponse {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long formulationId;
    private Long specSetId;
    private String batchNumber;
    private OffsetDateTime manufacturingDate;
    private OffsetDateTime expiryDate;
    private Double quantityProduced;
    private String unit;
    private String status;
    private String notes;
    private Boolean isExpired;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
