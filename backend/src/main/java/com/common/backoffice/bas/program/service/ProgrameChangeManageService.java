package com.common.backoffice.bas.program.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.program.mapper.ProgrameChangeManageMapper;
import com.common.backoffice.bas.program.modals.ProgrameChangeInfo;

import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProgrameChangeManageService {

	@Autowired
	private ProgrameChangeManageMapper programChnageMapper;
	
	
	public Map<String, Object> selectProgrmChangeRequst(Map<String, Object> vo) throws Exception {
		return programChnageMapper.selectProgrmChangeRequst(vo);
	}

	
	public List<Map<String, Object>> selectProgrmChangeRequstList(Map<String, Object> params) throws Exception {
		return programChnageMapper.selectProgrmChangeRequstList(params);
	}

	@Transactional(readOnly = false)
	public int updateProgrmChangeRequst(ProgrameChangeInfo vo) throws Exception {
		return (vo.getMode().equals(Globals.SAVE_MODE_INSERT)) ? programChnageMapper.insertProgrmChangeRequst(vo) : programChnageMapper.updateProgrmChangeRequst(vo);
		
	}

	@Transactional(readOnly = false)
	public void deleteProgrmChangeRequst(ProgrameChangeInfo vo) throws Exception {
		programChnageMapper.deleteProgrmChangeRequst(vo);
	}

	
	public String selectProgrmChangeRequstNo() throws Exception {
		return programChnageMapper.selectProgrmChangeRequstNo();
	}

	
	public List<Map<String, Object>> selectChangeRequstProcessList(Map<String, Object> params) throws Exception {
		return programChnageMapper.selectChangeRequstProcessList(params);
	}

	@Transactional(readOnly = false)
	public int updateProgrmChangeRequstProcess(ProgrameChangeInfo vo) throws Exception {
		return programChnageMapper.updateProgrmChangeRequstProcess(vo);
	}
}
