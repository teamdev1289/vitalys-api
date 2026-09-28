package com.vitalys.modules.sample.service;

import com.vitalys.modules.sample.dto.CustodyLogResponse;
import com.vitalys.modules.sample.dto.CustodyTransferRequest;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.SampleChainOfCustody;
import com.vitalys.modules.sample.repository.SampleChainOfCustodyRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChainOfCustodyServiceTest {

    @Mock
    private SampleChainOfCustodyRepository custodyRepository;
    @Mock
    private SampleRepository sampleRepository;

    @InjectMocks
    private ChainOfCustodyService custodyService;

    private Sample sample;

    @BeforeEach
    void setUp() {
        sample = Sample.builder()
                .id(1L)
                .sampleCode("SMP-20260901-0001")
                .currentLocation("Sample Reception A1")
                .storageCondition("ROOM_TEMPERATURE")
                .build();
    }

    @Test
    @DisplayName("transferCustody should update sample location and persist immutable custody log")
    void transferCustody_Success() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(custodyRepository.save(any(SampleChainOfCustody.class))).thenAnswer(inv -> {
            SampleChainOfCustody c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });

        CustodyTransferRequest req = CustodyTransferRequest.builder()
                .toUser("analyst_hoa")
                .toLocation("Analytical Lab Bench #4")
                .purpose("Prepare aliquot for HPLC Assay")
                .sampleCondition("INTACT")
                .storageCondition("COLD_ROOM_2_8C")
                .notes("Transferred via cold pack cooler")
                .build();

        CustodyLogResponse res = custodyService.transferCustody(1L, req);

        assertThat(res.getId()).isEqualTo(99L);
        assertThat(res.getToUser()).isEqualTo("analyst_hoa");
        assertThat(res.getToLocation()).isEqualTo("Analytical Lab Bench #4");
        assertThat(sample.getCurrentLocation()).isEqualTo("Analytical Lab Bench #4");
        assertThat(sample.getStorageCondition()).isEqualTo("COLD_ROOM_2_8C");

        verify(sampleRepository).save(sample);
        verify(custodyRepository).save(any(SampleChainOfCustody.class));
    }
}
