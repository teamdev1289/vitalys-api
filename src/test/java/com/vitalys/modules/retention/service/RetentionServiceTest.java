package com.vitalys.modules.retention.service;

import com.vitalys.modules.retention.dto.*;
import com.vitalys.modules.retention.entity.LegalHoldRecord;
import com.vitalys.modules.retention.entity.RetentionPolicy;
import com.vitalys.modules.retention.repository.LegalHoldRecordRepository;
import com.vitalys.modules.retention.repository.RetentionPolicyRepository;
import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetentionServiceTest {

    @Mock
    private RetentionPolicyRepository retentionPolicyRepository;

    @Mock
    private LegalHoldRecordRepository legalHoldRecordRepository;

    @Mock
    private SysAuditTrailRepository auditTrailRepository;

    @InjectMocks
    private RetentionService retentionService;

    private RetentionPolicy policy;
    private LegalHoldRecord hold;

    @BeforeEach
    void setUp() {
        policy = RetentionPolicy.builder()
                .id(1L)
                .moduleName("SDMS_RAW_FILES")
                .categoryTitle("SDMS Raw Files")
                .retentionYears(10)
                .isPermanent(false)
                .autoArchive(true)
                .build();

        hold = LegalHoldRecord.builder()
                .id(1L)
                .holdCode("LH-2026-TEST")
                .caseTitle("FDA Inspection")
                .caseReference("FDA-483")
                .targetModule("BATCH")
                .targetEntityId(1L)
                .status("ACTIVE")
                .placedBy("labadmin")
                .reason("FDA inspection lock")
                .build();
    }

    @Test
    @DisplayName("Should update retention policy years and log audit trail")
    void shouldUpdateRetentionPolicy() {
        when(retentionPolicyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(retentionPolicyRepository.save(any(RetentionPolicy.class))).thenAnswer(inv -> inv.getArgument(0));

        RetentionPolicyResponse resp = retentionService.updatePolicy(
                1L,
                RetentionPolicyUpdateRequest.builder().retentionYears(15).autoArchive(true).build(),
                "itadmin"
        );

        assertThat(resp).isNotNull();
        assertThat(resp.getRetentionYears()).isEqualTo(15);
        verify(auditTrailRepository).save(any(SysAuditTrail.class));
    }

    @Test
    @DisplayName("Should place legal hold on entity")
    void shouldPlaceLegalHold() {
        when(legalHoldRecordRepository.save(any(LegalHoldRecord.class))).thenAnswer(inv -> {
            LegalHoldRecord h = inv.getArgument(0);
            h.setId(10L);
            return h;
        });

        LegalHoldResponse resp = retentionService.placeLegalHold(LegalHoldCreateRequest.builder()
                .caseTitle("Audit Investigation")
                .caseReference("INV-2026-01")
                .targetModule("BATCH")
                .targetEntityId(5L)
                .reason("Preserve data")
                .build(), "labadmin");

        assertThat(resp).isNotNull();
        assertThat(resp.getStatus()).isEqualTo("ACTIVE");
        assertThat(resp.getTargetModule()).isEqualTo("BATCH");
        verify(auditTrailRepository).save(any(SysAuditTrail.class));
    }

    @Test
    @DisplayName("Should release legal hold")
    void shouldReleaseLegalHold() {
        when(legalHoldRecordRepository.findById(1L)).thenReturn(Optional.of(hold));
        when(legalHoldRecordRepository.save(any(LegalHoldRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        LegalHoldResponse resp = retentionService.releaseLegalHold(
                1L,
                LegalHoldReleaseRequest.builder().releaseNotes("Inspection completed satisfactorily").build(),
                "labadmin"
        );

        assertThat(resp).isNotNull();
        assertThat(resp.getStatus()).isEqualTo("RELEASED");
        verify(auditTrailRepository).save(any(SysAuditTrail.class));
    }
}
