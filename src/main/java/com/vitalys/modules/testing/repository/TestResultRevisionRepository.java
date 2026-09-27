package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.TestResultRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestResultRevisionRepository extends JpaRepository<TestResultRevision, Long> {
}
