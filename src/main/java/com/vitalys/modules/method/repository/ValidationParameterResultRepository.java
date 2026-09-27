package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.ValidationParameterResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationParameterResultRepository extends JpaRepository<ValidationParameterResult, Long> {
}
