package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.SpecificationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecificationItemRepository extends JpaRepository<SpecificationItem, Long> {
    List<SpecificationItem> findBySpecSetId(Long specSetId);
    List<SpecificationItem> findByMethodId(Long methodId);
    void deleteBySpecSetId(Long specSetId);
}
