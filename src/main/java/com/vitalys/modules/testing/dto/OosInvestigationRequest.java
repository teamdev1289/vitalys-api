package com.vitalys.modules.testing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OosInvestigationRequest {

    private Long resultId;

    private String phase; // PHASE_1_LAB, PHASE_2_MFG, CLOSED

    private String rootCauseCategory; // ANALYST_ERROR, INSTRUMENT_ERROR, SAMPLE_PREPARATION, BATCH_FAILURE, INCONCLUSIVE

    private String immediateAction;

    private String investigationFindings;

    private String supervisorComments;

    private Boolean retestApproved;

    private String conclusion; // CONFIRMED_OOS, LAB_ERROR_INVALIDATED, RETEST_PASSED, INCONCLUSIVE

    private String status; // OPEN, UNDER_REVIEW, CLOSED
}
