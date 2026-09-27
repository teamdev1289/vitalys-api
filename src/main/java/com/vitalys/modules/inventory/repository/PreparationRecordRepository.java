package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.PreparationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreparationRecordRepository extends JpaRepository<PreparationRecord, Long> {
}
