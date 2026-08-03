package com.common.backoffice.bas.role.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.common.backoffice.bas.role.modals.RoleInfo;
import com.common.backoffice.bas.role.modals.dto.RoleInfoRequestDto;

public interface RoleTService {

	
	List<Map<String, Object>> selectRoleInfoPageList( Map<String, Object> params);
	
	int updateRoleInfo(RoleInfoRequestDto vo);
	int deleteRoleInfo(String roleId);

	Optional<RoleInfo> selectRoleInfoDetail(String roleId);
}
