package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.InventoryUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryUsageLogRepository extends JpaRepository<InventoryUsageLog, Long> {
}
