package com.common.backoffice.sym.grp.mapper;

import com.common.backoffice.sym.grp.modals.GroupDidInfo;
import com.common.backoffice.sym.grp.modals.GroupDidInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;


@Mapper
public interface GroupDidInfoManagerMapper {

    public List<GroupDidInfoVO> selectGroupInfoManageListByPagination(String groupCode);

    public int selectGroupIDidCheckInfoManageListTotCnt_S(String groupId, String didId);

    public int selectGroupInfoManageListTotCnt_S(String groupCode);

    public int insertGroupInfoManage(GroupDidInfo vo);

    public int deleteGroupInfoManage(String didId);


   public List<GroupDidInfo> selectComboLst();
	   
	   
	   
}
