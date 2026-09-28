package com.vitalys.modules.method.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecificationSetDetailResponse {

    private Long id;
    private String specCode;
    private String name;
    private Long productId;
    private String productCode;
    private String productName;
    private Long formulationId;
    private String version;
    private String market;
    private String status;
    private String changeReason;
    private OffsetDateTime effectiveDate;
    private OffsetDateTime expiryDate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder.Default
    private List<SpecificationItemResponse> items = new ArrayList<>();
}
