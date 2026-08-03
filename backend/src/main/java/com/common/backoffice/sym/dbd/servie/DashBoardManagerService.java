package com.common.backoffice.sym.dbd.servie;

import java.util.List;

import com.common.backoffice.sym.dbd.mapper.DashBoardManagerMapper;
import com.common.backoffice.sym.dbd.modals.DashBoardInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class DashBoardManagerService  {

	
	private final DashBoardManagerMapper dashboard;
	
	
	public DashBoardInfo selectDidStatus() throws Exception {
		// TODO Auto-generated method stub
		return dashboard.selectDidStatus();
	}

	
	public DashBoardInfo selectBrodStatus() throws Exception {
		// TODO Auto-generated method stub
		return dashboard.selectBrodStatus();
	}

	
	public int dashStateUpdateStep01() throws Exception {
		// TODO Auto-generated method stub
		return dashboard.dashStateUpdateStep01();
	}

	
	public int dashStateUpdateStep02() throws Exception {
		// TODO Auto-generated method stub
		return dashboard.dashStateUpdateStep02();
	}

	
	public List<DashBoardInfo> selectBrodStatusPage01(DashBoardInfo searchVo) throws Exception {
		// TODO Auto-generated method stub
		return dashboard.selectBrodStatusPage01(searchVo);
	}

	
	public List<DashBoardInfo> selectBrodStatusPage02(DashBoardInfo searchVo) throws Exception {
		// TODO Auto-generated method stub
		return dashboard.selectBrodStatusPage02(searchVo);
	}

	
	public int selectBrodStatusPage01Cnt() throws Exception {
		// TODO Auto-generated method stub
		return dashboard.selectBrodStatusPage01Cnt();
	}

	
	public int selectBrodStatusPage02Cnt() throws Exception {
		// TODO Auto-generated method stub
		return dashboard.selectBrodStatusPage02Cnt();
	}

	

}
