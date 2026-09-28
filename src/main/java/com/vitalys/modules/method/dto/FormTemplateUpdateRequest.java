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
public class FormTemplateUpdateRequest {

    @NotBlank(message = "Tiêu đề biểu mẫu không được để trống")
    private String title;

    @NotBlank(message = "Phiên bản không được để trống")
    private String version;

    private String status;
    private String description;
}
