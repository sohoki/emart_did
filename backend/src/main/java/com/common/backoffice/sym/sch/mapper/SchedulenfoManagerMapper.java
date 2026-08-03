package com.common.backoffice.sym.sch.mapper;

import com.common.backoffice.sym.sch.modals.ScheduleInfo;
import com.common.backoffice.sym.sch.modals.ScheduleInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface SchedulenfoManagerMapper {

	
	public List<ScheduleInfoVO> selectScheduleInfoManageListByPagination( ScheduleInfoVO searchVO );
	
	public List<ScheduleInfo> selectScheduleInfoManageCombo();
	
	public ScheduleInfoVO selectScheduleInfoManageDetail(String schCode);
	
	public ScheduleInfoVO selectScheduleInfoManageDetailView(String schCode);
	
	public String selectScheduleMaxInfo ();
	
	public List<ScheduleInfo> selectScheduleConSeqList(String contentCode);
	
	public int  selectScheduleInfoManageListTotCnt_S( ScheduleInfoVO searchVO );
	
	public int  insertScheduleInfoManage(ScheduleInfo vo);
	
	public int  updateScheduleInfoManage(ScheduleInfo vo);
	
	public int  deleteScheduleInfoManage(String schCode);
	
}
