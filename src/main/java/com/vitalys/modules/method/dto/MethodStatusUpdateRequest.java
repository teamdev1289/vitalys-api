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
public class MethodStatusUpdateRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String changeReason;
    private String approvedBy;
}
