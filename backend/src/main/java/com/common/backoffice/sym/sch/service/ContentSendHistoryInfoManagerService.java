package com.common.backoffice.sym.sch.service;


import java.util.List;

import com.common.backoffice.sym.sch.mapper.ContentSendHistoryInfoManagerMapper;
import com.common.backoffice.sym.sch.modals.ContentSendHistoryInfo;
import com.common.backoffice.sym.sch.modals.ContentSendHistoryInfoVO;
import com.common.backoffice.sym.sch.modals.ScheduleInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentSendHistoryInfoManagerService {

	private final ContentSendHistoryInfoManagerMapper SendHistoryInfo;
	
	
	public List<ScheduleInfoVO> selectContentSendHistoryInfoManage(
			ContentSendHistoryInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.selectContentSendHistoryInfoManage(searchVO);
	}

    @Transactional(readOnly = false)
	public int insertContentSendHistoryInfoManage(ContentSendHistoryInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.insertContentSendHistoryInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentSendHistoryInfoManage(ContentSendHistoryInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.updateContentSendHistoryInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentSendHistoryDidInfoManage(String hisSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.updateContentSendHistoryDidInfoManage(hisSeq);
	}

    @Transactional(readOnly = false)
	public int deleteContentSendHistoryInfoManage(String schCode)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.deleteContentSendHistoryInfoManage(schCode);
	}

    @Transactional(readOnly = false)
	public int updateContentSendHistoryConInfoManage(String hisSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.updateContentSendHistoryConInfoManage(hisSeq);
	}

    @Transactional(readOnly = false)
	public int updateContentSendHistoryConPageInfoManage(String hisSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.updateContentSendHistoryConPageInfoManage(hisSeq);
	}

    @Transactional(readOnly = false)
	public int updateContentSendHistoryConFileInfoManage(String hisSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.updateContentSendHistoryConFileInfoManage(hisSeq);
	}

    @Transactional(readOnly = false)
	public int updateContentSchUpdateCheckReset(String hisSeq) throws Exception {
		// TODO Auto-generated method stub
		return SendHistoryInfo.updateContentSchUpdateCheckReset(hisSeq);
	}

}
