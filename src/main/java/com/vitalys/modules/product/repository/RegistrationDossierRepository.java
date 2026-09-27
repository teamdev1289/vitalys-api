package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.RegistrationDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationDossierRepository extends JpaRepository<RegistrationDossier, Long> {
}
