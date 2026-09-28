package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.SpecificationSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecificationSetRepository extends JpaRepository<SpecificationSet, Long> {
    Optional<SpecificationSet> findBySpecCode(String specCode);
    List<SpecificationSet> findByProductId(Long productId);
    List<SpecificationSet> findByProductIdAndStatus(Long productId, String status);
    List<SpecificationSet> findByStatus(String status);
}
