package com.vitalys.modules.sample.repository;

import com.vitalys.modules.sample.entity.SampleChainOfCustody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleChainOfCustodyRepository extends JpaRepository<SampleChainOfCustody, Long> {

    List<SampleChainOfCustody> findBySampleIdOrderByTransferredAtDesc(Long sampleId);
}
