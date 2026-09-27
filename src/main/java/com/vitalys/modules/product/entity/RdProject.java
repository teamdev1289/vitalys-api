package com.vitalys.modules.product.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * R&D project
 */
@Entity
@Table(name = "product_rd_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RdProject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "name")
    private String name;

    @Column(name = "objective")
    private String objective;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "status")
    private String status;


}
