package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysAuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SysAuditTrailRepository extends JpaRepository<SysAuditTrail, Long> {

    @Query("SELECT a FROM SysAuditTrail a WHERE " +
           "(:module IS NULL OR a.module = :module) " +
           "AND (:entityName IS NULL OR a.entityName = :entityName) " +
           "AND (:performedBy IS NULL OR a.performedBy = :performedBy) " +
           "AND (:fromDate IS NULL OR a.timestamp >= :fromDate) " +
           "AND (:toDate IS NULL OR a.timestamp <= :toDate) " +
           "ORDER BY a.timestamp DESC")
    Page<SysAuditTrail> searchAuditTrail(@Param("module") String module,
                                          @Param("entityName") String entityName,
                                          @Param("performedBy") String performedBy,
                                          @Param("fromDate") OffsetDateTime fromDate,
                                          @Param("toDate") OffsetDateTime toDate,
                                          Pageable pageable);

    /** Fetch all records for a given entity (used for CSV export). */
    List<SysAuditTrail> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
}
