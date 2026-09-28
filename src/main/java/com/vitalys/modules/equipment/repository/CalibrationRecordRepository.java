package com.vitalys.modules.equipment.repository;

import com.vitalys.modules.equipment.entity.CalibrationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalibrationRecordRepository extends JpaRepository<CalibrationRecord, Long> {

    List<CalibrationRecord> findByInstrumentIdOrderByPerformedAtDesc(Long instrumentId);

    Page<CalibrationRecord> findByInstrumentIdOrderByPerformedAtDesc(Long instrumentId, Pageable pageable);
}
