package com.common.backoffice.bas.code.mapper;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import org.apache.ibatis.annotations.Param;
import com.common.backoffice.bas.code.modals.dto.CmmnCodeDto;
import com.common.backoffice.bas.code.modals.dto.CmmnCodeReqDto;

@Mapper
public interface EgovCmmnCodeManageMapper {

    public List<CmmnCodeDto> selectCmmnCodeListByPagination(@Param("params") Map<String, Object> params);
	
	public List<CmmnCodeDto> selectCmmnCodeList();
	
	public CmmnCodeDto selectCmmnCodeDetail(String codeId);
	
	public int insertCmmnCode(CmmnCodeReqDto vo);
	
	public int updateCmmnCode(CmmnCodeReqDto vo);
	
	public int deleteCmmnCode(String codeId);
}
