package com.vitalys.modules.sys.service;

import com.vitalys.modules.sys.entity.SysLog;
import com.vitalys.modules.sys.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

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
        return logRepository.searchLogs(username, status, action, fromDate, toDate, pageable);
    }
}
