package com.vitalys.modules.retention.repository;

import com.vitalys.modules.retention.entity.LegalHoldRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalHoldRecordRepository extends JpaRepository<LegalHoldRecord, Long> {

    Optional<LegalHoldRecord> findByHoldCode(String holdCode);

    List<LegalHoldRecord> findByTargetModuleAndTargetEntityIdAndStatus(String targetModule, Long targetEntityId, String status);

    boolean existsByTargetModuleAndTargetEntityIdAndStatus(String targetModule, Long targetEntityId, String status);

    long countByStatus(String status);

    Page<LegalHoldRecord> findAllByOrderByPlacedAtDesc(Pageable pageable);
}
