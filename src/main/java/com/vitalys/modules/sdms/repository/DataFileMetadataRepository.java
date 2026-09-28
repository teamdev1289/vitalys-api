package com.vitalys.modules.sdms.repository;

import com.vitalys.modules.sdms.entity.DataFileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataFileMetadataRepository extends JpaRepository<DataFileMetadata, Long> {

    List<DataFileMetadata> findByDataFileIdOrderByIdAsc(Long dataFileId);

    void deleteByDataFileId(Long dataFileId);
}
