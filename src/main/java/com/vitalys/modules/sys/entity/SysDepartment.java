package com.vitalys.modules.sys.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a laboratory department (e.g., Hematology, Biochemistry).
 * Users are assigned to departments via sys_user_role_dept.
 */
@Entity
@Table(name = "sys_department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
