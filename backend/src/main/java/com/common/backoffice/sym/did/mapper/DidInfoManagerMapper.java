package com.common.backoffice.sym.did.mapper;


import com.common.backoffice.sym.did.modals.DidInfo;
import com.common.backoffice.sym.did.modals.DidInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface DidInfoManagerMapper {

	public List<DidInfoVO>  selectDidInfoManageListByPagination(DidInfoVO searchVO);
	public List<DidInfoVO> selectDidInfoManageCombo();
	public DidInfoVO selectDidrInfoManageDetail(String didId);
	public DidInfoVO selectDidrInfoManageDetailView(String didId);
	
	//DID 현황
	public List<DidInfoVO>  selectDidManagerInfoManageListByPagination(DidInfoVO searchVO);
	
	public int selectDidManagerInfoManageListTotCnt_S(DidInfoVO searchVO);
	
	
	public String selectDIDMac (String didId);
	
	public int selectDidInfoManageListTotCnt_S(DidInfoVO searchVO);
	
	public List<?> selectDidDetailContentInfo(String s);

    public String selectLastInsertDid(String s);
	
	public int insertDidInfoManage(DidInfo vo);
	
	public int updateDidInfoManage(DidInfo vo);
	
	public int updateDidMac(DidInfo vo);
	
	public int updateDidState(DidInfo vo);
	
	public int updateDidEndTime (DidInfo vo);
	
	public int deleteDidInfoManage(String didId);
	
	public int test1();
	public int test2();
	
	
	public List<DidInfoVO>  selectIntegrateManageListByPagination(DidInfoVO searchVO);
	
	public List<DidInfoVO> selectIntegrateRoleList(DidInfoVO searchVO);
	public List<DidInfoVO> selectIntegrateCenterList(DidInfoVO searchVO);
	public List<DidInfoVO> selectIntegrateDeviceList(DidInfoVO searchVO);
	
}
