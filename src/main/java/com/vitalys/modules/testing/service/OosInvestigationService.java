package com.vitalys.modules.testing.service;

import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.testing.dto.OosInvestigationRequest;
import com.vitalys.modules.testing.dto.OosInvestigationResponse;
import com.vitalys.modules.testing.entity.OosInvestigation;
import com.vitalys.modules.testing.entity.Result;
import com.vitalys.modules.testing.entity.TestEntity;
import com.vitalys.modules.testing.repository.OosInvestigationRepository;
import com.vitalys.modules.testing.repository.ResultRepository;
import com.vitalys.modules.testing.repository.TestEntityRepository;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OosInvestigationService {

    private final OosInvestigationRepository oosRepository;
    private final ResultRepository resultRepository;
    private final TestEntityRepository testRepository;
    private final SampleRepository sampleRepository;
    private final TestRequestRepository testRequestRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;

    @Transactional(readOnly = true)
    public Page<OosInvestigationResponse> getInvestigations(String search, String phase, String status, Pageable pageable) {
        Specification<OosInvestigation> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (phase != null && !phase.isBlank()) {
                predicates.add(cb.equal(root.get("phase"), phase.trim()));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("investigationCode")), term),
                        cb.like(cb.lower(root.get("rootCauseCategory")), term),
                        cb.like(cb.lower(root.get("investigatedBy")), term)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        return oosRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OosInvestigationResponse getInvestigationById(Long id) {
        return toResponse(oosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OOS Investigation not found with ID: " + id)));
    }

    @Auditable(module = "TESTING", entity = "OosInvestigation")
    @Transactional
    public OosInvestigationResponse updateInvestigation(Long id, OosInvestigationRequest request) {
        OosInvestigation oos = oosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OOS Investigation not found with ID: " + id));

        String currentUser = getCurrentUsername();

        if (request.getPhase() != null) oos.setPhase(request.getPhase());
        if (request.getRootCauseCategory() != null) oos.setRootCauseCategory(request.getRootCauseCategory());
        if (request.getImmediateAction() != null) oos.setImmediateAction(request.getImmediateAction());
        if (request.getInvestigationFindings() != null) oos.setInvestigationFindings(request.getInvestigationFindings());
        if (request.getSupervisorComments() != null) {
            oos.setSupervisorComments(request.getSupervisorComments());
            oos.setSupervisorReviewedBy(currentUser);
        }
        if (request.getRetestApproved() != null) oos.setRetestApproved(request.getRetestApproved());
        if (request.getConclusion() != null) oos.setConclusion(request.getConclusion());
        if (request.getStatus() != null) oos.setStatus(request.getStatus());

        OosInvestigation updated = oosRepository.save(oos);
        log.info("Updated OOS Investigation {}: status={}, conclusion={}",
                updated.getInvestigationCode(), updated.getStatus(), updated.getConclusion());

        return toResponse(updated);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    private OosInvestigationResponse toResponse(OosInvestigation o) {
        Double resultValue = null;
        String resultTextValue = null;
        Double specMin = null;
        Double specMax = null;
        String specTarget = null;
        String unit = null;

        Result r = resultRepository.findById(o.getResultId()).orElse(null);
        if (r != null) {
            resultValue = r.getValue();
            resultTextValue = r.getTextValue();
            specMin = r.getSpecMin();
            specMax = r.getSpecMax();
            specTarget = r.getSpecTarget();
            unit = r.getUnit();
        }

        String testCode = testRepository.findById(o.getTestId()).map(TestEntity::getTestCode).orElse(null);

        String sampleCode = null;
        String productName = null;
        String batchNumber = null;

        Sample s = sampleRepository.findById(o.getSampleId()).orElse(null);
        if (s != null) {
            sampleCode = s.getSampleCode();
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

        return OosInvestigationResponse.builder()
                .id(o.getId())
                .investigationCode(o.getInvestigationCode())
                .resultId(o.getResultId())
                .resultValue(resultValue)
                .resultTextValue(resultTextValue)
                .specMin(specMin)
                .specMax(specMax)
                .specTarget(specTarget)
                .unit(unit)
                .testId(o.getTestId())
                .testCode(testCode)
                .sampleId(o.getSampleId())
                .sampleCode(sampleCode)
                .productName(productName)
                .batchNumber(batchNumber)
                .phase(o.getPhase())
                .rootCauseCategory(o.getRootCauseCategory())
                .immediateAction(o.getImmediateAction())
                .investigationFindings(o.getInvestigationFindings())
                .investigatedBy(o.getInvestigatedBy())
                .investigatedAt(o.getInvestigatedAt())
                .supervisorReviewedBy(o.getSupervisorReviewedBy())
                .supervisorComments(o.getSupervisorComments())
                .retestApproved(o.getRetestApproved())
                .conclusion(o.getConclusion())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }
}
