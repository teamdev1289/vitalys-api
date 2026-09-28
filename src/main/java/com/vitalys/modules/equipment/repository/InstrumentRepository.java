package com.vitalys.modules.equipment.repository;

import com.vitalys.modules.equipment.entity.Instrument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByAssetCode(String assetCode);

    boolean existsByAssetCode(String assetCode);

    boolean existsByAssetCodeAndIdNot(String assetCode, Long id);

    List<Instrument> findByDepartmentId(Long departmentId);

    @Query("SELECT i FROM Instrument i WHERE " +
           "(:search IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(i.assetCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(i.model) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(i.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(i.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:departmentId IS NULL OR i.departmentId = :departmentId)")
    Page<Instrument> searchInstruments(@Param("search") String search,
                                       @Param("status") String status,
                                       @Param("departmentId") Long departmentId,
                                       Pageable pageable);
}
