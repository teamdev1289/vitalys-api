package com.vitalys.modules.method.dto;

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
public class SpecificationItemResponse {

    private Long id;
    private Long specSetId;
    private Long methodId;
    private String methodCode;
    private String methodName;
    private String analyte;
    private String parameterName;
    private Double minLimit;
    private Double maxLimit;
    private String unit;
    private String comparisonOperator;
    private String textAcceptanceCriteria;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
