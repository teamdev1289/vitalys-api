package com.vitalys.modules.stability.repository;

import com.vitalys.modules.stability.entity.StabilityTimePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StabilityTimePointRepository extends JpaRepository<StabilityTimePoint, Long> {
    List<StabilityTimePoint> findByStudyIdOrderByMonthOffsetAsc(Long studyId);
}
