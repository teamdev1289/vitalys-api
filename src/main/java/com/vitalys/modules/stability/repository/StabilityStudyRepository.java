package com.vitalys.modules.stability.repository;

import com.vitalys.modules.stability.entity.StabilityStudy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StabilityStudyRepository extends JpaRepository<StabilityStudy, Long> {

    Optional<StabilityStudy> findByStudyCode(String studyCode);

    @Query("SELECT s FROM StabilityStudy s WHERE " +
           "(CAST(:status AS string) IS NULL OR s.status = CAST(:status AS string)) AND " +
           "(:productId IS NULL OR s.productId = :productId) AND " +
           "(CAST(:search AS string) IS NULL OR LOWER(s.studyCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(s.studyTitle) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<StabilityStudy> searchStudies(
            @Param("status") String status,
            @Param("productId") Long productId,
            @Param("search") String search,
            Pageable pageable);
}
