package com.common.backoffice.sym.sch.mapper;

import com.common.backoffice.sym.sch.modals.ContentSendHistoryInfo;
import com.common.backoffice.sym.sch.modals.ContentSendHistoryInfoVO;
import com.common.backoffice.sym.sch.modals.ScheduleInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;



@Mapper
public interface ContentSendHistoryInfoManagerMapper  {

	public List<ScheduleInfoVO> selectContentSendHistoryInfoManage (ContentSendHistoryInfoVO searchVO);

	public int insertContentSendHistoryInfoManage(ContentSendHistoryInfo vo);
	
	public int  updateContentSendHistoryInfoManage (ContentSendHistoryInfo vo);
	
	public int updateContentSendHistoryDidInfoManage(String hisSeq);
	
	public int updateContentSendHistoryConInfoManage(String hisSeq);
	
	public int updateContentSendHistoryConPageInfoManage(String hisSeq);
	
	public int updateContentSendHistoryConFileInfoManage(String hisSeq);
	
	public int updateContentSchUpdateCheckReset(String hisSeq);
		
	public int deleteContentSendHistoryInfoManage(String schCode);
	
	
	
}
