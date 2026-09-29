package com.vitalys.modules.stability.service;

import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.stability.dto.*;
import com.vitalys.modules.stability.entity.*;
import com.vitalys.modules.stability.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StabilityServiceTest {

    @Mock
    private StabilityStudyRepository studyRepository;

    @Mock
    private StabilityStorageConditionRepository conditionRepository;

    @Mock
    private StabilityTimePointRepository timePointRepository;

    @Mock
    private StabilityPullEventRepository pullEventRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private TestRequestRepository testRequestRepository;

    @InjectMocks
    private StabilityService stabilityService;

    private StabilityStudy sampleStudy;
    private StabilityStorageCondition sampleCondition;

    @BeforeEach
    void setUp() {
        sampleStudy = StabilityStudy.builder()
                .id(1L)
                .studyCode("STB-2026-PARA01")
                .studyTitle("Paracetamol 500mg Stability")
                .productId(1L)
                .batchId(1L)
                .studyType("LONG_TERM")
                .durationMonths(24)
                .startDate(OffsetDateTime.now().minusMonths(6))
                .status("ACTIVE")
                .build();

        sampleCondition = StabilityStorageCondition.builder()
                .id(10L)
                .studyId(1L)
                .chamberName("Chamber #01")
                .temperatureCelsius(25.0)
                .relativeHumidity(60.0)
                .build();
    }

    @Test
    void testCreateStudy_GeneratesMatrix() {
        when(studyRepository.save(any(StabilityStudy.class))).thenAnswer(invocation -> {
            StabilityStudy s = invocation.getArgument(0);
            s.setId(100L);
            return s;
        });

        when(conditionRepository.save(any(StabilityStorageCondition.class))).thenAnswer(invocation -> {
            StabilityStorageCondition c = invocation.getArgument(0);
            c.setId(201L);
            return c;
        });

        when(timePointRepository.save(any(StabilityTimePoint.class))).thenAnswer(invocation -> {
            StabilityTimePoint t = invocation.getArgument(0);
            t.setId(301L);
            return t;
        });

        StabilityStudyCreateRequest req = StabilityStudyCreateRequest.builder()
                .studyTitle("Accelerated Study Batch 01")
                .studyType("ACCELERATED")
                .durationMonths(6)
                .startDate(OffsetDateTime.now())
                .conditions(List.of(
                        StabilityStudyCreateRequest.StorageConditionCreateDto.builder()
                                .chamberName("Chamber #02")
                                .temperatureCelsius(40.0)
                                .relativeHumidity(75.0)
                                .build()
                ))
                .timePoints(List.of(
                        StabilityStudyCreateRequest.TimePointCreateDto.builder()
                                .pointLabel("0M")
                                .monthOffset(0)
                                .build(),
                        StabilityStudyCreateRequest.TimePointCreateDto.builder()
                                .pointLabel("3M")
                                .monthOffset(3)
                                .build(),
                        StabilityStudyCreateRequest.TimePointCreateDto.builder()
                                .pointLabel("6M")
                                .monthOffset(6)
                                .build()
                ))
                .build();

        StabilityStudyResponse res = stabilityService.createStudy(req, "supervisor01");

        assertNotNull(res);
        assertEquals("ACTIVE", res.getStatus());
        // 1 condition x 3 timepoints = 3 pull events created
        verify(pullEventRepository, times(3)).save(any(StabilityPullEvent.class));
    }

    @Test
    void testCalculateTrend_LinearRegression() {
        when(studyRepository.findById(1L)).thenReturn(Optional.of(sampleStudy));
        when(conditionRepository.findByStudyId(1L)).thenReturn(List.of(sampleCondition));

        StabilityTimePoint tp0 = StabilityTimePoint.builder().id(1L).pointLabel("0M").monthOffset(0).build();
        StabilityTimePoint tp3 = StabilityTimePoint.builder().id(2L).pointLabel("3M").monthOffset(3).build();
        StabilityTimePoint tp6 = StabilityTimePoint.builder().id(3L).pointLabel("6M").monthOffset(6).build();

        when(timePointRepository.findById(1L)).thenReturn(Optional.of(tp0));
        when(timePointRepository.findById(2L)).thenReturn(Optional.of(tp3));
        when(timePointRepository.findById(3L)).thenReturn(Optional.of(tp6));

        List<StabilityPullEvent> events = List.of(
                StabilityPullEvent.builder().id(101L).timePointId(1L).scheduledDate(LocalDate.now().minusMonths(6)).assayResult(100.0).status("COMPLETED").build(),
                StabilityPullEvent.builder().id(102L).timePointId(2L).scheduledDate(LocalDate.now().minusMonths(3)).assayResult(99.0).status("COMPLETED").build(),
                StabilityPullEvent.builder().id(103L).timePointId(3L).scheduledDate(LocalDate.now()).assayResult(98.0).status("COMPLETED").build()
        );

        when(pullEventRepository.findByStudyIdAndStorageConditionIdOrderByScheduledDateAsc(1L, 10L)).thenReturn(events);

        StabilityTrendResponse trend = stabilityService.calculateTrend(1L, 10L);

        assertNotNull(trend);
        assertEquals(3, trend.getAssayPoints().size());
        assertNotNull(trend.getSlope());
        // Slope for (0, 100), (3, 99), (6, 98) is -1/3 ≈ -0.333
        assertTrue(trend.getSlope() < 0);
        assertEquals(100.0, trend.getIntercept(), 0.01);
        // At y = 95.0%, month = (95 - 100) / (-1/3) = 15.0 months
        assertEquals(15.0, trend.getShelfLifeEstimatedMonths(), 0.1);
    }

    @Test
    void testGetPullEventsByStudyId() {
        List<StabilityPullEvent> events = List.of(
                StabilityPullEvent.builder().id(101L).studyId(1L).scheduledDate(LocalDate.now()).status("SCHEDULED").build()
        );
        when(pullEventRepository.findByStudyIdOrderByScheduledDateAsc(1L)).thenReturn(events);

        List<StabilityPullEventResponse> res = stabilityService.getPullEventsByStudyId(1L);

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(101L, res.get(0).getId());
    }
}
