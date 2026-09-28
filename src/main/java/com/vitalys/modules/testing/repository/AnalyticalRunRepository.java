package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.AnalyticalRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyticalRunRepository extends JpaRepository<AnalyticalRun, Long>, JpaSpecificationExecutor<AnalyticalRun> {
    boolean existsByRunCode(String runCode);
    Optional<AnalyticalRun> findByRunCode(String runCode);
}
