package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    List<FormField> findByTemplateIdOrderByOrderIndexAsc(Long templateId);
    void deleteByTemplateId(Long templateId);
}
