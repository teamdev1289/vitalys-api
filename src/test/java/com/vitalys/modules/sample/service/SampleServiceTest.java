package com.vitalys.modules.sample.service;

import com.vitalys.modules.sample.dto.*;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.SampleChainOfCustody;
import com.vitalys.modules.sample.entity.SampleStatusHistory;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleChainOfCustodyRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.SampleStatusHistoryRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private TestRequestRepository testRequestRepository;
    @Mock
    private SampleStatusHistoryRepository statusHistoryRepository;
    @Mock
    private SampleChainOfCustodyRepository custodyRepository;
    @Mock
    private com.vitalys.modules.sys.repository.SysDepartmentRepository departmentRepository;
    @Mock
    private com.vitalys.modules.method.repository.SpecificationSetRepository specSetRepository;
    @Mock
    private com.vitalys.modules.product.repository.ProductRepository productRepository;
    @Mock
    private com.vitalys.modules.product.repository.BatchRepository batchRepository;

    @InjectMocks
    private SampleService sampleService;

    private TestRequest testRequest;
    private Sample existingSample;

    @BeforeEach
    void setUp() {
        testRequest = TestRequest.builder()
                .id(100L)
                .requestCode("REQ-20260901-001")
                .sourceType("PRODUCTION")
                .sampleType("FINISHED_PRODUCT")
                .requestedBy("qa_user")
                .status("SUBMITTED")
                .build();

        existingSample = Sample.builder()
                .id(1L)
                .requestId(100L)
                .sampleCode("SMP-20260901-0001")
                .barcode("BAR-SMP-20260901-0001")
                .status("RECEIVED")
                .storageCondition("ROOM_TEMPERATURE")
                .currentLocation("Sample Reception A1")
                .build();
    }

    @Test
    @DisplayName("accessionSamples should create samples with standard barcode and log initial custody")
    void accessionSamples_Success() {
        when(testRequestRepository.findById(100L)).thenReturn(Optional.of(testRequest));
        when(sampleRepository.existsBySampleCode(any())).thenReturn(false);
        when(sampleRepository.save(any(Sample.class))).thenAnswer(invocation -> {
            Sample s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        SampleAccessionRequest request = SampleAccessionRequest.builder()
                .requestId(100L)
                .containerCount(2)
                .storageCondition("COLD_ROOM_2_8C")
                .currentLocation("Cold Room Shelf B-12")
                .quantity(100.0)
                .unit("TABLET")
                .notes("Intact seals verified")
                .build();

        List<SampleResponse> result = sampleService.accessionSamples(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBarcode()).startsWith("BAR-SMP-");
        assertThat(result.get(0).getStatus()).isEqualTo("RECEIVED");

        verify(sampleRepository, times(2)).save(any(Sample.class));
        verify(statusHistoryRepository, times(2)).save(any(SampleStatusHistory.class));
        verify(custodyRepository, times(2)).save(any(SampleChainOfCustody.class));
        verify(testRequestRepository).save(any(TestRequest.class));
    }

    @Test
    @DisplayName("changeSampleStatus should succeed for valid transition with reason")
    void changeSampleStatus_ValidTransition() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(existingSample));
        when(sampleRepository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        SampleStatusChangeRequest req = SampleStatusChangeRequest.builder()
                .targetStatus("ASSIGNED")
                .reason("Assigned to senior analyst for HPLC testing")
                .build();

        SampleResponse res = sampleService.changeSampleStatus(1L, req);

        assertThat(res.getStatus()).isEqualTo("ASSIGNED");
        verify(statusHistoryRepository).save(argThat(h ->
                h.getFromStatus().equals("RECEIVED") &&
                h.getToStatus().equals("ASSIGNED") &&
                h.getReason().equals("Assigned to senior analyst for HPLC testing")
        ));
    }

    @Test
    @DisplayName("changeSampleStatus should throw IllegalStateException for invalid state transition")
    void changeSampleStatus_InvalidTransition_ThrowsException() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(existingSample));

        SampleStatusChangeRequest req = SampleStatusChangeRequest.builder()
                .targetStatus("APPROVED") // Cannot jump directly from RECEIVED to APPROVED
                .reason("Skipping tests")
                .build();

        assertThatThrownBy(() -> sampleService.changeSampleStatus(1L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid state transition");
    }
}
