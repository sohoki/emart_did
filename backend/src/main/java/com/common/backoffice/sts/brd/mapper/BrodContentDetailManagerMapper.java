package com.common.backoffice.sts.brd.mapper;


import com.common.backoffice.sts.brd.modals.BrodContentDetail;
import com.common.backoffice.sts.brd.modals.BrodContentDetailVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface BrodContentDetailManagerMapper {
	
	public List<BrodContentDetailVO> selectBrodContentDetailLst(BrodContentDetailVO searchVO);
	
	public List<BrodContentDetail> selectTimeCombo(String endTime);
	
	public List<BrodContentDetail> selectTimeHourCombo();
	
	public List<BrodContentDetail> selectBrodFileList(BrodContentDetail vo);
	
	public BrodContentDetailVO selectBrodContenDetailt(String brodSeq);
	
	public int insertLogFileMp3Data(Map<String, String> map);
	
	public int selectContentRegCnt(BrodContentDetail vo);
	
	public int selectContentRegTimeOverCheck(BrodContentDetail vo);
	
	public int selectContentRegTimeImsiOverTableCheck(BrodContentDetail vo);
	
	public int selectContentFileTime (String atchFileId);
	
	public int selectBrodContentDetailPageCnt(BrodContentDetailVO searchVO);
	
	public int insertBrodContentDetail(BrodContentDetail vo);
	
	public int insertBrodContentCopy(BrodContentDetail vo);
	
	public int insertBrodContentCenterCopy(BrodContentDetail vo);
	
	public int insertBrodContentScheduleOtherCopy(String brodCode);
	
	public int updateBrodContentDetail(BrodContentDetail vo);
	
	public int deleteBrodContentDetail(String brodSeq); 
	
	public int deleteBrodContentTimeDel(BrodContentDetail vo);
	
	public int deleteBrodContentBrodCode(String brodCode);
	
	public int deleteBrodContentBrodCodeALL(String brodCode);
	
	public int deleteBrodBasicBrod(String brodCode);
	           
	public int deleteContentDetailBasicContent(BrodContentDetail vo);
	
}
