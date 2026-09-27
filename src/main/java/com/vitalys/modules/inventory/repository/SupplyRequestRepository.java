package com.vitalys.modules.inventory.repository;

import com.vitalys.modules.inventory.entity.SupplyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyRequestRepository extends JpaRepository<SupplyRequest, Long> {
}
