package com.common.backoffice.sym.grp.mapper;


import com.common.backoffice.sym.grp.modals.GroupInfo;
import com.common.backoffice.sym.grp.modals.GroupInfoVO;
import org.apache.ibatis.annotations.Param;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;
import java.util.List;
import java.util.Map;


@Mapper
public interface GroupInfoManagerMapper {

	public List<GroupInfoVO>  selectGroupInfoManageListByPagination(@Param("params") Map<String, Object> searchVO);
	
	public List<GroupInfoVO> selectGroupInfoManageCombo(GroupInfoVO vo);
	
	public GroupInfo selectGroupInfoManageDetail (String groupCode);
	
	public int  selectGroupIDInfoManageListTotCnt_S(String groupCode);
	
	public int  selectGroupInfoManageListTotCnt_S(GroupInfoVO searchVO);
	
	public String selectLastInsertGroup();
	
	public int  insertGroupInfoManage(GroupInfo vo);
	
	public int  updateGroupInfoManage(GroupInfo vo);
	
	public int  deleteGroupInfoManage (String groupCode);
	
	
}
