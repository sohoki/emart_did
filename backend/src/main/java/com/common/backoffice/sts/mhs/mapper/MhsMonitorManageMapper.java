package com.common.backoffice.sts.mhs.mapper;

import com.common.backoffice.sts.mhs.modals.MhsMonitorInfo;
import com.common.backoffice.sts.mhs.modals.MhsMonitorInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface MhsMonitorManageMapper {
	
	public List<MhsMonitorInfoVO> selectMhsMonitorList(MhsMonitorInfoVO mhsMonitorInfoVO);
   
	public int selectMhsMonitorListCnt(MhsMonitorInfoVO mhsMonitorInfoVO);
	
	public MhsMonitorInfoVO selectMhsMonitorInfo(String mhsMonitorcd);
	
	public List<MhsMonitorInfoVO> selectMhsMonitorCombo(String mhsCentercd);

	public String selectMhsMonitorId(String mhsCentercd);

	public int insertMhsMonitorInfo (MhsMonitorInfo vo);
	
	public int updateMhsMonitorInfo (MhsMonitorInfo vo);
	
	public int updateMhsMonitorInfoIpMac (MhsMonitorInfo vo);
	
	public int updateMhsMonitorInfoStatus(String mhsMonitorcd);
	
	public int deleteMhsMonitorInfo(String mhsMonitorcd);
}
