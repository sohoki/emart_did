package com.common.backoffice.sts.mhs.mapper;

import com.common.backoffice.sts.mhs.modals.MhsViewConnInfo;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface MhsViewConnInfoManageMapper {

	public List<MhsViewConnInfoVO> selectViewMoniterClassInfo(MhsViewConnInfoVO searchVO);
	
	public List<MhsViewConnInfoVO> selectViewMoniterClassUninPageInfo(MhsViewConnInfoVO searchVO);
	
	public int selectViewMoniterClassUpdateInfoChange(String mhsMonitorcd);

	public int insertMoniterClassInfo(MhsViewConnInfo vo);
	
	public int updateMoniterClassInfo(MhsViewConnInfo vo);
	
	public int updateMoniterDidUpdateChange(String mhsMonitorcd);
	
	public int updateMoniterClassChangeInfo(String mhsClasscd);
	
	public int updateMoniterDidUpdateDayChange(String mhsMonitorcd);
	
	
	public int deleteMoniterClassInfo(String mhsConnSeq);
	
	
}
