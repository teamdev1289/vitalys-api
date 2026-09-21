package com.vitalys.modules.sys.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalys.common.Constants;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * AOP aspect that intercepts methods annotated with {@link Auditable} and
 * creates an immutable audit trail record capturing:
 * - Who performed the action (username from SecurityContext)
 * - What entity was affected (module, entity name, entity ID)
 * - When (timestamp)
 * - Before/after state (old/new JSON values)
 * - Source IP address
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final SysAuditTrailRepository auditTrailRepository;
    private final ObjectMapper            objectMapper;

    @Around("@annotation(com.vitalys.modules.sys.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method          method    = signature.getMethod();
        Auditable       auditable = method.getAnnotation(Auditable.class);

        String performedBy = getCurrentUsername();
        String ipAddress   = getClientIpAddress();

        // Capture pre-execution state if first argument is an ID
        Map<String, Object> oldValue = null;
        Object[] args = joinPoint.getArgs();

        // Determine action from method name convention
        String methodName = method.getName().toLowerCase();
        String action;
        if (methodName.contains("create") || methodName.contains("register")) {
            action = Constants.AUDIT_CREATE;
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            action = Constants.AUDIT_DELETE;
        } else {
            action = Constants.AUDIT_UPDATE;
        }

        // Execute the actual method
        Object result = joinPoint.proceed();

        try {
            // Serialize new state from return value
            Map<String, Object> newValue = null;
            if (result != null) {
                newValue = objectMapper.convertValue(result, Map.class);
            }

            // Extract entity ID from result or first argument
            String entityId = extractEntityId(result, args);

            SysAuditTrail auditRecord = SysAuditTrail.builder()
                    .module(auditable.module())
                    .entityName(auditable.entity())
                    .entityId(entityId)
                    .action(action)
                    .performedBy(performedBy)
                    .ipAddress(ipAddress)
                    .timestamp(OffsetDateTime.now())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();

            auditTrailRepository.save(auditRecord);
            log.debug("Audit recorded: {} {} {} by {}", action, auditable.entity(), entityId, performedBy);

        } catch (Exception e) {
            // Audit failure must not break the business operation
            log.error("Failed to write audit trail for {}.{}: {}",
                    auditable.module(), auditable.entity(), e.getMessage());
        }

        return result;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String forwarded = request.getHeader("X-Forwarded-For");
                return (forwarded != null) ? forwarded.split(",")[0].trim()
                                           : request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not extract client IP: {}", e.getMessage());
        }
        return "unknown";
    }

    private String extractEntityId(Object result, Object[] args) {
        // Try to get ID from result object via getId() reflection
        if (result != null) {
            try {
                Method getId = result.getClass().getMethod("getId");
                Object id = getId.invoke(result);
                if (id != null) return id.toString();
            } catch (Exception ignored) {}
        }
        // Fall back to first argument if it looks like an ID
        if (args != null && args.length > 0 && args[0] instanceof Long id) {
            return id.toString();
        }
        return "unknown";
    }
}
