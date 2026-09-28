package com.vitalys.modules.testing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalys.modules.equipment.entity.Instrument;
import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.method.entity.Method;
import com.vitalys.modules.method.entity.SpecificationItem;
import com.vitalys.modules.method.repository.MethodRepository;
import com.vitalys.modules.method.repository.SpecificationItemRepository;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.testing.dto.*;
import com.vitalys.modules.testing.entity.*;
import com.vitalys.modules.testing.repository.*;
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
public class TestingService {

    private final TestEntityRepository testRepository;
    private final ResultRepository resultRepository;
    private final TestResultRevisionRepository revisionRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final AnalyticalRunRepository runRepository;
    private final OosInvestigationRepository oosRepository;
    private final SampleRepository sampleRepository;
    private final TestRequestRepository testRequestRepository;
    private final MethodRepository methodRepository;
    private final SpecificationItemRepository specItemRepository;
    private final InstrumentRepository instrumentRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ObjectMapper objectMapper;

    // ── Worklist & Test Queries ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TestResponse> getWorklist(String analyst, String status, String priority,
                                         String search, Pageable pageable) {
        Specification<TestEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (analyst != null && !analyst.isBlank()) {
                predicates.add(cb.equal(root.get("assignedTo"), analyst.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (priority != null && !priority.isBlank()) {
                predicates.add(cb.equal(root.get("priority"), priority.trim()));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("testCode")), term),
                        cb.like(cb.lower(root.get("notes")), term)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, "dueDate").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        return testRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TestResponse getTestById(Long id) {
        TestEntity test = findOrThrow(id);
        return toResponse(test);
    }

    @Transactional(readOnly = true)
    public List<TestResponse> getTestsBySampleId(Long sampleId) {
        return testRepository.findBySampleId(sampleId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Test Assignment & Lifecycle ───────────────────────────────────────────

    @Auditable(module = "TESTING", entity = "TestEntity")
    @Transactional
    public TestResponse assignTest(TestAssignmentRequest request) {
        Sample sample = sampleRepository.findById(request.getSampleId())
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with ID: " + request.getSampleId()));

        methodRepository.findById(request.getMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Method not found with ID: " + request.getMethodId()));

        String code = generateTestCode();
        String priority = request.getPriority() != null ? request.getPriority() : "NORMAL";

        TestEntity test = TestEntity.builder()
                .testCode(code)
                .sampleId(request.getSampleId())
                .methodId(request.getMethodId())
                .specItemId(request.getSpecItemId())
                .formTemplateId(request.getFormTemplateId())
                .assignedTo(request.getAssignedTo().trim())
                .priority(priority)
                .dueDate(request.getDueDate() != null ? request.getDueDate() : OffsetDateTime.now().plusDays(2))
                .status("ASSIGNED")
                .notes(request.getNotes())
                .build();

        TestEntity saved = testRepository.save(test);

        // Update sample status to ASSIGNED if currently RECEIVED
        if ("RECEIVED".equals(sample.getStatus())) {
            sample.setStatus("ASSIGNED");
            sample.setAssignedTo(request.getAssignedTo().trim());
            sampleRepository.save(sample);
        }

        log.info("Assigned test {} for sample {} to analyst {}",
                saved.getTestCode(), sample.getSampleCode(), saved.getAssignedTo());

        return toResponse(saved);
    }

    @Auditable(module = "TESTING", entity = "TestEntity")
    @Transactional
    public TestResponse startTest(Long id) {
        TestEntity test = findOrThrow(id);
        test.setStatus("IN_PROGRESS");
        test.setStartedAt(OffsetDateTime.now());

        // Update Sample status to TESTING
        sampleRepository.findById(test.getSampleId()).ifPresent(s -> {
            if (!"TESTING".equals(s.getStatus())) {
                s.setStatus("TESTING");
                sampleRepository.save(s);
            }
        });

        TestEntity updated = testRepository.save(test);
        log.info("Test {} started by user {}", updated.getTestCode(), getCurrentUsername());
        return toResponse(updated);
    }

    @Auditable(module = "TESTING", entity = "TestEntity")
    @Transactional
    public TestResponse completeTest(Long id) {
        TestEntity test = findOrThrow(id);
        test.setStatus("COMPLETED");
        test.setCompletedAt(OffsetDateTime.now());
        TestEntity updated = testRepository.save(test);
        log.info("Test {} completed by user {}", updated.getTestCode(), getCurrentUsername());
        return toResponse(updated);
    }

    @Auditable(module = "TESTING", entity = "TestEntity")
    @Transactional
    public TestResponse reassignTest(Long id, TestAssignmentRequest request) {
        TestEntity test = findOrThrow(id);
        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            test.setAssignedTo(request.getAssignedTo().trim());
        }
        if (request.getPriority() != null) {
            test.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            test.setDueDate(request.getDueDate());
        }
        if (request.getNotes() != null) {
            test.setNotes(request.getNotes());
        }
        test.setStatus("ASSIGNED");
        TestEntity updated = testRepository.save(test);
        log.info("Test {} reassigned to {}", updated.getTestCode(), updated.getAssignedTo());
        return toResponse(updated);
    }

    // ── Result Entry & OOS Evaluation ─────────────────────────────────────────

    @Auditable(module = "TESTING", entity = "Result")
    @Transactional
    public ResultResponse enterResult(ResultEntryRequest request) {
        TestEntity test = findOrThrow(request.getTestId());
        String currentUser = getCurrentUsername();
        OffsetDateTime now = OffsetDateTime.now();

        // 1. If dynamic form data was provided, persist to FormSubmission
        if (request.getFormData() != null && !request.getFormData().isEmpty()) {
            try {
                FormSubmission submission = FormSubmission.builder()
                        .testId(test.getId())
                        .submittedBy(currentUser)
                        .submittedAt(now)
                        .data(objectMapper.writeValueAsString(request.getFormData()))
                        .build();
                formSubmissionRepository.save(submission);
            } catch (Exception e) {
                log.warn("Failed to persist form submission: {}", e.getMessage());
            }
        }

        // 2. Fetch specification criteria if available
        Double specMin = null;
        Double specMax = null;
        String specTarget = null;
        String unit = request.getUnit();

        if (test.getSpecItemId() != null) {
            SpecificationItem item = specItemRepository.findById(test.getSpecItemId()).orElse(null);
            if (item != null) {
                specMin = item.getMinLimit();
                specMax = item.getMaxLimit();
                specTarget = item.getTextAcceptanceCriteria();
                if (unit == null || unit.isBlank()) {
                    unit = item.getUnit();
                }
            }
        }

        // 3. Evaluate Pass / Fail and OOS
        boolean isOos = false;
        String passFail = "PASS";

        if (request.getValue() != null) {
            double val = request.getValue();
            if (specMin != null && val < specMin) {
                isOos = true;
                passFail = "FAIL";
            }
            if (specMax != null && val > specMax) {
                isOos = true;
                passFail = "FAIL";
            }
        } else if (specTarget != null && !specTarget.isBlank()) {
            if (request.getTextValue() != null &&
                !request.getTextValue().trim().equalsIgnoreCase(specTarget.trim()) &&
                !request.getTextValue().trim().equalsIgnoreCase("CONFORMS") &&
                !request.getTextValue().trim().equalsIgnoreCase("PASS")) {
                isOos = true;
                passFail = "FAIL";
            }
        }

        // 4. Save Result
        Result result = Result.builder()
                .testId(test.getId())
                .analyte(request.getAnalyte() != null ? request.getAnalyte() : "Assay Result")
                .value(request.getValue())
                .textValue(request.getTextValue() != null ? request.getTextValue() : (request.getValue() != null ? request.getValue().toString() : null))
                .unit(unit)
                .specMin(specMin)
                .specMax(specMax)
                .specTarget(specTarget)
                .passFail(passFail)
                .isOos(isOos)
                .enteredBy(currentUser)
                .enteredAt(now)
                .build();

        Result savedResult = resultRepository.save(result);

        // 5. If Out of Specification detected, automatically create Phase 1 Lab Investigation
        if (isOos) {
            String oosCode = generateOosCode();
            OosInvestigation oos = OosInvestigation.builder()
                    .investigationCode(oosCode)
                    .resultId(savedResult.getId())
                    .testId(test.getId())
                    .sampleId(test.getSampleId())
                    .phase("PHASE_1_LAB")
                    .immediateAction("Testing halted. Retain all stock solutions, sample aliquots, HPLC vials, and glassware for inspection.")
                    .investigationFindings("Out-of-Specification (OOS) result detected: " +
                            (savedResult.getValue() != null ? savedResult.getValue() + " " + unit : savedResult.getTextValue()) +
                            ". Acceptance criteria: " + (specMin != null ? "Min: " + specMin + ", " : "") + (specMax != null ? "Max: " + specMax : specTarget))
                    .investigatedBy(currentUser)
                    .investigatedAt(now)
                    .retestApproved(false)
                    .status("OPEN")
                    .build();

            OosInvestigation savedOos = oosRepository.save(oos);
            savedResult.setOosInvestigationId(savedOos.getId());
            resultRepository.save(savedResult);

            log.warn("CRITICAL: Out of Specification (OOS) triggered for test {} [Sample ID {}]: Code {}",
                    test.getTestCode(), test.getSampleId(), oosCode);
        }

        // 6. Complete test
        if (request.getRunId() != null) {
            test.setRunId(request.getRunId());
        }
        test.setStatus("COMPLETED");
        test.setCompletedAt(now);
        testRepository.save(test);

        log.info("Entered result for test {}: value={}, OOS={}, PassFail={}",
                test.getTestCode(), savedResult.getValue(), isOos, passFail);

        return toResultResponse(savedResult);
    }

    // ── Result Revision (ALCOA+ Non-destructive Auditing) ──────────────────────

    @Auditable(module = "TESTING", entity = "TestResultRevision")
    @Transactional
    public ResultResponse reviseResult(Long resultId, ResultRevisionRequest request) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new EntityNotFoundException("Result not found with ID: " + resultId));

        String currentUser = getCurrentUsername();
        OffsetDateTime now = OffsetDateTime.now();
        Double oldVal = result.getValue();

        // 1. Create immutable revision audit record
        TestResultRevision revision = TestResultRevision.builder()
                .resultId(resultId)
                .oldValue(oldVal)
                .newValue(request.getNewValue())
                .revisedBy(currentUser)
                .revisedAt(now)
                .reason(request.getReason().trim())
                .build();
        revisionRepository.save(revision);

        // 2. Re-evaluate OOS against specification
        boolean isOos = false;
        String passFail = "PASS";
        Double newVal = request.getNewValue();

        if (newVal != null) {
            if (result.getSpecMin() != null && newVal < result.getSpecMin()) {
                isOos = true;
                passFail = "FAIL";
            }
            if (result.getSpecMax() != null && newVal > result.getSpecMax()) {
                isOos = true;
                passFail = "FAIL";
            }
        }

        result.setValue(newVal);
        if (request.getNewTextValue() != null) {
            result.setTextValue(request.getNewTextValue());
        } else if (newVal != null) {
            result.setTextValue(newVal.toString());
        }
        result.setIsOos(isOos);
        result.setPassFail(passFail);
        result.setEnteredBy(currentUser);
        result.setEnteredAt(now);

        Result updated = resultRepository.save(result);

        log.info("Revised result ID {} by user {}: {} -> {} (Reason: {})",
                resultId, currentUser, oldVal, newVal, request.getReason());

        return toResultResponse(updated);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TestEntity findOrThrow(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with ID: " + id));
    }

    private synchronized String generateTestCode() {
        String prefix = "TST-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int seq = 1;
        String candidate;
        do {
            candidate = String.format("%s%04d", prefix, seq++);
        } while (testRepository.existsByTestCode(candidate));
        return candidate;
    }

    private synchronized String generateOosCode() {
        String prefix = "OOS-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int seq = 1;
        String candidate;
        do {
            candidate = String.format("%s%04d", prefix, seq++);
        } while (oosRepository.existsByInvestigationCode(candidate));
        return candidate;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    private TestResponse toResponse(TestEntity t) {
        String sampleCode = null;
        String barcode = null;
        String productName = null;
        String batchNumber = null;

        Sample s = sampleRepository.findById(t.getSampleId()).orElse(null);
        if (s != null) {
            sampleCode = s.getSampleCode();
            barcode = s.getBarcode();

            TestRequest req = testRequestRepository.findById(s.getRequestId()).orElse(null);
            if (req != null) {
                if (req.getProductId() != null) {
                    productName = productRepository.findById(req.getProductId())
                            .map(Product::getProductName).orElse(null);
                }
                if (req.getBatchId() != null) {
                    batchNumber = batchRepository.findById(req.getBatchId())
                            .map(Batch::getBatchNumber).orElse(null);
                }
            }
        }

        String methodCode = null;
        String methodName = null;
        if (t.getMethodId() != null) {
            Method m = methodRepository.findById(t.getMethodId()).orElse(null);
            if (m != null) {
                methodCode = m.getMethodCode();
                methodName = m.getName();
            }
        }

        String specParam = null;
        Double specMin = null;
        Double specMax = null;
        String specTarget = null;
        String specUnit = null;
        if (t.getSpecItemId() != null) {
            SpecificationItem item = specItemRepository.findById(t.getSpecItemId()).orElse(null);
            if (item != null) {
                specParam = item.getParameterName();
                specMin = item.getMinLimit();
                specMax = item.getMaxLimit();
                specTarget = item.getTextAcceptanceCriteria();
                specUnit = item.getUnit();
            }
        }

        String runCode = null;
        if (t.getRunId() != null) {
            runCode = runRepository.findById(t.getRunId()).map(AnalyticalRun::getRunCode).orElse(null);
        }

        List<ResultResponse> results = resultRepository.findByTestId(t.getId()).stream()
                .map(this::toResultResponse)
                .toList();

        return TestResponse.builder()
                .id(t.getId())
                .testCode(t.getTestCode())
                .sampleId(t.getSampleId())
                .sampleCode(sampleCode)
                .barcode(barcode)
                .productName(productName)
                .batchNumber(batchNumber)
                .methodId(t.getMethodId())
                .methodCode(methodCode)
                .methodName(methodName)
                .specItemId(t.getSpecItemId())
                .specParameter(specParam)
                .specMin(specMin)
                .specMax(specMax)
                .specTarget(specTarget)
                .specUnit(specUnit)
                .runId(t.getRunId())
                .runCode(runCode)
                .formTemplateId(t.getFormTemplateId())
                .assignedTo(t.getAssignedTo())
                .priority(t.getPriority())
                .dueDate(t.getDueDate())
                .startedAt(t.getStartedAt())
                .completedAt(t.getCompletedAt())
                .status(t.getStatus())
                .notes(t.getNotes())
                .results(results)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private ResultResponse toResultResponse(Result r) {
        List<TestResultRevisionResponse> revisions = revisionRepository.findByResultIdOrderByIdDesc(r.getId()).stream()
                .map(rev -> TestResultRevisionResponse.builder()
                        .id(rev.getId())
                        .resultId(rev.getResultId())
                        .oldValue(rev.getOldValue())
                        .newValue(rev.getNewValue())
                        .revisedBy(rev.getRevisedBy())
                        .revisedAt(rev.getRevisedAt())
                        .reason(rev.getReason())
                        .build())
                .toList();

        return ResultResponse.builder()
                .id(r.getId())
                .testId(r.getTestId())
                .analyte(r.getAnalyte())
                .value(r.getValue())
                .textValue(r.getTextValue())
                .unit(r.getUnit())
                .specMin(r.getSpecMin())
                .specMax(r.getSpecMax())
                .specTarget(r.getSpecTarget())
                .passFail(r.getPassFail())
                .isOos(r.getIsOos())
                .oosInvestigationId(r.getOosInvestigationId())
                .enteredBy(r.getEnteredBy())
                .enteredAt(r.getEnteredAt())
                .revisions(revisions)
                .build();
    }
}
