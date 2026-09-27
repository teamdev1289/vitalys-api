package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.FormulationTrialComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormulationTrialComponentRepository extends JpaRepository<FormulationTrialComponent, Long> {
}
