package com.vitalys.modules.sample.service;

import com.vitalys.modules.method.entity.SpecificationSet;
import com.vitalys.modules.method.repository.SpecificationSetRepository;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.dto.*;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.SampleChainOfCustody;
import com.vitalys.modules.sample.entity.SampleStatusHistory;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleChainOfCustodyRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.SampleStatusHistoryRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.sys.entity.SysDepartment;
import com.vitalys.modules.sys.repository.SysDepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleService {

    private final SampleRepository sampleRepository;
    private final TestRequestRepository testRequestRepository;
    private final SampleStatusHistoryRepository statusHistoryRepository;
    private final SampleChainOfCustodyRepository custodyRepository;
    private final SysDepartmentRepository departmentRepository;
    private final SpecificationSetRepository specSetRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;

    // ── Valid State Transitions ───────────────────────────────────────────────
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "SUBMITTED", Set.of("RECEIVED", "CANCELLED"),
            "RECEIVED",  Set.of("ASSIGNED", "TESTING", "DISPOSED"),
            "ASSIGNED",  Set.of("TESTING", "RECEIVED"),
            "TESTING",   Set.of("REVIEWED", "REJECTED"),
            "REVIEWED",  Set.of("APPROVED", "REJECTED", "TESTING"),
            "APPROVED",  Set.of("DISPOSED"),
            "REJECTED",  Set.of("DISPOSED")
    );

    @Transactional(readOnly = true)
    public Page<SampleResponse> getSamples(String search, String status, Long departmentId,
                                          Long requestId, Pageable pageable) {
        Specification<Sample> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("departmentId"), departmentId));
            }
            if (requestId != null) {
                predicates.add(cb.equal(root.get("requestId"), requestId));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("sampleCode")), term),
                        cb.like(cb.lower(root.get("barcode")), term),
                        cb.like(cb.lower(root.get("currentLocation")), term),
                        cb.like(cb.lower(root.get("assignedTo")), term)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Sample> page = sampleRepository.findAll(spec, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SampleResponse getSampleById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public SampleResponse getSampleByBarcode(String barcode) {
        Sample sample = sampleRepository.findByBarcode(barcode)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with barcode: " + barcode));
        return toResponse(sample);
    }

    @Transactional(readOnly = true)
    public List<SampleStatusHistoryResponse> getStatusHistory(Long sampleId) {
        findOrThrow(sampleId);
        return statusHistoryRepository.findBySampleIdOrderByChangedAtDesc(sampleId).stream()
                .map(h -> SampleStatusHistoryResponse.builder()
                        .id(h.getId())
                        .sampleId(h.getSampleId())
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .reason(h.getReason())
                        .build())
                .toList();
    }

    /**
     * Accession one or more physical sample containers for a test request.
     * Generates standard sample codes (SMP-YYYYMMDD-XXXX), barcodes, and initial custody log.
     */
    @Auditable(module = "SAMPLE", entity = "Sample")
    @Transactional
    public List<SampleResponse> accessionSamples(SampleAccessionRequest request) {
        TestRequest testRequest = testRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new EntityNotFoundException("Test request not found with ID: " + request.getRequestId()));

        String currentUser = getCurrentUsername();
        OffsetDateTime now = OffsetDateTime.now();
        int count = Math.max(1, request.getContainerCount());
        List<Sample> createdSamples = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String sampleCode = generateSampleCode();
            String barcode = "BAR-" + sampleCode;

            Long specSetId = request.getSpecSetId() != null ? request.getSpecSetId() : testRequest.getSpecSetId();

            Sample sample = Sample.builder()
                    .requestId(testRequest.getId())
                    .departmentId(request.getDepartmentId())
                    .specSetId(specSetId)
                    .sampleCode(sampleCode)
                    .barcode(barcode)
                    .status("RECEIVED")
                    .storageCondition(request.getStorageCondition())
                    .currentLocation(request.getCurrentLocation())
                    .quantity(request.getQuantity())
                    .unit(request.getUnit())
                    .receivedBy(currentUser)
                    .assignedTo(request.getAssignedTo())
                    .receivedAt(now)
                    .samplingDate(request.getSamplingDate())
                    .samplingLocation(request.getSamplingLocation())
                    .notes(request.getNotes())
                    .build();

            Sample saved = sampleRepository.save(sample);

            // Log initial status change
            SampleStatusHistory history = SampleStatusHistory.builder()
                    .sampleId(saved.getId())
                    .fromStatus("SUBMITTED")
                    .toStatus("RECEIVED")
                    .changedBy(currentUser)
                    .changedAt(now)
                    .reason("Physical accessioning into lab registry (Container #" + (i + 1) + " of " + count + ")")
                    .build();
            statusHistoryRepository.save(history);

            // Log initial Chain of Custody
            SampleChainOfCustody custody = SampleChainOfCustody.builder()
                    .sampleId(saved.getId())
                    .fromUser(testRequest.getRequestedBy() != null ? testRequest.getRequestedBy() : "Sampler / Courier")
                    .toUser(currentUser)
                    .fromLocation("External / Sampling Site")
                    .toLocation(request.getCurrentLocation())
                    .transferredAt(now)
                    .purpose("Sample Accessioning & Intake Inspection")
                    .sampleCondition("INTACT")
                    .storageCondition(request.getStorageCondition())
                    .notes("Intake check passed, barcode " + barcode + " affixed")
                    .build();
            custodyRepository.save(custody);

            createdSamples.add(saved);
        }

        // Update Test Request status to RECEIVED
        if ("SUBMITTED".equals(testRequest.getStatus())) {
            testRequest.setStatus("RECEIVED");
            testRequestRepository.save(testRequest);
        }

        log.info("Accessioned {} samples for TestRequest {}: code prefix {}",
                createdSamples.size(), testRequest.getRequestCode(), createdSamples.get(0).getSampleCode());

        return createdSamples.stream().map(this::toResponse).toList();
    }

    /**
     * Enforce State Machine transition with mandatory 21 CFR Part 11 reason.
     */
    @Auditable(module = "SAMPLE", entity = "Sample")
    @Transactional
    public SampleResponse changeSampleStatus(Long id, SampleStatusChangeRequest request) {
        Sample sample = findOrThrow(id);
        String currentStatus = sample.getStatus();
        String targetStatus = request.getTargetStatus().trim().toUpperCase();

        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(targetStatus)) {
            throw new IllegalStateException(String.format(
                    "Invalid state transition from [%s] to [%s]. Allowed transitions: %s",
                    currentStatus, targetStatus, allowed));
        }

        String currentUser = getCurrentUsername();
        OffsetDateTime now = OffsetDateTime.now();

        sample.setStatus(targetStatus);
        Sample updated = sampleRepository.save(sample);

        SampleStatusHistory history = SampleStatusHistory.builder()
                .sampleId(id)
                .fromStatus(currentStatus)
                .toStatus(targetStatus)
                .changedBy(currentUser)
                .changedAt(now)
                .reason(request.getReason())
                .build();
        statusHistoryRepository.save(history);

        log.info("Sample ID {} transitioned from [{}] to [{}] by user {}. Reason: {}",
                id, currentStatus, targetStatus, currentUser, request.getReason());

        return toResponse(updated);
    }

    @Auditable(module = "SAMPLE", entity = "Sample")
    @Transactional
    public SampleResponse updateSample(Long id, SampleUpdateRequest request) {
        Sample sample = findOrThrow(id);

        if (request.getDepartmentId() != null) sample.setDepartmentId(request.getDepartmentId());
        if (request.getSpecSetId() != null) sample.setSpecSetId(request.getSpecSetId());
        if (request.getStorageCondition() != null) sample.setStorageCondition(request.getStorageCondition());
        if (request.getCurrentLocation() != null) sample.setCurrentLocation(request.getCurrentLocation());
        if (request.getAssignedTo() != null) sample.setAssignedTo(request.getAssignedTo());
        if (request.getQuantity() != null) sample.setQuantity(request.getQuantity());
        if (request.getUnit() != null) sample.setUnit(request.getUnit());
        if (request.getSamplingDate() != null) sample.setSamplingDate(request.getSamplingDate());
        if (request.getSamplingLocation() != null) sample.setSamplingLocation(request.getSamplingLocation());
        if (request.getNotes() != null) sample.setNotes(request.getNotes());

        Sample updated = sampleRepository.save(sample);
        log.info("Updated sample metadata for ID {}: {}", id, updated.getSampleCode());
        return toResponse(updated);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Sample findOrThrow(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with ID: " + id));
    }

    private synchronized String generateSampleCode() {
        String prefix = "SMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int seq = 1;
        String candidate;
        do {
            candidate = String.format("%s%04d", prefix, seq++);
        } while (sampleRepository.existsBySampleCode(candidate));
        return candidate;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    private SampleResponse toResponse(Sample s) {
        String requestCode = null;
        String productName = null;
        String batchNumber = null;

        TestRequest req = testRequestRepository.findById(s.getRequestId()).orElse(null);
        if (req != null) {
            requestCode = req.getRequestCode();
            if (req.getProductId() != null) {
                productName = productRepository.findById(req.getProductId())
                        .map(Product::getProductName).orElse(null);
            }
            if (req.getBatchId() != null) {
                batchNumber = batchRepository.findById(req.getBatchId())
                        .map(Batch::getBatchNumber).orElse(null);
            }
        }

        String deptName = null;
        if (s.getDepartmentId() != null) {
            deptName = departmentRepository.findById(s.getDepartmentId())
                    .map(SysDepartment::getName).orElse(null);
        }

        String specCode = null;
        String specSetName = null;
        if (s.getSpecSetId() != null) {
            SpecificationSet spec = specSetRepository.findById(s.getSpecSetId()).orElse(null);
            if (spec != null) {
                specCode = spec.getSpecCode();
                specSetName = spec.getName();
            }
        }

        return SampleResponse.builder()
                .id(s.getId())
                .requestId(s.getRequestId())
                .requestCode(requestCode)
                .productName(productName)
                .batchNumber(batchNumber)
                .departmentId(s.getDepartmentId())
                .departmentName(deptName)
                .specSetId(s.getSpecSetId())
                .specCode(specCode)
                .specSetName(specSetName)
                .sampleCode(s.getSampleCode())
                .barcode(s.getBarcode())
                .status(s.getStatus())
                .storageCondition(s.getStorageCondition())
                .currentLocation(s.getCurrentLocation())
                .quantity(s.getQuantity())
                .unit(s.getUnit())
                .receivedBy(s.getReceivedBy())
                .assignedTo(s.getAssignedTo())
                .receivedAt(s.getReceivedAt())
                .samplingDate(s.getSamplingDate())
                .samplingLocation(s.getSamplingLocation())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
