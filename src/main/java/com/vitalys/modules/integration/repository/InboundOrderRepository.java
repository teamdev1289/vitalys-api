package com.vitalys.modules.integration.repository;

import com.vitalys.modules.integration.entity.InboundOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {

    Optional<InboundOrder> findByExternalOrderId(String externalOrderId);

    @Query("SELECT o FROM InboundOrder o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:sourceSystem IS NULL OR o.sourceSystem = :sourceSystem) " +
           "ORDER BY o.receivedAt DESC")
    Page<InboundOrder> searchOrders(@Param("status") String status,
                                    @Param("sourceSystem") String sourceSystem,
                                    Pageable pageable);
}
