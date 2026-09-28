package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByBatchNumber(String batchNumber);

    boolean existsByBatchNumber(String batchNumber);

    boolean existsByBatchNumberAndIdNot(String batchNumber, Long id);

    List<Batch> findByProductId(Long productId);

    @Query("SELECT b FROM Batch b WHERE " +
           "(:search IS NULL OR LOWER(b.batchNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(b.notes) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:productId IS NULL OR b.productId = :productId) " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<Batch> searchBatches(@Param("search") String search,
                              @Param("productId") Long productId,
                              @Param("status") String status,
                              Pageable pageable);
}
