package com.common.backoffice.sym.did.service;


import java.util.List;

import com.common.backoffice.sym.did.mapper.DidInfoManagerMapper;
import com.common.backoffice.sym.did.modals.DidInfo;
import com.common.backoffice.sym.did.modals.DidInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class DidInfoManageService {

	
	
	private final DidInfoManagerMapper didInfoManagerMapper;
	
	
	public List<DidInfoVO> selectDidInfoManageListByPagination(
			DidInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidInfoManageListByPagination(searchVO);
	}

	
	public List<DidInfoVO> selectDidInfoManageCombo() throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidInfoManageCombo();
	}

	
	public DidInfoVO selectDidrInfoManageDetail(String didId) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidrInfoManageDetail(didId);
	}

	
	public int selectDidInfoManageListTotCnt_S(DidInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidInfoManageListTotCnt_S(searchVO);
	}

    @Transactional(readOnly = false)
	public int insertDidInfoManage(DidInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.insertDidInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateDidInfoManage(DidInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.updateDidInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteDidInfoManage(String  didId) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.deleteDidInfoManage(didId);
	}

	
	public DidInfoVO selectDidrInfoManageDetailView(String didId) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidrInfoManageDetailView(didId);
	}

    @Transactional(readOnly = false)
	public int updateDidMac(DidInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.updateDidMac(vo);
	}

    @Transactional(readOnly = false)
	public int updateDidState(DidInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.updateDidState(vo);
	}

    @Transactional(readOnly = false)
	public int updateDidEndTime(DidInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.updateDidEndTime(vo);
	}

	
	public List<DidInfoVO> selectDidManagerInfoManageListByPagination(
			DidInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidManagerInfoManageListByPagination(searchVO);
	}

	
	public int selectDidManagerInfoManageListTotCnt_S(DidInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidManagerInfoManageListTotCnt_S(searchVO);
	}

	
	public String selectDIDMac(String didId) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDIDMac(didId);
	}

	
	public List<?> selectDidDetailContentInfo(String didId) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectDidDetailContentInfo(didId);
	}

	
	public String selectLastInsertDid(String centerId) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectLastInsertDid(centerId);
	}

	
	
	
	
	
	public List<DidInfoVO> selectIntegrateManageListByPagination(
			DidInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectIntegrateManageListByPagination(searchVO);
	}

	
	public List<DidInfoVO> selectIntegrateRoleList(DidInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectIntegrateRoleList(searchVO);
	}

	
	public List<DidInfoVO> selectIntegrateCenterList(DidInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectIntegrateCenterList(searchVO);
	}

	
	public List<DidInfoVO> selectIntegrateDeviceList(DidInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return didInfoManagerMapper.selectIntegrateDeviceList(searchVO);
	}
	
	
	
	@Transactional
	public int test() throws Exception{
		
		return 0;
	}
}
