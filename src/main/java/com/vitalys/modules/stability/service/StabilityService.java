package com.vitalys.modules.stability.service;

import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.stability.dto.*;
import com.vitalys.modules.stability.entity.*;
import com.vitalys.modules.stability.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StabilityService {

    private final StabilityStudyRepository studyRepository;
    private final StabilityStorageConditionRepository conditionRepository;
    private final StabilityTimePointRepository timePointRepository;
    private final StabilityPullEventRepository pullEventRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final SampleRepository sampleRepository;
    private final TestRequestRepository testRequestRepository;

    // ── Stability Study Management ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StabilityStudyResponse> searchStudies(String status, Long productId, String search, Pageable pageable) {
        return studyRepository.searchStudies(status, productId, search, pageable)
                .map(this::mapToStudyResponse);
    }

    @Transactional(readOnly = true)
    public StabilityStudyResponse getStudyById(Long id) {
        StabilityStudy study = studyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nghiên cứu độ ổn định ID: " + id));
        return mapToStudyResponse(study);
    }

    @Transactional
    public StabilityStudyResponse createStudy(StabilityStudyCreateRequest req, String username) {
        String studyCode = "STB-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        StabilityStudy study = StabilityStudy.builder()
                .studyCode(studyCode)
                .studyTitle(req.getStudyTitle())
                .productId(req.getProductId())
                .batchId(req.getBatchId())
                .studyType(req.getStudyType())
                .protocolNumber(req.getProtocolNumber())
                .durationMonths(req.getDurationMonths())
                .startDate(req.getStartDate())
                .status("ACTIVE")
                .createdBy(username != null ? username : "supervisor")
                .notes(req.getNotes())
                .build();

        study = studyRepository.save(study);

        // Save storage conditions
        List<StabilityStorageCondition> savedConditions = new ArrayList<>();
        for (StabilityStudyCreateRequest.StorageConditionCreateDto condDto : req.getConditions()) {
            StabilityStorageCondition cond = StabilityStorageCondition.builder()
                    .studyId(study.getId())
                    .chamberName(condDto.getChamberName())
                    .temperatureCelsius(condDto.getTemperatureCelsius())
                    .temperatureTolerance(condDto.getTemperatureTolerance() != null ? condDto.getTemperatureTolerance() : 2.0)
                    .relativeHumidity(condDto.getRelativeHumidity())
                    .humidityTolerance(condDto.getHumidityTolerance() != null ? condDto.getHumidityTolerance() : 5.0)
                    .lightCondition(condDto.getLightCondition() != null ? condDto.getLightCondition() : "DARK")
                    .shelfLocation(condDto.getShelfLocation())
                    .build();
            savedConditions.add(conditionRepository.save(cond));
        }

        // Save time points
        List<StabilityTimePoint> savedTimePoints = new ArrayList<>();
        for (StabilityStudyCreateRequest.TimePointCreateDto tpDto : req.getTimePoints()) {
            StabilityTimePoint tp = StabilityTimePoint.builder()
                    .studyId(study.getId())
                    .pointLabel(tpDto.getPointLabel())
                    .monthOffset(tpDto.getMonthOffset())
                    .toleranceDays(tpDto.getToleranceDays() != null ? tpDto.getToleranceDays() : 7)
                    .testRegime(tpDto.getTestRegime())
                    .build();
            savedTimePoints.add(timePointRepository.save(tp));
        }

        // Automated Matrix Generation: Storage Conditions x Time Points -> Pull Events
        LocalDate startLocalDate = req.getStartDate().toLocalDate();
        for (StabilityStorageCondition cond : savedConditions) {
            for (StabilityTimePoint tp : savedTimePoints) {
                LocalDate scheduledDate = startLocalDate.plusMonths(tp.getMonthOffset());
                int tol = tp.getToleranceDays() != null ? tp.getToleranceDays() : 7;
                LocalDate windowStart = scheduledDate.minusDays(tol);
                LocalDate windowEnd = scheduledDate.plusDays(tol);

                StabilityPullEvent event = StabilityPullEvent.builder()
                        .studyId(study.getId())
                        .timePointId(tp.getId())
                        .storageConditionId(cond.getId())
                        .scheduledDate(scheduledDate)
                        .windowStart(windowStart)
                        .windowEnd(windowEnd)
                        .status(scheduledDate.isEqual(LocalDate.now()) ? "PENDING_PULL" : "SCHEDULED")
                        .notes(String.format("Lịch rút mẫu mốc %s tại buồng %s", tp.getPointLabel(), cond.getChamberName()))
                        .build();
                pullEventRepository.save(event);
            }
        }

        log.info("Initialized Stability Study [{}] with {} conditions x {} timepoints = {} pull events",
                study.getStudyCode(), savedConditions.size(), savedTimePoints.size(), savedConditions.size() * savedTimePoints.size());

        return mapToStudyResponse(study);
    }

    // ── Sample Pull Events & Testing Trigger ───────────────────────────────────

    @Transactional(readOnly = true)
    public Page<StabilityPullEventResponse> searchPullEvents(Long studyId, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return pullEventRepository.searchPullEvents(studyId, status, fromDate, toDate, pageable)
                .map(this::mapToPullEventResponse);
    }

    @Transactional
    public StabilityPullEventResponse executePull(Long eventId, StabilityPullActionRequest req, String username) {
        StabilityPullEvent event = pullEventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sự kiện rút mẫu ID: " + eventId));

        event.setStatus("PULLED");
        event.setPullDate(OffsetDateTime.now());
        event.setPulledBy(username != null ? username : "operator");

        if (req.getAssayResult() != null) {
            event.setAssayResult(req.getAssayResult());
        }
        if (req.getDissolutionResult() != null) {
            event.setDissolutionResult(req.getDissolutionResult());
        }
        if (req.getNotes() != null) {
            event.setNotes(req.getNotes());
        }

        // Automated Test Request Generation if requested
        if (Boolean.TRUE.equals(req.getCreateTestRequest())) {
            StabilityStudy study = studyRepository.findById(event.getStudyId()).orElse(null);
            StabilityTimePoint tp = timePointRepository.findById(event.getTimePointId()).orElse(null);

            if (study != null) {
                // Create Sample
                String sampleCode = "SMP-STB-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                Sample sample = Sample.builder()
                        .sampleCode(sampleCode)
                        .productId(study.getProductId())
                        .batchId(study.getBatchId())
                        .status("RECEIVED")
                        .storageCondition("Tủ mẫu ổn định")
                        .notes(String.format("Mẫu rút độ ổn định study %s - Mốc %s", study.getStudyCode(), tp != null ? tp.getPointLabel() : ""))
                        .build();
                sample = sampleRepository.save(sample);
                event.setSampleId(sample.getId());

                // Create TestRequest
                String requestCode = "REQ-STB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                TestRequest testReq = TestRequest.builder()
                        .requestCode(requestCode)
                        .requestType("STABILITY")
                        .productId(study.getProductId())
                        .batchId(study.getBatchId())
                        .priority("NORMAL")
                        .status("SUBMITTED")
                        .build();
                testReq = testRequestRepository.save(testReq);
                event.setTestRequestId(testReq.getId());
                event.setStatus("IN_TESTING");
            }
        }

        event = pullEventRepository.save(event);
        log.info("Executed sample pull event [{}] for Study [{}] by {}", event.getId(), event.getStudyId(), username);
        return mapToPullEventResponse(event);
    }

    // ── Statistical Trend Analysis & Shelf-life Estimation ────────────────────

    @Transactional(readOnly = true)
    public StabilityTrendResponse calculateTrend(Long studyId, Long conditionId) {
        StabilityStudy study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy study ID: " + studyId));

        Product product = study.getProductId() != null ? productRepository.findById(study.getProductId()).orElse(null) : null;
        Batch batch = study.getBatchId() != null ? batchRepository.findById(study.getBatchId()).orElse(null) : null;

        List<StabilityStorageCondition> conditions = conditionRepository.findByStudyId(studyId);
        StabilityStorageCondition condition = conditions.stream()
                .filter(c -> conditionId == null || c.getId().equals(conditionId))
                .findFirst()
                .orElse(conditions.isEmpty() ? null : conditions.get(0));

        if (condition == null) {
            throw new NoSuchElementException("Không có điều kiện bảo quản nào cho study này.");
        }

        List<StabilityPullEvent> events = pullEventRepository
                .findByStudyIdAndStorageConditionIdOrderByScheduledDateAsc(studyId, condition.getId());

        List<StabilityTrendResponse.DataPoint> assayPoints = new ArrayList<>();
        List<StabilityTrendResponse.DataPoint> dissolutionPoints = new ArrayList<>();

        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        for (StabilityPullEvent e : events) {
            StabilityTimePoint tp = timePointRepository.findById(e.getTimePointId()).orElse(null);
            int month = tp != null ? tp.getMonthOffset() : 0;
            String label = tp != null ? tp.getPointLabel() : (month + "M");

            if (e.getAssayResult() != null) {
                assayPoints.add(StabilityTrendResponse.DataPoint.builder()
                        .monthOffset(month)
                        .pointLabel(label)
                        .value(e.getAssayResult())
                        .pullDate(e.getPullDate() != null ? e.getPullDate().toString() : null)
                        .status(e.getStatus())
                        .build());
                xList.add((double) month);
                yList.add(e.getAssayResult());
            }

            if (e.getDissolutionResult() != null) {
                dissolutionPoints.add(StabilityTrendResponse.DataPoint.builder()
                        .monthOffset(month)
                        .pointLabel(label)
                        .value(e.getDissolutionResult())
                        .pullDate(e.getPullDate() != null ? e.getPullDate().toString() : null)
                        .status(e.getStatus())
                        .build());
            }
        }

        // Linear Regression: y = slope * x + intercept
        Double slope = null;
        Double intercept = null;
        Double rSquared = null;
        Double shelfLifeMonths = null;

        int n = xList.size();
        if (n >= 2) {
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
            for (int i = 0; i < n; i++) {
                double x = xList.get(i);
                double y = yList.get(i);
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
                sumY2 += y * y;
            }

            double denominator = (n * sumX2 - sumX * sumX);
            if (denominator != 0) {
                slope = (n * sumXY - sumX * sumY) / denominator;
                intercept = (sumY - slope * sumX) / n;

                double rNumerator = (n * sumXY - sumX * sumY);
                double rDenom = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
                if (rDenom != 0) {
                    double r = rNumerator / rDenom;
                    rSquared = r * r;
                }

                // If degradation slope is negative, calculate month when assay reaches 95.0%
                if (slope < 0) {
                    shelfLifeMonths = Math.max(0.0, (95.0 - intercept) / slope);
                } else {
                    shelfLifeMonths = 36.0; // stable over 36 months
                }
            }
        }

        String condLabel = String.format("%s (%.1f°C / %.1f%%RH)",
                condition.getChamberName(), condition.getTemperatureCelsius(),
                condition.getRelativeHumidity() != null ? condition.getRelativeHumidity() : 0.0);

        return StabilityTrendResponse.builder()
                .studyId(study.getId())
                .studyCode(study.getStudyCode())
                .productName(product != null ? product.getProductName() : "N/A")
                .batchNumber(batch != null ? batch.getBatchNumber() : "N/A")
                .conditionId(condition.getId())
                .chamberName(condition.getChamberName())
                .conditionLabel(condLabel)
                .specMin(95.0)
                .specMax(105.0)
                .assayPoints(assayPoints)
                .dissolutionPoints(dissolutionPoints)
                .slope(slope)
                .intercept(intercept)
                .rSquared(rSquared)
                .shelfLifeEstimatedMonths(shelfLifeMonths)
                .build();
    }

    // ── Mapping Helpers ───────────────────────────────────────────────────────

    private StabilityStudyResponse mapToStudyResponse(StabilityStudy s) {
        Product product = s.getProductId() != null ? productRepository.findById(s.getProductId()).orElse(null) : null;
        Batch batch = s.getBatchId() != null ? batchRepository.findById(s.getBatchId()).orElse(null) : null;

        List<StabilityStorageCondition> conditions = conditionRepository.findByStudyId(s.getId());
        List<StabilityTimePoint> timePoints = timePointRepository.findByStudyIdOrderByMonthOffsetAsc(s.getId());
        List<StabilityPullEvent> events = pullEventRepository.findByStudyIdOrderByScheduledDateAsc(s.getId());

        int total = events.size();
        int completed = (int) events.stream().filter(e -> "COMPLETED".equals(e.getStatus())).count();
        int pending = (int) events.stream().filter(e -> "PENDING_PULL".equals(e.getStatus()) || "SCHEDULED".equals(e.getStatus())).count();

        List<StabilityStudyResponse.StorageConditionDto> condDtos = conditions.stream()
                .map(c -> StabilityStudyResponse.StorageConditionDto.builder()
                        .id(c.getId())
                        .chamberName(c.getChamberName())
                        .temperatureCelsius(c.getTemperatureCelsius())
                        .temperatureTolerance(c.getTemperatureTolerance())
                        .relativeHumidity(c.getRelativeHumidity())
                        .humidityTolerance(c.getHumidityTolerance())
                        .lightCondition(c.getLightCondition())
                        .shelfLocation(c.getShelfLocation())
                        .build())
                .collect(Collectors.toList());

        List<StabilityStudyResponse.TimePointDto> tpDtos = timePoints.stream()
                .map(t -> StabilityStudyResponse.TimePointDto.builder()
                        .id(t.getId())
                        .pointLabel(t.getPointLabel())
                        .monthOffset(t.getMonthOffset())
                        .toleranceDays(t.getToleranceDays())
                        .testRegime(t.getTestRegime())
                        .build())
                .collect(Collectors.toList());

        return StabilityStudyResponse.builder()
                .id(s.getId())
                .studyCode(s.getStudyCode())
                .studyTitle(s.getStudyTitle())
                .productId(s.getProductId())
                .productCode(product != null ? product.getProductCode() : null)
                .productName(product != null ? product.getProductName() : null)
                .batchId(s.getBatchId())
                .batchNumber(batch != null ? batch.getBatchNumber() : null)
                .studyType(s.getStudyType())
                .protocolNumber(s.getProtocolNumber())
                .durationMonths(s.getDurationMonths())
                .startDate(s.getStartDate())
                .status(s.getStatus())
                .createdBy(s.getCreatedBy())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .conditions(condDtos)
                .timePoints(tpDtos)
                .totalPullEvents(total)
                .completedPullEvents(completed)
                .pendingPullEvents(pending)
                .build();
    }

    private StabilityPullEventResponse mapToPullEventResponse(StabilityPullEvent e) {
        StabilityStudy study = studyRepository.findById(e.getStudyId()).orElse(null);
        StabilityTimePoint tp = timePointRepository.findById(e.getTimePointId()).orElse(null);
        StabilityStorageCondition sc = conditionRepository.findById(e.getStorageConditionId()).orElse(null);
        Sample sample = e.getSampleId() != null ? sampleRepository.findById(e.getSampleId()).orElse(null) : null;

        LocalDate today = LocalDate.now();
        boolean isOverdue = "SCHEDULED".equals(e.getStatus()) && e.getScheduledDate().isBefore(today);
        boolean isDueToday = "SCHEDULED".equals(e.getStatus()) && e.getScheduledDate().isEqual(today);

        return StabilityPullEventResponse.builder()
                .id(e.getId())
                .studyId(e.getStudyId())
                .studyCode(study != null ? study.getStudyCode() : "N/A")
                .studyTitle(study != null ? study.getStudyTitle() : "N/A")
                .timePointId(e.getTimePointId())
                .pointLabel(tp != null ? tp.getPointLabel() : "N/A")
                .monthOffset(tp != null ? tp.getMonthOffset() : 0)
                .storageConditionId(e.getStorageConditionId())
                .chamberName(sc != null ? sc.getChamberName() : "N/A")
                .temperatureCelsius(sc != null ? sc.getTemperatureCelsius() : null)
                .relativeHumidity(sc != null ? sc.getRelativeHumidity() : null)
                .scheduledDate(e.getScheduledDate())
                .windowStart(e.getWindowStart())
                .windowEnd(e.getWindowEnd())
                .pullDate(e.getPullDate())
                .pulledBy(e.getPulledBy())
                .sampleId(e.getSampleId())
                .sampleCode(sample != null ? sample.getSampleCode() : null)
                .testRequestId(e.getTestRequestId())
                .status(e.getStatus())
                .assayResult(e.getAssayResult())
                .dissolutionResult(e.getDissolutionResult())
                .notes(e.getNotes())
                .isOverdue(isOverdue)
                .isDueToday(isDueToday)
                .build();
    }
}
