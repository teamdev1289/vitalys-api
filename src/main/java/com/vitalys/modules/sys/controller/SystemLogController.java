package com.vitalys.modules.sys.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sys.entity.SysLog;
import com.vitalys.modules.sys.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * System log controller for viewing security and operational events.
 */
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "System Logs", description = "Security and operational event log viewer")
@SecurityRequirement(name = "bearerAuth")
public class SystemLogController {

    private final SystemLogService logService;

    @Operation(summary = "Search system event logs with filters")
    @GetMapping
    @PreAuthorize("hasAuthority('SYS:LOG:READ')")
    public ResponseEntity<ResponseDto<Page<SysLog>>> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDto.ok(logService.search(username, status, action, from, to, pageable)));
    }
}
