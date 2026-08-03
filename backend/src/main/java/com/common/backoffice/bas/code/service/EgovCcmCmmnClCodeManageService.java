package com.common.backoffice.bas.code.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.code.mapper.EgovCmmnClCodeManageMapper;
import com.common.backoffice.bas.code.modals.CmmnClCode;
import com.common.backoffice.bas.code.modals.dto.CmmnClCodeReqDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class EgovCcmCmmnClCodeManageService {

	
	private final EgovCmmnClCodeManageMapper cmmMapper;
	
	
	public List<Map<String, Object>> selectCmmnClCodeListByPagination(Map<String, Object> params){	
		return cmmMapper.selectCmmnClCodeListByPagination(params);
	}
	
	public List<Map<String, Object>> selectCmmnClCodeList(){
		return cmmMapper.selectCmmnClCodeList();
	}
	
	public Optional<CmmnClCode> selectCmmnClCodeDetail(String clCode){
		return cmmMapper.selectCmmnClCodeDetail(clCode);
	}
	
	@Transactional(readOnly = false)
	public int updateCmmnClCode(CmmnClCodeReqDto vo) {
		return vo.getMode().equals("Ins") ? cmmMapper.insertCmmnClCode(vo) : cmmMapper.updateCmmnClCode(vo);
	}
	@Transactional(readOnly = false)
	public int deleteCmmnClCode(String clCode) {
		try {
			cmmMapper.deleteCmmnClCode(clCode);
			return 1;
		}catch(NullPointerException e1) {
			log.error("deleteCmmnClCode service:", e1);
			return 0;
		}catch(Exception e) {
			log.error("deleteCmmnClCode service:", e);
			return 0;
		}
	}
}
