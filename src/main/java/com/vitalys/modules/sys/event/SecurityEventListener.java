package com.vitalys.modules.sys.event;

import com.vitalys.common.Constants;
import com.vitalys.modules.sys.entity.SysLog;
import com.vitalys.modules.sys.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Async listener for security events.
 * Persists log records and pushes real-time updates to WebSocket subscribers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private final SysLogRepository       logRepository;
    private final SimpMessagingTemplate   messagingTemplate;

    /**
     * Handles login success and failure events asynchronously.
     * Async ensures log writing does not block the HTTP response.
     */
    @Async
    @EventListener
    public void handleLoginEvent(UserLoggedInEvent event) {
        SysLog sysLog = SysLog.builder()
                .username(event.getUsername())
                .action(event.isSuccess()
                        ? Constants.LOG_ACTION_LOGIN
                        : Constants.LOG_ACTION_LOGIN_FAILED)
                .module("SYS")
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .status(event.isSuccess()
                        ? Constants.LOG_STATUS_SUCCESS
                        : Constants.LOG_STATUS_FAILED)
                .errorMessage(event.getErrorMessage())
                .timestamp(OffsetDateTime.now())
                .build();

        logRepository.save(sysLog);

        // Push real-time update to WebSocket subscribers
        try {
            messagingTemplate.convertAndSend(Constants.WS_TOPIC_LOGS, sysLog);
        } catch (Exception e) {
            log.debug("WebSocket push failed for log entry: {}", e.getMessage());
        }

        log.debug("Security event logged: {} for user {}", sysLog.getAction(), event.getUsername());
    }
}
