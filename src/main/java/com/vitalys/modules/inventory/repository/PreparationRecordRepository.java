package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.PreparationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PreparationRecordRepository extends JpaRepository<PreparationRecord, Long> {

    @Query("SELECT p FROM PreparationRecord p WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(p.solutionName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(p.sopReference) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<PreparationRecord> searchRecords(@Param("search") String search, Pageable pageable);
}
