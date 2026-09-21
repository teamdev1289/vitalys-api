package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    Optional<SysUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM SysUser u WHERE " +
           "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "   OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:departmentId IS NULL OR u.department.id = :departmentId)")
    Page<SysUser> searchUsers(@Param("search") String search,
                              @Param("status") String status,
                              @Param("departmentId") Long departmentId,
                              Pageable pageable);
}
