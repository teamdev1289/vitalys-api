package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByCode(String code);

    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(:search IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(i.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(i.casNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:category IS NULL OR i.category = :category) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<InventoryItem> searchItems(
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") String status,
            Pageable pageable);
}
