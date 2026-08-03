package com.common.backoffice.bas.role.service.repository;

import com.common.backoffice.bas.role.modals.RoleTInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleTRepository extends JpaRepository<RoleTInfo, String> {

    void deleteByRoleId(String roleId);

    boolean existsByRoleId(String roleId);

    Optional<RoleTInfo> findByRoleId(String roleId);
}
