package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.AnalyticalRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticalRunRepository extends JpaRepository<AnalyticalRun, Long> {
}
