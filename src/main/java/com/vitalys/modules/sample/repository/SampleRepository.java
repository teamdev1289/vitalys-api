package com.vitalys.modules.sample.repository;

import com.vitalys.modules.sample.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long>, JpaSpecificationExecutor<Sample> {

    Optional<Sample> findBySampleCode(String sampleCode);

    Optional<Sample> findByBarcode(String barcode);

    boolean existsBySampleCode(String sampleCode);

    List<Sample> findByRequestId(Long requestId);

    List<Sample> findByDepartmentId(Long departmentId);
}
