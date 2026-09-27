package com.vitalys.modules.method.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Method validation protocol
 */
@Entity
@Table(name = "method_method_validation_protocol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MethodValidationProtocol extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "protocol_code")
    private String protocolCode;

    @Column(name = "validation_type")
    private String validationType;

    @Column(name = "trigger_reason")
    private String triggerReason;

    @Column(name = "status")
    private String status;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;


}
