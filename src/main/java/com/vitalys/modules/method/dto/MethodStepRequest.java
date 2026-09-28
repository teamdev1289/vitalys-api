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
public class MethodStepRequest {

    private Long id;

    @NotBlank(message = "Mã bước (step_key) không được để trống")
    private String stepKey;

    @NotBlank(message = "Tên bước / hành động không được để trống")
    private String label;

    private String role;
    private Long expectedItemId;
    private Double expectedQuantity;
    private String unit;
    private String orderIndex;
    private String description;
    private String instructionNotes;
}
