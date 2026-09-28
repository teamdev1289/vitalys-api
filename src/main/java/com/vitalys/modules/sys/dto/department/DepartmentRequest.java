package com.vitalys.modules.sys.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Code must be under 50 characters")
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(max = 255, message = "Name must be under 255 characters")
    private String name;

    private String description;
}
