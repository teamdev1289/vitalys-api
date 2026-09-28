package com.vitalys.modules.retention.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicyResponse {
    private Long id;
    private String moduleName;
    private String categoryTitle;
    private Integer retentionYears;
    private Boolean isPermanent;
    private String description;
    private Boolean autoArchive;
    private OffsetDateTime updatedAt;
}
