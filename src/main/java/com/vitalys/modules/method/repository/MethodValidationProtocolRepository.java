package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.MethodValidationProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MethodValidationProtocolRepository extends JpaRepository<MethodValidationProtocol, Long> {
    List<MethodValidationProtocol> findByMethodId(Long methodId);
    Optional<MethodValidationProtocol> findByProtocolCode(String protocolCode);
}
