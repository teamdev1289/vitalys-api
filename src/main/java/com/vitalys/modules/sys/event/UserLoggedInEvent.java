package com.vitalys.modules.sys.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Application event published on successful or failed user login.
 * Consumed by SecurityEventListener to write to sys_log.
 */
@Getter
public class UserLoggedInEvent extends ApplicationEvent {

    private final String  username;
    private final String  ipAddress;
    private final String  userAgent;
    private final boolean success;
    private final String  errorMessage;

    public UserLoggedInEvent(Object source, String username, String ipAddress,
                             String userAgent, boolean success, String errorMessage) {
        super(source);
        this.username     = username;
        this.ipAddress    = ipAddress;
        this.userAgent    = userAgent;
        this.success      = success;
        this.errorMessage = errorMessage;
    }
}
