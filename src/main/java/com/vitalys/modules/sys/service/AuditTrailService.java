package com.vitalys.modules.sys.service;

import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;

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
        return auditTrailRepository.searchAuditTrail(module, entityName, performedBy, fromDate, toDate, pageable);
    }

    /**
     * Export audit trail records to CSV format.
     * Returned as a String for streaming via the controller.
     */
    @Transactional(readOnly = true)
    public String exportToCsv(String module, String entityName, String performedBy,
                               OffsetDateTime fromDate, OffsetDateTime toDate) throws IOException {
        // Fetch all (no pagination) for export — use with caution on large datasets
        var records = auditTrailRepository.searchAuditTrail(
                module, entityName, performedBy, fromDate, toDate,
                Pageable.unpaged()).getContent();

        StringWriter writer  = new StringWriter();
        CSVFormat    format  = CSVFormat.DEFAULT.builder()
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
}
