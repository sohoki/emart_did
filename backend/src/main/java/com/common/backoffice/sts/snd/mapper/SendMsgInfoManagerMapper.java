package com.common.backoffice.sts.snd.mapper;

import com.common.backoffice.sts.snd.modals.SendMsgInfo;
import com.common.backoffice.sts.snd.modals.SendMsgInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface SendMsgInfoManagerMapper {

	
	
	public List<SendMsgInfoVO> selectSendMsgInfoManageListByPagination(SendMsgInfoVO searchVO);
	
	public  List<SendMsgInfoVO>  selectDIDOrderLst (SendMsgInfoVO searchVO);

	public int selectSendMsgInfoManageListTotCnt_S(SendMsgInfoVO searchVO);
	
	public int selectDIDScheduleCount(SendMsgInfo vo);
	
	public int selectDIDOrderCount ( SendMsgInfo vo);
	
	public int selectDIDMessageCount ( SendMsgInfo vo);
	
	public int insertSendMsgInfoManage(SendMsgInfo vo);
	
	public int insertSendMsgInfoManageState(SendMsgInfo vo);
	
	public int insertSendMessageInsertManage(SendMsgInfo vo);
	
	public int updateSendMsgInfoManage(SendMsgInfo vo);

	public int selectSendDidIDCheckCnt(String sendDidId);
	
	public String selectSendDidIDMsgSeq(String sendDidId);
}
