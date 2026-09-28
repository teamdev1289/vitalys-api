package com.vitalys.modules.method.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationItemRequest {

    private Long id;
    private Long methodId;
    private String analyte;

    @NotBlank(message = "Tên chỉ tiêu (parameter_name) không được để trống")
    private String parameterName;

    private Double minLimit;
    private Double maxLimit;
    private String unit;
    private String comparisonOperator;
    private String textAcceptanceCriteria;
}
