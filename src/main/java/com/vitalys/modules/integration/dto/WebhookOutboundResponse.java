package com.vitalys.modules.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookOutboundResponse {
    private Long id;
    private String targetSystem;
    private String webhookUrl;
    private String eventType;
    private String payload;
    private String deliveryStatus;
    private Integer responseCode;
    private String responseBody;
    private Integer retryCount;
    private OffsetDateTime sentAt;
}
