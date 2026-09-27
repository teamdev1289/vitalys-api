package com.vitalys.modules.sample.repository;

import com.vitalys.modules.sample.entity.TestRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRequestRepository extends JpaRepository<TestRequest, Long> {
}
