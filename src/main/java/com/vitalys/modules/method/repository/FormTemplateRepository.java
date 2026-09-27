package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
}
