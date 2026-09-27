package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.FormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
}
