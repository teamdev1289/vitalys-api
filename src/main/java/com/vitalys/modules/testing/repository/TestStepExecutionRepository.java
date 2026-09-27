package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.TestStepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestStepExecutionRepository extends JpaRepository<TestStepExecution, Long> {
}
