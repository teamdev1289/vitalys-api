package com.vitalys.modules.testing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.entity.SpecificationItem;
import com.vitalys.modules.method.repository.MethodRepository;
import com.vitalys.modules.method.repository.SpecificationItemRepository;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.testing.dto.*;
import com.vitalys.modules.testing.entity.*;
import com.vitalys.modules.testing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestingServiceTest {

    @Mock
    private TestEntityRepository testRepository;
    @Mock
    private ResultRepository resultRepository;
    @Mock
    private TestResultRevisionRepository revisionRepository;
    @Mock
    private FormSubmissionRepository formSubmissionRepository;
    @Mock
    private AnalyticalRunRepository runRepository;
    @Mock
    private OosInvestigationRepository oosRepository;
    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private TestRequestRepository testRequestRepository;
    @Mock
    private MethodRepository methodRepository;
    @Mock
    private SpecificationItemRepository specItemRepository;
    @Mock
    private InstrumentRepository instrumentRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TestingService testingService;

    private Sample sample;
    private Method method;
    private SpecificationItem specItem;
    private TestEntity testEntity;

    @BeforeEach
    void setUp() {
        sample = Sample.builder()
                .id(10L)
                .sampleCode("SMP-20260901-0001")
                .status("RECEIVED")
                .build();

        method = Method.builder()
                .id(20L)
                .methodCode("SOP-HPLC-001")
                .name("Paracetamol Assay")
                .build();

        specItem = SpecificationItem.builder()
                .id(30L)
                .parameterName("Assay")
                .minLimit(98.5)
                .maxLimit(101.5)
                .unit("%")
                .build();

        testEntity = TestEntity.builder()
                .id(1L)
                .testCode("TST-20260901-0001")
                .sampleId(10L)
                .methodId(20L)
                .specItemId(30L)
                .assignedTo("analyst_hoa")
                .status("ASSIGNED")
                .build();
    }

    @Test
    @DisplayName("assignTest should assign procedure, set ASSIGNED status and update sample")
    void assignTest_Success() {
        when(sampleRepository.findById(10L)).thenReturn(Optional.of(sample));
        when(methodRepository.findById(20L)).thenReturn(Optional.of(method));
        when(testRepository.existsByTestCode(any())).thenReturn(false);
        when(testRepository.save(any(TestEntity.class))).thenAnswer(inv -> {
            TestEntity t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        TestAssignmentRequest request = TestAssignmentRequest.builder()
                .sampleId(10L)
                .methodId(20L)
                .specItemId(30L)
                .assignedTo("analyst_hoa")
                .priority("URGENT")
                .notes("Emergency batch release testing")
                .build();

        TestResponse res = testingService.assignTest(request);

        assertThat(res.getStatus()).isEqualTo("ASSIGNED");
        assertThat(res.getAssignedTo()).isEqualTo("analyst_hoa");
        assertThat(res.getPriority()).isEqualTo("URGENT");
        verify(testRepository).save(any(TestEntity.class));
        verify(sampleRepository).save(sample);
        assertThat(sample.getStatus()).isEqualTo("ASSIGNED");
    }

    @Test
    @DisplayName("enterResult with passing value should set PASS and isOos=false")
    void enterResult_Passing() {
        when(testRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(specItemRepository.findById(30L)).thenReturn(Optional.of(specItem));
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> {
            Result r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });
        when(revisionRepository.findByResultIdOrderByIdDesc(50L)).thenReturn(Collections.emptyList());

        ResultEntryRequest req = ResultEntryRequest.builder()
                .testId(1L)
                .analyte("Paracetamol Content")
                .value(100.2) // Within 98.5 - 101.5
                .unit("%")
                .build();

        ResultResponse res = testingService.enterResult(req);

        assertThat(res.getPassFail()).isEqualTo("PASS");
        assertThat(res.getIsOos()).isFalse();
        assertThat(testEntity.getStatus()).isEqualTo("COMPLETED");
        verify(oosRepository, never()).save(any());
    }

    @Test
    @DisplayName("enterResult with out-of-specification value should trigger OOS Investigation")
    void enterResult_OutOfSpecification_TriggersOOS() {
        when(testRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(specItemRepository.findById(30L)).thenReturn(Optional.of(specItem));
        when(oosRepository.existsByInvestigationCode(any())).thenReturn(false);
        when(oosRepository.save(any(OosInvestigation.class))).thenAnswer(inv -> {
            OosInvestigation o = inv.getArgument(0);
            o.setId(999L);
            return o;
        });
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> {
            Result r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });
        when(revisionRepository.findByResultIdOrderByIdDesc(50L)).thenReturn(Collections.emptyList());

        ResultEntryRequest req = ResultEntryRequest.builder()
                .testId(1L)
                .analyte("Paracetamol Content")
                .value(96.8) // Below min 98.5 -> OOS!
                .unit("%")
                .build();

        ResultResponse res = testingService.enterResult(req);

        assertThat(res.getPassFail()).isEqualTo("FAIL");
        assertThat(res.getIsOos()).isTrue();
        verify(oosRepository).save(any(OosInvestigation.class));
    }

    @Test
    @DisplayName("reviseResult should persist immutable revision audit log")
    void reviseResult_Success() {
        Result existing = Result.builder()
                .id(50L)
                .testId(1L)
                .value(96.8)
                .specMin(98.5)
                .specMax(101.5)
                .isOos(true)
                .passFail("FAIL")
                .build();

        when(resultRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));
        when(revisionRepository.findByResultIdOrderByIdDesc(50L)).thenReturn(Collections.emptyList());

        ResultRevisionRequest req = ResultRevisionRequest.builder()
                .newValue(100.1)
                .reason("Dilution factor corrected from 100 to 105 per SOP calculation note")
                .build();

        ResultResponse res = testingService.reviseResult(50L, req);

        assertThat(res.getValue()).isEqualTo(100.1);
        assertThat(res.getPassFail()).isEqualTo("PASS");
        assertThat(res.getIsOos()).isFalse();

        verify(revisionRepository).save(argThat(rev ->
                rev.getOldValue().equals(96.8) &&
                rev.getNewValue().equals(100.1) &&
                rev.getReason().contains("Dilution factor corrected")
        ));
    }
}
