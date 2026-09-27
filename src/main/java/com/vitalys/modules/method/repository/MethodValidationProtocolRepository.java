package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.MethodValidationProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodValidationProtocolRepository extends JpaRepository<MethodValidationProtocol, Long> {
}
