package com.vitalys.modules.method.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormTemplateResponse {

    private Long id;
    private Long methodId;
    private String methodCode;
    private String methodName;
    private String schemaName;
    private String title;
    private String version;
    private String status;
    private String description;
    private Integer fieldCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder.Default
    private List<FormFieldResponse> fields = new ArrayList<>();
}
