package com.vitalys.modules.partner.repository;

import com.vitalys.modules.partner.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<Project> findByCustomerId(Long customerId);

    @Query("SELECT p FROM Project p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(p.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:customerId IS NULL OR p.customerId = :customerId) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<Project> searchProjects(@Param("search") String search,
                                 @Param("customerId") Long customerId,
                                 @Param("status") String status,
                                 Pageable pageable);
}
