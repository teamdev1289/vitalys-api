package com.vitalys.modules.sdms.repository;

import com.vitalys.modules.sdms.entity.DataFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataFileRepository extends JpaRepository<DataFile, Long>, JpaSpecificationExecutor<DataFile> {

    Optional<DataFile> findByFileCode(String fileCode);

    List<DataFile> findBySampleIdOrderByCreatedAtDesc(Long sampleId);

    List<DataFile> findByTestIdOrderByCreatedAtDesc(Long testId);

    List<DataFile> findByInstrumentIdOrderByCreatedAtDesc(Long instrumentId);

    List<DataFile> findByRunIdOrderByCreatedAtDesc(Long runId);

    boolean existsByChecksumSha256(String checksumSha256);
}
