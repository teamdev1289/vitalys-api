package com.vitalys.modules.testing.repository;

import com.vitalys.modules.testing.entity.OosInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OosInvestigationRepository extends JpaRepository<OosInvestigation, Long>, JpaSpecificationExecutor<OosInvestigation> {
    Optional<OosInvestigation> findByInvestigationCode(String investigationCode);
    boolean existsByInvestigationCode(String investigationCode);
    List<OosInvestigation> findByResultId(Long resultId);
    List<OosInvestigation> findBySampleId(Long sampleId);
}
