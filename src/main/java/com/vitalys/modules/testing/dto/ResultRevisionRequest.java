package com.vitalys.modules.testing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultRevisionRequest {

    @com.fasterxml.jackson.annotation.JsonAlias({"value", "newValue"})
    private Double newValue;

    @com.fasterxml.jackson.annotation.JsonAlias({"textValue", "newTextValue"})
    private String newTextValue;

    @NotBlank(message = "Mandatory GxP justification reason is required for result revision")
    @Size(min = 5, message = "Reason must be at least 5 characters long")
    private String reason;
}
