package com.vitalys.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long formulationId;
    private Long specSetId;

    @NotBlank(message = "Batch number is required")
    @Size(max = 255, message = "Batch number must not exceed 255 characters")
    private String batchNumber;

    @NotNull(message = "Manufacturing date is required")
    private OffsetDateTime manufacturingDate;

    @NotNull(message = "Expiry date is required")
    private OffsetDateTime expiryDate;

    private Double quantityProduced;

    private String unit;

    private String status;

    private String notes;
}
