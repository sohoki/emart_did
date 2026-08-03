package com.common.backoffice.uat.hri.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface ManagerInfoManagerMapper {

	public List<Map<String, Object>> selectManagerManageListByPagination(@Param("params") Map<String, Object> params);
}
