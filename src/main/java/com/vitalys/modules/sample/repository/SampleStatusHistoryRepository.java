package com.vitalys.modules.sample.repository;

import com.vitalys.modules.sample.entity.SampleStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleStatusHistoryRepository extends JpaRepository<SampleStatusHistory, Long> {

    List<SampleStatusHistory> findBySampleIdOrderByChangedAtDesc(Long sampleId);
}
