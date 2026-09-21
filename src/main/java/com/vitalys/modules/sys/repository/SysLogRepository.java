package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long> {

    @Query("SELECT l FROM SysLog l WHERE " +
           "(:username IS NULL OR l.username = :username) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:action IS NULL OR l.action = :action) " +
           "AND (:fromDate IS NULL OR l.timestamp >= :fromDate) " +
           "AND (:toDate IS NULL OR l.timestamp <= :toDate) " +
           "ORDER BY l.timestamp DESC")
    Page<SysLog> searchLogs(@Param("username") String username,
                             @Param("status") String status,
                             @Param("action") String action,
                             @Param("fromDate") OffsetDateTime fromDate,
                             @Param("toDate") OffsetDateTime toDate,
                             Pageable pageable);
}
