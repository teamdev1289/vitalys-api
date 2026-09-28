package com.vitalys.modules.approval.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 21 CFR Part 11 Electronic Signature approval or rejection request.
 * Mandates re-authentication via password, explicit signature meaning, and comment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalActionRequest {

    @NotBlank(message = "Action must be APPROVE or REJECT")
    private String action; // APPROVE, REJECT

    @NotBlank(message = "Re-authentication password is required by 21 CFR Part 11")
    private String password;

    private String meaning; // e.g. "I have reviewed these analytical results and approve release"

    private String comment; // required if action is REJECT
}
