package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long>, JpaSpecificationExecutor<SysLog> {
}
