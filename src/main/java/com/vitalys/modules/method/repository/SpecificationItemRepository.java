package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.SpecificationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificationItemRepository extends JpaRepository<SpecificationItem, Long> {
}
