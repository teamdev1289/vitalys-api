package com.vitalys.modules.testing.service;

import com.vitalys.modules.equipment.entity.Instrument;
import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.testing.dto.AnalyticalRunRequest;
import com.vitalys.modules.testing.dto.AnalyticalRunResponse;
import com.vitalys.modules.testing.entity.AnalyticalRun;
import com.vitalys.modules.testing.repository.AnalyticalRunRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticalRunService {

    private final AnalyticalRunRepository runRepository;
    private final InstrumentRepository instrumentRepository;

    @Transactional(readOnly = true)
    public Page<AnalyticalRunResponse> getRuns(String search, String status, Pageable pageable) {
        Specification<AnalyticalRun> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("runCode")), term),
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("analyst")), term)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        return runRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AnalyticalRunResponse getRunById(Long id) {
        return toResponse(runRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Analytical run not found with ID: " + id)));
    }

    @Auditable(module = "TESTING", entity = "AnalyticalRun")
    @Transactional
    public AnalyticalRunResponse createRun(AnalyticalRunRequest request) {
        String code = generateRunCode();
        String analyst = request.getAnalyst() != null ? request.getAnalyst() : getCurrentUsername();

        AnalyticalRun run = AnalyticalRun.builder()
                .runCode(code)
                .name(request.getName().trim())
                .instrumentId(request.getInstrumentId())
                .runDate(request.getRunDate() != null ? request.getRunDate() : OffsetDateTime.now())
                .analyst(analyst)
                .status(request.getStatus() != null ? request.getStatus() : "COMPLETED")
                .batchSize(request.getBatchSize() != null ? request.getBatchSize() : 1)
                .notes(request.getNotes())
                .build();

        AnalyticalRun saved = runRepository.save(run);
        log.info("Created analytical run: {} ({}) on instrument ID {}", saved.getRunCode(), saved.getName(), saved.getInstrumentId());
        return toResponse(saved);
    }

    private synchronized String generateRunCode() {
        String prefix = "RUN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int seq = 1;
        String candidate;
        do {
            candidate = String.format("%s%03d", prefix, seq++);
        } while (runRepository.existsByRunCode(candidate));
        return candidate;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }

    private AnalyticalRunResponse toResponse(AnalyticalRun r) {
        String instrumentName = null;
        String instrumentAssetCode = null;

        if (r.getInstrumentId() != null) {
            Instrument inst = instrumentRepository.findById(r.getInstrumentId()).orElse(null);
            if (inst != null) {
                instrumentName = inst.getName();
                instrumentAssetCode = inst.getAssetCode();
            }
        }

        return AnalyticalRunResponse.builder()
                .id(r.getId())
                .runCode(r.getRunCode())
                .name(r.getName())
                .instrumentId(r.getInstrumentId())
                .instrumentName(instrumentName)
                .instrumentAssetCode(instrumentAssetCode)
                .runDate(r.getRunDate())
                .analyst(r.getAnalyst())
                .status(r.getStatus())
                .batchSize(r.getBatchSize())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
