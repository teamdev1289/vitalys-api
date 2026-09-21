package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    Optional<SysRole> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT r FROM SysRole r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<SysRole> findByIdWithPermissions(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM SysRole r LEFT JOIN FETCH r.permissions")
    List<SysRole> findAllWithPermissions();
}
