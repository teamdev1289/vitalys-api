package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.SpecificationSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificationSetRepository extends JpaRepository<SpecificationSet, Long> {
}
