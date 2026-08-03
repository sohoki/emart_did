package com.common.backoffice.use.mapper;

import com.common.backoffice.use.modals.Group;
import com.common.backoffice.use.modals.GroupVo;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;


@Mapper
public interface GroupManagerMapper {

	public List<GroupVo> selectUserGroupManageListByPagination(GroupVo vo);
	
	public Group selectGroupManageDetail(String codeId);
	
	public int insertGroupManage(Group vo);
	
	public int insertGroupManageMhs(Group vo);
	
	public int updateGroupManage(Group vo);
	
	public int deleteGroupManage(String codeId);
		
	public int selectGroupManageListTotCnt_S(GroupVo vo);
	
	public List<GroupVo> selectGroupManageCombo(GroupVo vo);
	
	public String selectGroupCode();
	
	
}
