package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.Method;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodRepository extends JpaRepository<Method, Long> {
}
