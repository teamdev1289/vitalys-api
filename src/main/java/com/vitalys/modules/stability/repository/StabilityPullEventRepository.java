package com.vitalys.modules.stability.repository;

import com.vitalys.modules.stability.entity.StabilityPullEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StabilityPullEventRepository extends JpaRepository<StabilityPullEvent, Long> {

    List<StabilityPullEvent> findByStudyIdOrderByScheduledDateAsc(Long studyId);

    List<StabilityPullEvent> findByStudyIdAndStorageConditionIdOrderByScheduledDateAsc(Long studyId, Long storageConditionId);

    @Query("SELECT e FROM StabilityPullEvent e WHERE " +
           "(:studyId IS NULL OR e.studyId = :studyId) AND " +
           "(CAST(:status AS string) IS NULL OR e.status = CAST(:status AS string)) AND " +
           "(CAST(:fromDate AS date) IS NULL OR e.scheduledDate >= :fromDate) AND " +
           "(CAST(:toDate AS date) IS NULL OR e.scheduledDate <= :toDate) " +
           "ORDER BY e.scheduledDate ASC")
    Page<StabilityPullEvent> searchPullEvents(
            @Param("studyId") Long studyId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT e FROM StabilityPullEvent e WHERE e.status = 'SCHEDULED' AND e.scheduledDate <= :cutoffDate")
    List<StabilityPullEvent> findDuePullEvents(@Param("cutoffDate") LocalDate cutoffDate);
}
