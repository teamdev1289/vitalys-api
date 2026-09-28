package com.vitalys.modules.approval.service;

import jakarta.persistence.EntityNotFoundException;
import com.vitalys.modules.approval.dto.ApprovalStepResponse;
import com.vitalys.modules.approval.dto.ReportGenerateRequest;
import com.vitalys.modules.approval.dto.ReportResponse;
import com.vitalys.modules.approval.entity.ApprovalStep;
import com.vitalys.modules.approval.entity.Report;
import com.vitalys.modules.approval.repository.ApprovalStepRepository;
import com.vitalys.modules.approval.repository.ReportRepository;
import com.vitalys.modules.product.entity.Batch;
import com.vitalys.modules.product.entity.Product;
import com.vitalys.modules.product.repository.BatchRepository;
import com.vitalys.modules.product.repository.ProductRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.testing.entity.Result;
import com.vitalys.modules.testing.entity.TestEntity;
import com.vitalys.modules.testing.repository.ResultRepository;
import com.vitalys.modules.testing.repository.TestEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ApprovalStepRepository stepRepository;
    private final SampleRepository sampleRepository;
    private final TestRequestRepository testRequestRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final TestEntityRepository testEntityRepository;
    private final ResultRepository resultRepository;
    private final CoaPdfGeneratorService coaPdfGeneratorService;

    @Transactional
    public ReportResponse generateCoa(ReportGenerateRequest request) {
        Long sampleId = request.getSampleId();
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with id " + sampleId));

        // Check if report already exists for this sample
        Optional<Report> existingOpt = reportRepository.findFirstBySampleIdAndStatusOrderByCreatedAtDesc(sampleId, "APPROVED");
        if (existingOpt.isEmpty()) {
            existingOpt = reportRepository.findFirstBySampleIdAndStatusOrderByCreatedAtDesc(sampleId, "DRAFT");
        }

        Report report;
        if (existingOpt.isPresent()) {
            report = existingOpt.get();
        } else {
            String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String reportCode = "COA-" + datePrefix + "-" + String.format("%04d", sampleId);

            report = Report.builder()
                    .sampleId(sampleId)
                    .reportCode(reportCode)
                    .version("1.0")
                    .status("APPROVED".equalsIgnoreCase(sample.getStatus()) ? "APPROVED" : "DRAFT")
                    .conclusion(request.getConclusion() != null && !request.getConclusion().isBlank() ?
                            request.getConclusion() : "Mẫu thử ĐẠT các chỉ tiêu kiểm nghiệm theo tiêu chuẩn Dược điển Việt Nam V (DĐVN V). Đủ điều kiện xuất xưởng.")
                    .qrCodeData("https://lims.vitalys.pharma/verify/coa/" + reportCode)
                    .notes(request.getNotes())
                    .generatedBy(getCurrentUsername())
                    .build();

            report = reportRepository.save(report);
            log.info("Created new COA Report {} for Sample ID {}", reportCode, sampleId);
        }

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public byte[] getPdfBytes(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id " + reportId));

        Sample sample = sampleRepository.findById(report.getSampleId()).orElse(null);
        TestRequest testRequest = null;
        Product product = null;
        Batch batch = null;

        if (sample != null && sample.getRequestId() != null) {
            testRequest = testRequestRepository.findById(sample.getRequestId()).orElse(null);
            if (testRequest != null) {
                if (testRequest.getProductId() != null) {
                    product = productRepository.findById(testRequest.getProductId()).orElse(null);
                }
                if (testRequest.getBatchId() != null) {
                    batch = batchRepository.findById(testRequest.getBatchId()).orElse(null);
                }
            }
        }

        List<TestEntity> tests = sample != null ? testEntityRepository.findBySampleId(sample.getId()) : List.of();
        Map<Long, List<Result>> resultsByTestId = new HashMap<>();
        for (TestEntity t : tests) {
            resultsByTestId.put(t.getId(), resultRepository.findByTestId(t.getId()));
        }

        List<ApprovalStep> signatures = stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc("SAMPLE", report.getSampleId());

        return coaPdfGeneratorService.generateCoaPdf(
                report, sample, testRequest, product, batch, tests, resultsByTestId, signatures
        );
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id " + reportId));
        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsBySampleId(Long sampleId) {
        return reportRepository.findBySampleIdOrderByCreatedAtDesc(sampleId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReportResponse toResponse(Report r) {
        String sampleCode = null;
        String productName = null;
        String batchNumber = null;
        String customerName = null;

        Sample sample = sampleRepository.findById(r.getSampleId()).orElse(null);
        if (sample != null) {
            sampleCode = sample.getSampleCode();
            if (sample.getRequestId() != null) {
                TestRequest req = testRequestRepository.findById(sample.getRequestId()).orElse(null);
                if (req != null) {
                    if (req.getProductId() != null) {
                        Product p = productRepository.findById(req.getProductId()).orElse(null);
                        if (p != null) productName = p.getProductName();
                    }
                    if (req.getBatchId() != null) {
                        Batch b = batchRepository.findById(req.getBatchId()).orElse(null);
                        if (b != null) batchNumber = b.getBatchNumber();
                    }
                }
            }
        }

        List<ApprovalStep> steps = stepRepository.findByEntityTypeAndEntityIdOrderByStepNumberAsc("SAMPLE", r.getSampleId());
        List<ApprovalStepResponse> sigResponses = steps.stream()
                .filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus()))
                .map(s -> ApprovalStepResponse.builder()
                        .id(s.getId())
                        .stepNumber(s.getStepNumber())
                        .stepName(s.getStepName())
                        .requiredRole(s.getRequiredRole())
                        .status(s.getStatus())
                        .actionedBy(s.getActionedBy())
                        .actionedAt(s.getActionedAt())
                        .meaning(s.getMeaning())
                        .eSignatureHash(s.getESignatureHash())
                        .build())
                .collect(Collectors.toList());

        return ReportResponse.builder()
                .id(r.getId())
                .sampleId(r.getSampleId())
                .sampleCode(sampleCode)
                .productName(productName)
                .batchNumber(batchNumber)
                .customerName(customerName)
                .reportCode(r.getReportCode())
                .version(r.getVersion())
                .status(r.getStatus())
                .conclusion(r.getConclusion())
                .notes(r.getNotes())
                .qrCodeData(r.getQrCodeData())
                .generatedBy(r.getGeneratedBy())
                .createdAt(r.getCreatedAt())
                .signatures(sigResponses)
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }
}
