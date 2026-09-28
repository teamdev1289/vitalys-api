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
public class FormFieldResponse {

    private Long id;
    private Long templateId;
    private String fieldKey;
    private String label;
    private String fieldType;
    private String dataBinding;
    private Boolean isRequired;
    private String orderIndex;
    private String optionsJson;
    private String defaultValue;
    private String unit;
    private String formulaExpression;
    private String validationRules;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
