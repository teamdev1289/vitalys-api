package com.vitalys.modules.approval.service;

import com.vitalys.modules.approval.dto.ApprovalActionRequest;
import com.vitalys.modules.approval.dto.ApprovalStepResponse;
import com.vitalys.modules.approval.dto.WorkflowResponse;
import com.vitalys.modules.approval.entity.ApprovalStep;
import com.vitalys.modules.approval.repository.ApprovalStepRepository;
import com.vitalys.modules.approval.repository.ReportRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalWorkflowServiceTest {

    @Mock
    private ApprovalStepRepository stepRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private ESignatureService eSignatureService;
    @Mock
    private SysAuditTrailRepository auditTrailRepository;

    @InjectMocks
    private ApprovalWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "supervisor", "Supervisor@123",
                List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should retrieve workflow and determine current pending step")
    void testGetWorkflow() {
        ApprovalStep step1 = ApprovalStep.builder()
                .id(1L).entityType("SAMPLE").entityId(10L).stepNumber(1)
                .stepName("Analyst Verification").requiredRole("OPERATOR").status("APPROVED")
                .build();
        ApprovalStep step2 = ApprovalStep.builder()
                .id(2L).entityType("SAMPLE").entityId(10L).stepNumber(2)
                .stepName("Supervisor Review").requiredRole("SUPERVISOR").status("PENDING")
                .build();
        ApprovalStep step3 = ApprovalStep.builder()
                .id(3L).entityType("SAMPLE").entityId(10L).stepNumber(3)
                .stepName("QA Approval").requiredRole("LAB_ADMIN").status("WAITING")
                .build();

        when(stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc("SAMPLE", 10L))
                .thenReturn(List.of(step1, step2, step3));

        WorkflowResponse res = workflowService.getWorkflow("SAMPLE", 10L);

        assertThat(res).isNotNull();
        assertThat(res.getOverallStatus()).isEqualTo("IN_PROGRESS");
        assertThat(res.getCurrentStepNumber()).isEqualTo(2);
        assertThat(res.isCanCurrentUserAction()).isTrue();
        assertThat(res.getSteps()).hasSize(3);
    }

    @Test
    @DisplayName("Should approve step with 21 CFR Part 11 signature and activate next step")
    void testApproveStepAdvancesWorkflow() {
        ApprovalStep step2 = ApprovalStep.builder()
                .id(2L).entityType("SAMPLE").entityId(10L).stepNumber(2)
                .stepName("Supervisor Review").requiredRole("SUPERVISOR").status("PENDING")
                .build();

        ApprovalStep step3 = ApprovalStep.builder()
                .id(3L).entityType("SAMPLE").entityId(10L).stepNumber(3)
                .stepName("QA Approval").requiredRole("LAB_ADMIN").status("WAITING")
                .build();

        when(stepRepository.findById(2L)).thenReturn(Optional.of(step2));
        when(stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc("SAMPLE", 10L))
                .thenReturn(List.of(step2, step3));
        when(eSignatureService.generateSignatureHash(any(), any(), any(), any()))
                .thenReturn("hash_abc_123_sha256");

        ApprovalActionRequest req = ApprovalActionRequest.builder()
                .action("APPROVE")
                .password("Supervisor@123")
                .meaning("I have reviewed and approve")
                .comment("Verified chromatography data")
                .build();

        ApprovalStepResponse res = workflowService.actionStep(2L, req);

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo("APPROVED");
        assertThat(res.getESignatureHash()).isEqualTo("hash_abc_123_sha256");
        assertThat(step3.getStatus()).isEqualTo("PENDING");

        verify(stepRepository, times(1)).save(step2);
        verify(stepRepository, times(1)).save(step3);
        verify(auditTrailRepository, times(1)).save(any(SysAuditTrail.class));
    }

    @Test
    @DisplayName("Should fail signature when step is not PENDING")
    void testActionStepNonPendingThrows() {
        ApprovalStep step = ApprovalStep.builder()
                .id(1L).entityType("SAMPLE").entityId(10L).stepNumber(1)
                .status("APPROVED")
                .build();

        when(stepRepository.findById(1L)).thenReturn(Optional.of(step));

        ApprovalActionRequest req = ApprovalActionRequest.builder()
                .action("APPROVE")
                .password("test")
                .build();

        assertThrows(IllegalArgumentException.class, () -> workflowService.actionStep(1L, req));
    }
}
