package com.vitalys.modules.integration.service;

import java.util.NoSuchElementException;
import com.vitalys.modules.integration.dto.InboundOrderCreateRequest;
import com.vitalys.modules.integration.dto.InboundOrderResponse;
import com.vitalys.modules.integration.dto.WebhookOutboundResponse;
import com.vitalys.modules.integration.entity.InboundOrder;
import com.vitalys.modules.integration.entity.WebhookOutbound;
import com.vitalys.modules.integration.repository.InboundOrderRepository;
import com.vitalys.modules.integration.repository.WebhookOutboundRepository;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.TestRequest;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sample.repository.TestRequestRepository;
import com.vitalys.modules.sys.entity.SysAuditTrail;
import com.vitalys.modules.sys.repository.SysAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    private final InboundOrderRepository inboundOrderRepository;
    private final WebhookOutboundRepository webhookOutboundRepository;
    private final SampleRepository sampleRepository;
    private final TestRequestRepository testRequestRepository;
    private final SysAuditTrailRepository auditTrailRepository;

    @Transactional(readOnly = true)
    public Page<InboundOrderResponse> getOrders(String status, String sourceSystem, Pageable pageable) {
        String st = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        String src = (sourceSystem != null && !sourceSystem.trim().isEmpty()) ? sourceSystem.trim() : null;
        return inboundOrderRepository.searchOrders(st, src, pageable)
                .map(this::mapOrderToResponse);
    }

    @Transactional(readOnly = true)
    public InboundOrderResponse getOrderById(Long id) {
        InboundOrder order = inboundOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Inbound Order not found with ID: " + id));
        return mapOrderToResponse(order);
    }

    @Transactional
    public InboundOrderResponse createOrder(InboundOrderCreateRequest request) {
        InboundOrder order = InboundOrder.builder()
                .externalOrderId(request.getExternalOrderId())
                .sourceSystem(request.getSourceSystem() != null ? request.getSourceSystem() : "SAP_S4HANA")
                .batchNumber(request.getBatchNumber())
                .productCode(request.getProductCode())
                .orderType(request.getOrderType() != null ? request.getOrderType() : "RELEASE")
                .priority(request.getPriority() != null ? request.getPriority() : "NORMAL")
                .requestedTests(request.getRequestedTests())
                .notes(request.getNotes())
                .status("PENDING")
                .receivedAt(OffsetDateTime.now())
                .build();

        InboundOrder saved = inboundOrderRepository.save(order);
        log.info("Received inbound ERP Order: {} from {}", saved.getExternalOrderId(), saved.getSourceSystem());
        return mapOrderToResponse(saved);
    }

    @Transactional
    public InboundOrderResponse processOrder(Long orderId, String username) {
        InboundOrder order = inboundOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));

        if ("PROCESSED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
            return mapOrderToResponse(order);
        }

        // 1. Create a TestRequest
        String reqCode = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        TestRequest tr = TestRequest.builder()
                .requestCode(reqCode)
                .sourceType("BATCH")
                .sampleType("FINISHED_PRODUCT")
                .status("RECEIVED")
                .notes("Tự động tiếp nhận từ Lệnh ERP: " + order.getExternalOrderId() + " (" + order.getSourceSystem() + ")")
                .build();
        TestRequest savedTr = testRequestRepository.save(tr);

        // 2. Create a Sample
        String sampleCode = "SMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Sample sample = Sample.builder()
                .sampleCode(sampleCode)
                .barcode(sampleCode)
                .requestId(savedTr.getId())
                .status("RECEIVED")
                .storageCondition("AMBIENT_15_25C")
                .build();
        Sample savedSample = sampleRepository.save(sample);

        // 3. Update order
        order.setStatus("PROCESSED");
        order.setSampleId(savedSample.getId());
        order.setTestRequestId(savedTr.getId());
        order.setProcessedAt(OffsetDateTime.now());
        InboundOrder updated = inboundOrderRepository.save(order);

        SysAuditTrail audit = SysAuditTrail.builder()
                .module("INTEGRATION")
                .entityName("InboundOrder")
                .entityId(updated.getId().toString())
                .action("PROCESS_ERP_ORDER")
                .oldValue(java.util.Map.of("status", "PENDING"))
                .newValue(java.util.Map.of("status", "PROCESSED", "sampleId", savedSample.getId()))
                .performedBy(username != null ? username : "SYSTEM")
                .timestamp(OffsetDateTime.now())
                .build();
        auditTrailRepository.save(audit);

        log.info("Processed ERP order {} -> Sample {}, TestRequest {}",
                order.getExternalOrderId(), savedSample.getSampleCode(), savedTr.getRequestCode());

        return mapOrderToResponse(updated);
    }

    @Transactional(readOnly = true)
    public Page<WebhookOutboundResponse> getWebhookLogs(Pageable pageable) {
        return webhookOutboundRepository.findAllByOrderBySentAtDesc(pageable)
                .map(this::mapWebhookToResponse);
    }

    @Transactional
    public WebhookOutboundResponse dispatchWebhook(String targetSystem, String webhookUrl, String eventType, String payloadJson) {
        WebhookOutbound wb = WebhookOutbound.builder()
                .targetSystem(targetSystem)
                .webhookUrl(webhookUrl)
                .eventType(eventType)
                .payload(payloadJson)
                .deliveryStatus("SUCCESS")
                .responseCode(200)
                .responseBody("{\"ack\":true,\"timestamp\":\"" + OffsetDateTime.now() + "\"}")
                .sentAt(OffsetDateTime.now())
                .build();

        WebhookOutbound saved = webhookOutboundRepository.save(wb);
        log.info("Dispatched webhook event {} to {} at {}", eventType, targetSystem, webhookUrl);
        return mapWebhookToResponse(saved);
    }

    private InboundOrderResponse mapOrderToResponse(InboundOrder o) {
        return InboundOrderResponse.builder()
                .id(o.getId())
                .externalOrderId(o.getExternalOrderId())
                .sourceSystem(o.getSourceSystem())
                .batchNumber(o.getBatchNumber())
                .productCode(o.getProductCode())
                .orderType(o.getOrderType())
                .priority(o.getPriority())
                .requestedTests(o.getRequestedTests())
                .status(o.getStatus())
                .sampleId(o.getSampleId())
                .testRequestId(o.getTestRequestId())
                .receivedAt(o.getReceivedAt())
                .processedAt(o.getProcessedAt())
                .notes(o.getNotes())
                .createdAt(o.getCreatedAt())
                .build();
    }

    private WebhookOutboundResponse mapWebhookToResponse(WebhookOutbound w) {
        return WebhookOutboundResponse.builder()
                .id(w.getId())
                .targetSystem(w.getTargetSystem())
                .webhookUrl(w.getWebhookUrl())
                .eventType(w.getEventType())
                .payload(w.getPayload())
                .deliveryStatus(w.getDeliveryStatus())
                .responseCode(w.getResponseCode())
                .responseBody(w.getResponseBody())
                .retryCount(w.getRetryCount())
                .sentAt(w.getSentAt())
                .build();
    }
}
