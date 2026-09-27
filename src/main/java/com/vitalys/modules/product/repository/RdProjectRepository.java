package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.RdProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RdProjectRepository extends JpaRepository<RdProject, Long> {
}
