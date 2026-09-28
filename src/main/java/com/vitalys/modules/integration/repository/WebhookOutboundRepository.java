package com.vitalys.modules.integration.repository;

import com.vitalys.modules.integration.entity.WebhookOutbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookOutboundRepository extends JpaRepository<WebhookOutbound, Long> {

    List<WebhookOutbound> findTop20ByOrderBySentAtDesc();

    Page<WebhookOutbound> findAllByOrderBySentAtDesc(Pageable pageable);
}
