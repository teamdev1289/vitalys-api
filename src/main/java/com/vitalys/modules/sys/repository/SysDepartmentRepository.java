package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysDepartmentRepository extends JpaRepository<SysDepartment, Long> {

    Optional<SysDepartment> findByCode(String code);

    boolean existsByCode(String code);
}
