package com.common.backoffice.bas.code.mapper;

import java.util.List;
import java.util.Map;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import com.common.backoffice.bas.code.modals.CmmnDetailCode;
import com.common.backoffice.bas.code.modals.dto.CmmnDetailCodeDto;

@Mapper
public interface EgovCmmnDetailCodeManageMapper {

	public List<CmmnDetailCodeDto> selectCmmnDetailCodeListByPagination(String codeId);
	
	public List<Map<String, Object>>selectCmmnDetailList();
	
	public List<CmmnDetailCodeDto> selectCmmnDetailCombo (String code);
	
	public List<CmmnDetailCodeDto> selectCmmnDetailComboLamp (String code);
	
	public List<CmmnDetailCodeDto> selectCmmnDetailComboEtc(String code);
	
	public CmmnDetailCodeDto selectCmmnDetailCodeDetail(String code);
	
	public CmmnDetailCodeDto selectCmmnDetail(String code);
	
	public int insertCmmnDetailCode(CmmnDetailCode vo);
	
	public int updateCmmnDetailCode(CmmnDetailCode vo);
	
	public int deleteCmmnDetailCode(String code);
	
	public int deleteCmmnDetailCodeId(String value);
	
	public List<CmmnDetailCodeDto> selectCmmnDetailResTypeCombo (Map<String, Object> vo);
}
