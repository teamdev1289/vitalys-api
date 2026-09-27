package com.vitalys.modules.equipment.repository;

import com.vitalys.modules.equipment.entity.CalibrationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalibrationRecordRepository extends JpaRepository<CalibrationRecord, Long> {
}
