package com.common.backoffice.sts.brd.mapper;

import com.common.backoffice.sts.brd.modals.BrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;



@Mapper
public interface BrodScheduleManagerMapper {

	
	public List<BrodScheduleInfoVO> selectBrodRigthLst (BrodScheduleInfoVO searchVO);
	
	public List<BrodScheduleInfoVO> selectBrodScheduleStatusLst (BrodScheduleInfoVO searchVO);
	
	public List<BrodScheduleInfoVO>  selectBrodScheduleUpdateChanage (String bordCode);
	
	public List<BrodScheduleInfo> selectBrodScheduleCreateCheckList (BrodScheduleInfo searchVO);
	
	public int selectBrodScheduleStatusPageCnt(BrodScheduleInfoVO searchVO);
	
	public int selectBrodScheduleCnt(BrodScheduleInfo searchVO);
	
	public String selectBrodScheduleAnniScheduleSeqDay(BrodScheduleInfo searchVO);
	
	public int selectBordScheduleCount(BrodScheduleInfo vo);
	
	public int selectBrodScheduleCenterCnt(String centerId);
	
	public int selectBrodScheduleStateCnt(BrodScheduleInfo vo);
	
	public BrodScheduleInfo selectBordScheduleCode(BrodScheduleInfo vo);
	
	public BrodScheduleInfo selectBordScheduleCodeRedown(BrodScheduleInfo vo);
	//신규 부분 
    public BrodScheduleInfo selectBordScheduleCodeNew(BrodScheduleInfo vo);
	
	public BrodScheduleInfo selectBordScheduleCodeRedownNew(BrodScheduleInfo vo);
	//신규 부분  끝 부분 		
	public int insertBrodSchedule(BrodScheduleInfo vo);
	
	public int updateBrodSchedule(BrodScheduleInfo vo);
	
	public int updateBrodScheduleReset(BrodScheduleInfo vo);
	
	public int updateBrodScheduleCenter(BrodScheduleInfo vo);
	
	public int updateBrodDidCenterID(BrodScheduleInfo vo);
	
	public int updateBrodScheduleCenterBrod(BrodScheduleInfo vo);
	
	public int updateCenterSchedule(BrodScheduleInfo vo);
	
	public int updateBrodScheduleCenterNotUse(String centerId);
	
	public int deleteBrodSchedule(BrodScheduleInfo vo);
	
	public int deleteBrodScheduleSeq(String scheduleSeq);
	
	public int deleteBrodScheduleOther(BrodScheduleInfo vo);
	
	public int deleteBrodScheduleAll(String brodCode);
	
	public int deleteBrodScheduleState(BrodScheduleInfo vo);
}
