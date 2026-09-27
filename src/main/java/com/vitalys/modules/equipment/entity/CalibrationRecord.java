package com.vitalys.modules.equipment.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Calibration history
 */
@Entity
@Table(name = "equipment_calibration_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "performed_at")
    private OffsetDateTime performedAt;

    @Column(name = "next_due")
    private String nextDue;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Column(name = "notes")
    private String notes;


}
