package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {
}
