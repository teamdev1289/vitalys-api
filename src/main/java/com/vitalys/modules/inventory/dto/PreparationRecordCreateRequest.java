package com.vitalys.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationRecordCreateRequest {
    @NotBlank(message = "Solution name is required")
    private String solutionName;

    private Long methodId;
    private String sopReference;
    private Double targetVolume;
    private String unit;
    private OffsetDateTime expiryAt;
    private String notes;

    @NotNull(message = "At least one input ingredient is required")
    private List<PreparationInputCreateDto> inputs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreparationInputCreateDto {
        @NotNull(message = "Source lot ID is required")
        private Long sourceLotId;

        @NotNull(message = "Quantity used is required")
        private Double quantityUsed;

        private String unit;
    }
}
