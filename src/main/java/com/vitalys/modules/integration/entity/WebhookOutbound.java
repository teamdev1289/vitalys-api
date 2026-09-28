package com.vitalys.modules.integration.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "integration_webhook_outbound")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookOutbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_system", nullable = false, length = 50)
    private String targetSystem; // SAP_S4HANA, WERUM_MES

    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // SAMPLE_RELEASED, TEST_OOS, COA_SIGNED

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "delivery_status", nullable = false, length = 30)
    @Builder.Default
    private String deliveryStatus = "SUCCESS"; // SUCCESS, FAILED, RETRYING

    @Column(name = "response_code")
    @Builder.Default
    private Integer responseCode = 200;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private OffsetDateTime sentAt = OffsetDateTime.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
