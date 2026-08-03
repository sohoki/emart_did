package com.common.backoffice.bas.cnt.service;

import java.util.List;

import com.common.backoffice.bas.cnt.mapper.CenterInfoManagerMapper;
import com.common.backoffice.bas.cnt.modals.CenterInfo;
import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class CenterInfoManageService {

	
    private final CenterInfoManagerMapper centerInfoManagerMapper;
	
	
	
	public List<CenterInfoVO> selectCenterInfoManageListByPagination( CenterInfoVO searchVo) {
		return centerInfoManagerMapper.selectCenterInfoManageListByPagination(searchVo);
	}

	
	public List<CenterInfoVO> selectCenterInfoManageCombo(CenterInfoVO searchVO) {
		return centerInfoManagerMapper.selectCenterInfoManageCombo(searchVO);
	}

	
	public CenterInfoVO selectCenterInfoManageDetail(String centerId) {
		return centerInfoManagerMapper.selectCenterInfoManageDetail(centerId);
	}

	
	public int selectCenterInfoManageListTotCnt_S(CenterInfoVO searchVo) {
		return centerInfoManagerMapper.selectCenterInfoManageListTotCnt_S(searchVo);
	}

    @Transactional(readOnly = false)
	public int insertCenterInfoManage(CenterInfo vo) throws Exception {
		vo.setCenterId(centerInfoManagerMapper.selectCenterId());
		return centerInfoManagerMapper.insertCenterInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateCenterInfoManage(CenterInfo vo) {
		return centerInfoManagerMapper.updateCenterInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteCenterInfoManage(String centerId) {
		return centerInfoManagerMapper.deleteCenterInfoManage(centerId);
	}

	
	public String selectCenterTimeInfo(CenterInfo vo) {
		// TODO Auto-generated method stub
		return centerInfoManagerMapper.selectCenterTimeInfo(vo);
	}

	
	public List<CenterInfo> selectCenterBrodCombo(String centerId) {
		// TODO Auto-generated method stub
		return centerInfoManagerMapper.selectCenterBrodCombo(centerId);
	}

	
	public String selectCenterInfoBrod(String centerId) throws Exception {
		// TODO Auto-generated method stub
		return centerInfoManagerMapper.selectCenterInfoBrod(centerId);
	}
	public List<CenterInfoVO> selectGroupInCenterInfo(
			CenterInfoVO searchVo) {
		return centerInfoManagerMapper.selectGroupInCenterInfo(searchVo);
	}
	
}
