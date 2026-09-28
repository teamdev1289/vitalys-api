package com.vitalys.modules.stability.repository;

import com.vitalys.modules.stability.entity.StabilityStorageCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StabilityStorageConditionRepository extends JpaRepository<StabilityStorageCondition, Long> {
    List<StabilityStorageCondition> findByStudyId(Long studyId);
}
