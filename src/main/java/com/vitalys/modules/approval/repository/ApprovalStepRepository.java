package com.vitalys.modules.approval.repository;

import com.vitalys.modules.approval.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByEntityTypeAndEntityIdOrderByStepNumberAsc(String entityType, Long entityId);

    Optional<ApprovalStep> findFirstByEntityTypeAndEntityIdAndStatusOrderByStepNumberAsc(
            String entityType, Long entityId, String status);

    boolean existsByEntityTypeAndEntityIdAndStatus(String entityType, Long entityId, String status);
}
