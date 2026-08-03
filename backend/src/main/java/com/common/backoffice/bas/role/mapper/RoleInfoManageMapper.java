package com.common.backoffice.bas.role.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import com.common.backoffice.bas.role.modals.RoleInfo;
import com.common.backoffice.bas.role.modals.dto.RoleInfoRequestDto;

@Mapper
public interface RoleInfoManageMapper {

	
	public List<Map<String, Object>> selectRoleInfoPageList(@Param("params") Map<String, Object> params);
	
	public List<Map<String, Object>> selectRoleInfoComboList(@Param("params") Map<String, Object> params);
	
	public int insertRoleInfo(RoleInfoRequestDto vo);
		
	public int updateRoleInfo(RoleInfoRequestDto vo);

    public int updateRoleUseynInfo(RoleInfoRequestDto vo);

	public int deleteRoleInfo(String roleId);

	public Optional<RoleInfo> selectRoleInfoDetail(String roleId);
}
