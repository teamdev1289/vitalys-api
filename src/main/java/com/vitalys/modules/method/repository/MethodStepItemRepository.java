package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.MethodStepItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodStepItemRepository extends JpaRepository<MethodStepItem, Long> {
    List<MethodStepItem> findByMethodIdOrderByOrderIndexAsc(Long methodId);
    void deleteByMethodId(Long methodId);
}
