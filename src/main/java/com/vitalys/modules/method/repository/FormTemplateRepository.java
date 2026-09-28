package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    List<FormTemplate> findByMethodId(Long methodId);
    Optional<FormTemplate> findBySchemaName(String schemaName);
    Optional<FormTemplate> findByMethodIdAndStatus(Long methodId, String status);
}
