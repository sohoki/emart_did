package com.common.backoffice.bas.code.service;

import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.code.service.repository.ComtccmmcodeRepository;
import com.common.backoffice.bas.code.service.repository.ComtccmmndetailcodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.code.mapper.EgovCmmnCodeManageMapper;
import com.common.backoffice.bas.code.modals.dto.CmmnCodeDto;
import com.common.backoffice.bas.code.modals.dto.CmmnCodeReqDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Transactional(value = "txManager", readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class EgovCcmCmmnCodeManageService {

	private final EgovCmmnCodeManageMapper cmmMapper;
	private final ComtccmmcodeRepository repository;
    private final ComtccmmndetailcodeRepository detailRepository;



	public List<CmmnCodeDto> selectCmmnCodeListByPagination(Map<String, Object> params){
		return cmmMapper.selectCmmnCodeListByPagination(params);
	}
	
	public List<CmmnCodeDto> selectCmmnCodeList(){
		return cmmMapper.selectCmmnCodeList();
	}
	
	public CmmnCodeDto selectCmmnCodeDetail(String codeId) {
		return cmmMapper.selectCmmnCodeDetail(codeId);
	}


    public boolean existsByCodeId(String codeId){
        return repository.existsByCodeId(codeId);

    }

	@Transactional(readOnly = false)
	public int updateCmmnCode(CmmnCodeReqDto vo) {
		return vo.getMode().equals("Ins") ? cmmMapper.insertCmmnCode(vo) : cmmMapper.updateCmmnCode(vo);
	}
    @Transactional("jpaTransactionManager")
    public int updateCmmnUseYnCode(CmmnCodeReqDto vo) {
        try {
            int ret  = repository.updateUseAtOnly(vo.getUseAt(), vo.getUserId(), vo.getCodeId());
            return ret;
        }catch(Exception e) {

            log.error("updateCmmnCode error:"+ e.toString());
            return -1;
        }
    }
	@Transactional("jpaTransactionManager")
	public int deleteCmmnCode(String codeId) {
		try {
			repository.deleteById(codeId);
            detailRepository.deleteByCodeId(codeId);
			return 1;
		}catch(NullPointerException e1) {
			log.error("deleteCmmnCode service:", e1);
			return 0;
		}catch(Exception e){
			log.error("deleteCmmnCode service:", e);
			return 0;
		}
	}


}
