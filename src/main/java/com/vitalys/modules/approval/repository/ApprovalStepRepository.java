package com.vitalys.modules.approval.repository;

import com.vitalys.modules.approval.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
}
