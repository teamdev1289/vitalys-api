package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long>, JpaSpecificationExecutor<Result> {
    List<Result> findByTestId(Long testId);
    Optional<Result> findFirstByTestIdOrderByIdDesc(Long testId);
    List<Result> findByIsOosTrue();
}
