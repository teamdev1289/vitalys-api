package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.InventoryLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {

    Optional<InventoryLot> findByLotNumber(String lotNumber);

    List<InventoryLot> findByInventoryItemIdAndStatus(Long inventoryItemId, String status);

    @Query("SELECT l FROM InventoryLot l WHERE " +
           "(:inventoryItemId IS NULL OR l.inventoryItemId = :inventoryItemId) AND " +
           "(CAST(:status AS string) IS NULL OR l.status = CAST(:status AS string)) AND " +
           "(CAST(:search AS string) IS NULL OR LOWER(l.lotNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(l.manufacturer) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<InventoryLot> searchLots(
            @Param("inventoryItemId") Long inventoryItemId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT l FROM InventoryLot l WHERE l.status = 'AVAILABLE' AND l.expiryDate <= :expiryThreshold")
    List<InventoryLot> findLotsExpiringBefore(@Param("expiryThreshold") OffsetDateTime expiryThreshold);
}
