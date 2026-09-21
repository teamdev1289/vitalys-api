package com.vitalys.modules.sys.repository;

import com.vitalys.modules.sys.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    List<SysPermission> findByModule(String module);

    Optional<SysPermission> findByModuleAndScreenAndAction(String module, String screen, String action);
}
