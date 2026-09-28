package com.vitalys.modules.method.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationSetCreateRequest {

    @NotBlank(message = "Mã bộ tiêu chuẩn (spec_code) không được để trống")
    private String specCode;

    @NotBlank(message = "Tên bộ tiêu chuẩn không được để trống")
    private String name;

    @NotNull(message = "ID sản phẩm (product_id) không được để trống")
    private Long productId;

    private Long formulationId;

    @NotBlank(message = "Phiên bản không được để trống")
    private String version;

    private String market; // US, EU, VN, GLOBAL
    private String changeReason;
    private OffsetDateTime effectiveDate;
    private OffsetDateTime expiryDate;

    private List<SpecificationItemRequest> items;
}
