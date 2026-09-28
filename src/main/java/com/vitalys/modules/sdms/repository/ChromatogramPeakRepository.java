package com.vitalys.modules.sdms.repository;

import com.vitalys.modules.sdms.entity.ChromatogramPeak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChromatogramPeakRepository extends JpaRepository<ChromatogramPeak, Long> {

    List<ChromatogramPeak> findByDataFileIdOrderByPeakNumberAsc(Long dataFileId);

    void deleteByDataFileId(Long dataFileId);
}
