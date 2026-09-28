package com.vitalys.modules.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequestUpdateRequest {

    private String priority;
    private OffsetDateTime dueDate;
    private String testScope;
    private Long specSetId;
    private String notes;
}
