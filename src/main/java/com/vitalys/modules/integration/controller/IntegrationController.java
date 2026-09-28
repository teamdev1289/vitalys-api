package com.vitalys.modules.integration.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.integration.dto.InboundOrderCreateRequest;
import com.vitalys.modules.integration.dto.InboundOrderResponse;
import com.vitalys.modules.integration.dto.WebhookOutboundResponse;
import com.vitalys.modules.integration.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
@Tag(name = "Integration & Connectors", description = "ERP/SAP Inbound Orders & Webhook Event Dispatching")
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('INTEGRATION:ERP:READ') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Get list of inbound ERP/SAP orders")
    public ResponseEntity<ResponseDto<Page<InboundOrderResponse>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sourceSystem,
            Pageable pageable) {
        Page<InboundOrderResponse> orders = integrationService.getOrders(status, sourceSystem, pageable);
        return ResponseEntity.ok(ResponseDto.ok(orders));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAuthority('INTEGRATION:ERP:READ') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Get inbound ERP order by ID")
    public ResponseEntity<ResponseDto<InboundOrderResponse>> getOrderById(@PathVariable Long id) {
        InboundOrderResponse order = integrationService.getOrderById(id);
        return ResponseEntity.ok(ResponseDto.ok(order));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('INTEGRATION:ERP:SYNC') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Receive new inbound order from external ERP/SAP/MES")
    public ResponseEntity<ResponseDto<InboundOrderResponse>> createOrder(
            @Valid @RequestBody InboundOrderCreateRequest request) {
        InboundOrderResponse created = integrationService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.created(created));
    }

    @PostMapping("/orders/{id}/process")
    @PreAuthorize("hasAuthority('INTEGRATION:ERP:SYNC') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Process ERP order into active LIMS Sample and TestRequest")
    public ResponseEntity<ResponseDto<InboundOrderResponse>> processOrder(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "SYSTEM";
        InboundOrderResponse processed = integrationService.processOrder(id, username);
        return ResponseEntity.ok(ResponseDto.ok(processed));
    }

    @GetMapping("/webhooks")
    @PreAuthorize("hasAuthority('INTEGRATION:ERP:READ') or hasRole('IT_ADMIN') or hasRole('LAB_ADMIN')")
    @Operation(summary = "Get list of outbound webhook logs")
    public ResponseEntity<ResponseDto<Page<WebhookOutboundResponse>>> getWebhookLogs(Pageable pageable) {
        Page<WebhookOutboundResponse> logs = integrationService.getWebhookLogs(pageable);
        return ResponseEntity.ok(ResponseDto.ok(logs));
    }
}
