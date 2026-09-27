package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.FormulationTrial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormulationTrialRepository extends JpaRepository<FormulationTrial, Long> {
}
