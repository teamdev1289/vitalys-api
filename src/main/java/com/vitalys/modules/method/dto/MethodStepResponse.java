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
public class MethodStepResponse {

    private Long id;
    private Long methodId;
    private String stepKey;
    private String label;
    private String role;
    private Long expectedItemId;
    private Double expectedQuantity;
    private String unit;
    private String orderIndex;
    private String description;
    private String instructionNotes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
