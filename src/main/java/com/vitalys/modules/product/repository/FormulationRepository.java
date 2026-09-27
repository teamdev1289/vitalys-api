package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.Formulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormulationRepository extends JpaRepository<Formulation, Long> {
}
