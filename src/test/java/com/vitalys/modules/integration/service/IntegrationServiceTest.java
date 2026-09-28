package com.vitalys.modules.integration.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceTest {

    @Mock
    private InboundOrderRepository inboundOrderRepository;

    @Mock
    private WebhookOutboundRepository webhookOutboundRepository;

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private TestRequestRepository testRequestRepository;

    @Mock
    private SysAuditTrailRepository auditTrailRepository;

    @InjectMocks
    private IntegrationService integrationService;

    private InboundOrder order;

    @BeforeEach
    void setUp() {
        order = InboundOrder.builder()
                .id(1L)
                .externalOrderId("SAP-QM-2026-TEST")
                .sourceSystem("SAP_S4HANA")
                .batchNumber("BATCH-TEST")
                .productCode("PRD-TEST")
                .orderType("RELEASE")
                .priority("NORMAL")
                .status("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should create inbound ERP order successfully")
    void shouldCreateInboundOrder() {
        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(inv -> {
            InboundOrder o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        InboundOrderResponse resp = integrationService.createOrder(InboundOrderCreateRequest.builder()
                .externalOrderId("SAP-ORD-999")
                .sourceSystem("SAP_S4HANA")
                .batchNumber("BATCH-999")
                .productCode("PRD-999")
                .build());

        assertThat(resp).isNotNull();
        assertThat(resp.getExternalOrderId()).isEqualTo("SAP-ORD-999");
        assertThat(resp.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should process ERP order into active Sample and TestRequest")
    void shouldProcessOrderIntoSampleAndTestRequest() {
        when(inboundOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(testRequestRepository.save(any(TestRequest.class))).thenAnswer(inv -> {
            TestRequest tr = inv.getArgument(0);
            tr.setId(100L);
            return tr;
        });

        when(sampleRepository.save(any(Sample.class))).thenAnswer(inv -> {
            Sample s = inv.getArgument(0);
            s.setId(200L);
            return s;
        });

        when(inboundOrderRepository.save(any(InboundOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        InboundOrderResponse resp = integrationService.processOrder(1L, "supervisor");

        assertThat(resp).isNotNull();
        assertThat(resp.getStatus()).isEqualTo("PROCESSED");
        assertThat(resp.getSampleId()).isEqualTo(200L);
        assertThat(resp.getTestRequestId()).isEqualTo(100L);
        verify(auditTrailRepository).save(any(SysAuditTrail.class));
    }

    @Test
    @DisplayName("Should dispatch outbound webhook event successfully")
    void shouldDispatchWebhookEvent() {
        when(webhookOutboundRepository.save(any(WebhookOutbound.class))).thenAnswer(inv -> {
            WebhookOutbound wb = inv.getArgument(0);
            wb.setId(50L);
            return wb;
        });

        WebhookOutboundResponse resp = integrationService.dispatchWebhook(
                "SAP_ERP", "https://erp.vitalys.com/hook", "SAMPLE_RELEASED", "{\"status\":\"PASS\"}");

        assertThat(resp).isNotNull();
        assertThat(resp.getTargetSystem()).isEqualTo("SAP_ERP");
        assertThat(resp.getDeliveryStatus()).isEqualTo("SUCCESS");
    }
}
