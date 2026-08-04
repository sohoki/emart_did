package com.common.backoffice.uat.hri.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import com.common.backoffice.uat.hri.models.dto.ManagerInfoReqDto;
import com.common.backoffice.uat.hri.models.dto.ManagerInfoResDto;

@Mapper
public interface ManagerInfoManagerMapper {

	public List<Map<String, Object>> selectManagerManageListByPagination(@Param("params") Map<String, Object> params);

	public Optional<ManagerInfoResDto> selectManagerManageDetail(String managerId);

	public int selectManagerUserMangerIDCheck(String managerId);

	public int selectManagerPasswordCheck(@Param("params") Map<String, Object> params);

	public int insertManagerManage(ManagerInfoReqDto vo);

	public int updateManagerManage(ManagerInfoReqDto vo);

	public int updatePassChange(ManagerInfoReqDto vo);

	public int updagteManageState(ManagerInfoReqDto vo);

	public int updateUseYn(ManagerInfoReqDto vo);

	public int deleteManagerMange(String managerId);
}
