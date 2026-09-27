package com.vitalys.modules.sample.repository;

import com.vitalys.modules.sample.entity.SampleAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleAttachmentRepository extends JpaRepository<SampleAttachment, Long> {
}
