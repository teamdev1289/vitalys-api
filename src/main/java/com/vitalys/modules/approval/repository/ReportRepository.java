package com.vitalys.modules.approval.repository;

import com.vitalys.modules.approval.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    List<Report> findBySampleIdOrderByCreatedAtDesc(Long sampleId);

    Optional<Report> findByReportCode(String reportCode);

    Optional<Report> findFirstBySampleIdAndStatusOrderByCreatedAtDesc(Long sampleId, String status);
}
