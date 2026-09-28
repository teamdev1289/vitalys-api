package com.vitalys.modules.retention.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalHoldReleaseRequest {

    private String releasedBy;

    @NotBlank(message = "Release justification / closure note is required")
    private String releaseNotes;
}
