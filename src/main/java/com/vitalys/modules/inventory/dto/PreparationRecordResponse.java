package com.vitalys.modules.inventory.dto;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationRecordResponse {
    private Long id;
    private String solutionName;
    private Long resultLotId;
    private String resultLotNumber;
    private Long methodId;
    private String sopReference;
    private String preparedBy;
    private OffsetDateTime preparedAt;
    private OffsetDateTime expiryAt;
    private Double targetVolume;
    private String unit;
    private String status;
    private String notes;
    private List<PreparationInputDto> inputs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreparationInputDto {
        private Long id;
        private Long sourceLotId;
        private String sourceLotNumber;
        private String itemName;
        private Double quantityUsed;
        private String unit;
    }
}
