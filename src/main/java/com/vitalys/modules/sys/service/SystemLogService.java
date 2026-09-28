package com.vitalys.modules.sys.service;

import com.vitalys.modules.sys.entity.SysLog;
import com.vitalys.modules.sys.repository.SysLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * System log service for querying security and operational event logs.
 */
@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SysLogRepository logRepository;

    @Transactional(readOnly = true)
    public Page<SysLog> search(String username, String status, String action,
                                OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        Specification<SysLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.isBlank()) {
                predicates.add(cb.equal(root.get("username"), username.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action.trim()));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "timestamp"));
        }

        return logRepository.findAll(spec, pageable);
    }
}
