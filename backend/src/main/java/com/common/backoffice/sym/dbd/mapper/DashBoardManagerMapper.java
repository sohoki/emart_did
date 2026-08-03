package com.common.backoffice.sym.dbd.mapper;

import com.common.backoffice.sym.dbd.modals.DashBoardInfo;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface DashBoardManagerMapper {

	
	public DashBoardInfo selectDidStatus();
	
	public DashBoardInfo selectBrodStatus();
	
	
	public List<DashBoardInfo>selectBrodStatusPage01(DashBoardInfo searchVo);
	
	public List<DashBoardInfo>selectBrodStatusPage02(DashBoardInfo searchVo);
	
	public int selectBrodStatusPage01Cnt();
	
	public int selectBrodStatusPage02Cnt();
	
	int dashStateUpdateStep01();
	
	int dashStateUpdateStep02();
	
	
}
