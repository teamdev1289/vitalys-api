package com.vitalys.modules.sample.service;

import com.vitalys.modules.method.entity.SpecificationSet;
import com.vitalys.modules.method.repository.SpecificationSetRepository;
import com.vitalys.modules.partner.entity.Customer;
import com.vitalys.modules.partner.entity.Project;
import com.vitalys.modules.partner.repository.CustomerRepository;
import com.vitalys.modules.partner.repository.ProjectRepository;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.dto.TestRequestCreateRequest;
import com.vitalys.modules.sample.dto.TestRequestResponse;
import com.vitalys.modules.sample.dto.TestRequestUpdateRequest;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.sys.annotation.Auditable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRequestService {

    private final TestRequestRepository testRequestRepository;
    private final SampleRepository sampleRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;
    private final SpecificationSetRepository specSetRepository;

    @Transactional(readOnly = true)
    public Page<TestRequestResponse> getRequests(String search, String status, String priority,
                                                 String sourceType, Pageable pageable) {
        Specification<TestRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (priority != null && !priority.isBlank()) {
                predicates.add(cb.equal(root.get("priority"), priority.trim()));
            }
            if (sourceType != null && !sourceType.isBlank()) {
                predicates.add(cb.equal(root.get("sourceType"), sourceType.trim()));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("requestCode")), term),
                        cb.like(cb.lower(root.get("requestedBy")), term),
                        cb.like(cb.lower(root.get("testScope")), term)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<TestRequest> page = testRequestRepository.findAll(spec, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TestRequestResponse getRequestById(Long id) {
        TestRequest req = findOrThrow(id);
        return toResponse(req);
    }

    @Auditable(module = "SAMPLE", entity = "TestRequest")
    @Transactional
    public TestRequestResponse createRequest(TestRequestCreateRequest request) {
        String currentUser = getCurrentUsername();
        String code = generateRequestCode();

        TestRequest req = TestRequest.builder()
                .requestCode(code)
                .sourceType(request.getSourceType())
                .sampleType(request.getSampleType())
                .productId(request.getProductId())
                .batchId(request.getBatchId())
                .processStage(request.getProcessStage())
                .formulationTrialId(request.getFormulationTrialId())
                .customerId(request.getCustomerId())
                .projectId(request.getProjectId())
                .specSetId(request.getSpecSetId())
                .requestedBy(currentUser)
                .requestDate(OffsetDateTime.now())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .testScope(request.getTestScope())
                .status("SUBMITTED")
                .notes(request.getNotes())
                .build();

        TestRequest saved = testRequestRepository.save(req);
        log.info("Created TestRequest: {} (ID: {})", saved.getRequestCode(), saved.getId());
        return toResponse(saved);
    }

    @Auditable(module = "SAMPLE", entity = "TestRequest")
    @Transactional
    public TestRequestResponse updateRequest(Long id, TestRequestUpdateRequest request) {
        TestRequest req = findOrThrow(id);
        if ("COMPLETED".equals(req.getStatus()) || "CANCELLED".equals(req.getStatus())) {
            throw new IllegalStateException("Cannot update a completed or cancelled test request");
        }

        if (request.getPriority() != null) req.setPriority(request.getPriority());
        if (request.getDueDate() != null) req.setDueDate(request.getDueDate());
        if (request.getTestScope() != null) req.setTestScope(request.getTestScope());
        if (request.getSpecSetId() != null) req.setSpecSetId(request.getSpecSetId());
        if (request.getNotes() != null) req.setNotes(request.getNotes());

        TestRequest updated = testRequestRepository.save(req);
        log.info("Updated TestRequest ID {}: {}", id, updated.getRequestCode());
        return toResponse(updated);
    }

    @Auditable(module = "SAMPLE", entity = "TestRequest")
    @Transactional
    public TestRequestResponse cancelRequest(Long id, String reason) {
        TestRequest req = findOrThrow(id);
        if ("COMPLETED".equals(req.getStatus())) {
            throw new IllegalStateException("Cannot cancel a completed test request");
        }
        int existingSamples = sampleRepository.findByRequestId(id).size();
        if (existingSamples > 0) {
            throw new IllegalStateException("Cannot cancel request: physical samples have already been accessioned");
        }

        req.setStatus("CANCELLED");
        req.setNotes((req.getNotes() != null ? req.getNotes() + "\n" : "") + "[CANCELLED] Reason: " + reason);
        TestRequest saved = testRequestRepository.save(req);
        log.info("Cancelled TestRequest ID {}: Reason={}", id, reason);
        return toResponse(saved);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TestRequest findOrThrow(Long id) {
        return testRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test request not found with ID: " + id));
    }

    private synchronized String generateRequestCode() {
        String prefix = "REQ-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int seq = 1;
        String candidate;
        do {
            candidate = String.format("%s%03d", prefix, seq++);
        } while (testRequestRepository.existsByRequestCode(candidate));
        return candidate;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    private TestRequestResponse toResponse(TestRequest req) {
        int sampleCount = sampleRepository.findByRequestId(req.getId()).size();

        String productCode = null;
        String productName = null;
        if (req.getProductId() != null) {
            Product p = productRepository.findById(req.getProductId()).orElse(null);
            if (p != null) {
                productCode = p.getProductCode();
                productName = p.getProductName();
            }
        }

        String batchNumber = null;
        if (req.getBatchId() != null) {
            batchNumber = batchRepository.findById(req.getBatchId())
                    .map(Batch::getBatchNumber).orElse(null);
        }

        String customerName = null;
        if (req.getCustomerId() != null) {
            customerName = customerRepository.findById(req.getCustomerId())
                    .map(Customer::getName).orElse(null);
        }

        String projectCode = null;
        if (req.getProjectId() != null) {
            projectCode = projectRepository.findById(req.getProjectId())
                    .map(Project::getCode).orElse(null);
        }

        String specCode = null;
        String specSetName = null;
        if (req.getSpecSetId() != null) {
            SpecificationSet spec = specSetRepository.findById(req.getSpecSetId()).orElse(null);
            if (spec != null) {
                specCode = spec.getSpecCode();
                specSetName = spec.getName();
            }
        }

        return TestRequestResponse.builder()
                .id(req.getId())
                .requestCode(req.getRequestCode())
                .sourceType(req.getSourceType())
                .sampleType(req.getSampleType())
                .productId(req.getProductId())
                .productCode(productCode)
                .productName(productName)
                .batchId(req.getBatchId())
                .batchNumber(batchNumber)
                .processStage(req.getProcessStage())
                .formulationTrialId(req.getFormulationTrialId())
                .customerId(req.getCustomerId())
                .customerName(customerName)
                .projectId(req.getProjectId())
                .projectCode(projectCode)
                .specSetId(req.getSpecSetId())
                .specCode(specCode)
                .specSetName(specSetName)
                .requestedBy(req.getRequestedBy())
                .requestDate(req.getRequestDate())
                .dueDate(req.getDueDate())
                .priority(req.getPriority())
                .testScope(req.getTestScope())
                .status(req.getStatus())
                .sampleCount(sampleCount)
                .notes(req.getNotes())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }
}
