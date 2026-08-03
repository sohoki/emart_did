package com.common.backoffice.sym.sch.service;

import java.util.List;

import com.common.backoffice.sym.sch.mapper.SchedulenfoManagerMapper;
import com.common.backoffice.sym.sch.modals.ScheduleInfo;
import com.common.backoffice.sym.sch.modals.ScheduleInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ScheduleInfoManageService {

	private final SchedulenfoManagerMapper schedulenfoManagerMapper;
	
	
	public List<ScheduleInfoVO> selectScheduleInfoManageListByPagination(
			ScheduleInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleInfoManageListByPagination(searchVO);
	}

	
	public List<ScheduleInfo> selectScheduleInfoManageCombo() throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleInfoManageCombo();
	}

	
	public ScheduleInfoVO selectScheduleInfoManageDetail(String schCode)
			throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleInfoManageDetail(schCode);
	}

	
	public ScheduleInfoVO selectScheduleInfoManageDetailView(String schCode)
			throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleInfoManageDetailView(schCode);
	}

	
	public int selectScheduleInfoManageListTotCnt_S(ScheduleInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleInfoManageListTotCnt_S(searchVO);
	}

    @Transactional(readOnly = false)
	public int insertScheduleInfoManage(ScheduleInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.insertScheduleInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateScheduleInfoManage(ScheduleInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.updateScheduleInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteScheduleInfoManage(String schCode) throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.deleteScheduleInfoManage(schCode);
	}

	
	public String selectScheduleMaxInfo() throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleMaxInfo();
	}

	
	public List<ScheduleInfo> selectScheduleConSeqList(String contentCode)
			throws Exception {
		// TODO Auto-generated method stub
		return schedulenfoManagerMapper.selectScheduleConSeqList(contentCode);
	}

}
