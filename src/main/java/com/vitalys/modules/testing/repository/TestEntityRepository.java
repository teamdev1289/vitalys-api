package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestEntityRepository extends JpaRepository<TestEntity, Long>, JpaSpecificationExecutor<TestEntity> {
    List<TestEntity> findBySampleId(Long sampleId);
    List<TestEntity> findByAssignedTo(String assignedTo);
    List<TestEntity> findByRunId(Long runId);
    boolean existsByTestCode(String testCode);
    Optional<TestEntity> findByTestCode(String testCode);
}
