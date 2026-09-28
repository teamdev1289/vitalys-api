package com.vitalys.modules.partner.dto;

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
public class CustomerResponse {

    private Long id;
    private String code;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String taxId;
    private String status;
    private String notes;
    private Integer activeProjectCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
