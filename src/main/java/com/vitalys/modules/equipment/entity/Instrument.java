package com.vitalys.modules.equipment.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Laboratory Equipment / Analytical Instrument entity.
 * Represents analytical instruments (HPLC, GC, UV-Vis, Balances, etc.)
 * subject to GxP and 21 CFR Part 11 instrument qualification/calibration.
 */
@Entity
@Table(name = "equipment_instrument")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "model")
    private String model;

    @Column(name = "asset_code", nullable = false, unique = true)
    private String assetCode;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "location")
    private String location;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "IN_SERVICE";

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
