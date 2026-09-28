package com.vitalys.modules.testing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticalRunRequest {

    @NotBlank(message = "Run sequence name is required")
    private String name;

    private Long instrumentId;

    private OffsetDateTime runDate;

    private String analyst;

    @Builder.Default
    private String status = "COMPLETED";

    @Builder.Default
    private Integer batchSize = 1;

    private String notes;
}
