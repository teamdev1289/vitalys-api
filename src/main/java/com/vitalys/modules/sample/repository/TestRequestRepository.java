package com.vitalys.modules.sample.repository;

import com.vitalys.modules.sample.entity.TestRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestRequestRepository extends JpaRepository<TestRequest, Long>, JpaSpecificationExecutor<TestRequest> {

    Optional<TestRequest> findByRequestCode(String requestCode);

    boolean existsByRequestCode(String requestCode);
}
