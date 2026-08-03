package com.common.backoffice.sts.brd.mapper;


import java.util.List;

import com.common.backoffice.sts.brd.modals.BasicBrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodScheduleInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

@Mapper
public interface BasicBrodScheduleManagerMapper {




	public List<BasicBrodScheduleInfoVO> selectBasicBrodScheduleLst(BasicBrodScheduleInfoVO searchVO);
	
	public int selectBasicBrodScheduleLstCnt(BasicBrodScheduleInfoVO searchVO);
	
	public List<BasicBrodScheduleInfoVO> selectBasicBrodScheduleCheckList(String centerGubun); 
	
	public String selectBasicBrodContentlDownCheck(BasicBrodScheduleInfoVO searchVO );
	
	public int insertBasicBrodSchedule(BasicBrodScheduleInfo vo);
	
	public int insertBasicBrodScheduleDistribute (String basicCode);
	
	public int updateBasicBrodScheduleCenter(BasicBrodScheduleInfo vo);
	
	public int updateBasicBrodSchedule(BasicBrodScheduleInfo vo);
	
	public int updateBasicBrodScheduleRestart(BasicBrodScheduleInfo vo);
	
	public int updateBasicCodeCenterUpdate(BasicBrodScheduleInfoVO vo);
	
	public int updateBasicBrodScheduleCenterE(BasicBrodScheduleInfoVO vo);
	
	public int updateBasicCodeCenterReset(String basicCode);
	
	public int updateBasicBrodScheduleCenterDownCheck(BasicBrodScheduleInfoVO vo);
	
	public int updateBasicBrodScheduleCenterStateChange(BasicBrodScheduleInfoVO vo);
	
	public int updateBasicBrodScheduleState(String basicCode);
	
	public int deleteBasicBrodScheduleCenter(BasicBrodScheduleInfo vo);

	public int deleteBasicBrodSchedule(BasicBrodScheduleInfo vo);
	
	public int deleteBasicBrodScheduleOther(BasicBrodScheduleInfo vo);
	
}
