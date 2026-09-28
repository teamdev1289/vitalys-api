package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAuditTrailRepository extends JpaRepository<SysAuditTrail, Long>, JpaSpecificationExecutor<SysAuditTrail> {

    /** Fetch all records for a given entity (used for CSV export). */
    List<SysAuditTrail> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
}
