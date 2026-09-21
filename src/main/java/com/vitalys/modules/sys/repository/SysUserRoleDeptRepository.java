package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysUserRoleDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserRoleDeptRepository extends JpaRepository<SysUserRoleDept, Long> {

    List<SysUserRoleDept> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM SysUserRoleDept urd WHERE urd.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
