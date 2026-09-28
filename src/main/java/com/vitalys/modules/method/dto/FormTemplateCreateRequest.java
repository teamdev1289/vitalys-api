package com.vitalys.modules.method.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplateCreateRequest {

    @NotNull(message = "ID phương pháp thử (method_id) không được để trống")
    private Long methodId;

    @NotBlank(message = "Mã schema (schema_name) không được để trống")
    private String schemaName;

    @NotBlank(message = "Tiêu đề biểu mẫu không được để trống")
    private String title;

    @NotBlank(message = "Phiên bản không được để trống")
    private String version;

    private String status; // DRAFT, RELEASED, ARCHIVED
    private String description;
    private List<FormFieldRequest> fields;
}
