package com.vitalys.modules.sys.service;

import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Audit trail service: search with multi-dimension filters, CSV export.
 * Write operations are intentionally excluded — audit records are immutable.
 */
@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final SysAuditTrailRepository auditTrailRepository;

    @Transactional(readOnly = true)
    public Page<SysAuditTrail> search(String module, String entityName, String performedBy,
                                       OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        Specification<SysAuditTrail> spec = buildSpecification(module, entityName, performedBy, fromDate, toDate);

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "timestamp"));
        }

        return auditTrailRepository.findAll(spec, pageable);
    }

    /**
     * Export audit trail records to CSV format.
     * Returned as a String for streaming via the controller.
     */
    @Transactional(readOnly = true)
    public String exportToCsv(String module, String entityName, String performedBy,
                               OffsetDateTime fromDate, OffsetDateTime toDate) throws IOException {
        Specification<SysAuditTrail> spec = buildSpecification(module, entityName, performedBy, fromDate, toDate);
        List<SysAuditTrail> records = auditTrailRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "timestamp"));

        StringWriter writer  = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Module", "Entity", "EntityId", "Action",
                           "PerformedBy", "IpAddress", "Timestamp")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (SysAuditTrail r : records) {
                printer.printRecord(
                        r.getId(), r.getModule(), r.getEntityName(), r.getEntityId(),
                        r.getAction(), r.getPerformedBy(), r.getIpAddress(), r.getTimestamp());
            }
        }
        return writer.toString();
    }

    private Specification<SysAuditTrail> buildSpecification(String module, String entityName, String performedBy,
                                                            OffsetDateTime fromDate, OffsetDateTime toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (module != null && !module.isBlank()) {
                predicates.add(cb.equal(root.get("module"), module.trim()));
            }
            if (entityName != null && !entityName.isBlank()) {
                predicates.add(cb.equal(root.get("entityName"), entityName.trim()));
            }
            if (performedBy != null && !performedBy.isBlank()) {
                predicates.add(cb.equal(root.get("performedBy"), performedBy.trim()));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
