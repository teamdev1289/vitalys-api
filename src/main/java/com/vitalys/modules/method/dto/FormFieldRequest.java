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
public class FormFieldRequest {

    private Long id;

    @NotBlank(message = "Field key không được để trống")
    private String fieldKey;

    @NotBlank(message = "Label không được để trống")
    private String label;

    @NotBlank(message = "Field type (TEXT, NUMBER, FORMULA, SELECT, etc.) không được để trống")
    private String fieldType;

    private String dataBinding;
    private Boolean isRequired;
    private String orderIndex;
    private String optionsJson;
    private String defaultValue;
    private String unit;
    private String formulaExpression;
    private String validationRules;
}
