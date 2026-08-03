package com.common.backoffice.sym.grp.service;


import java.util.List;
import java.util.Map;

import com.common.backoffice.sym.grp.mapper.GroupInfoManagerMapper;
import com.common.backoffice.sym.grp.modals.GroupInfo;
import com.common.backoffice.sym.grp.modals.GroupInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class GroupInfoManageService {

	private final GroupInfoManagerMapper groupInfoManagerMapper;
	
	
	public List<GroupInfoVO> selectGroupInfoManageListByPagination(
            Map<String, Object> searchVO) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.selectGroupInfoManageListByPagination(searchVO);
	}

	
	public List<GroupInfoVO> selectGroupInfoManageCombo(GroupInfoVO searchVO) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.selectGroupInfoManageCombo(searchVO);
	}

	
	public GroupInfo selectGroupInfoManageDetail(String groupCode) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.selectGroupInfoManageDetail(groupCode);
	}

	
	public int selectGroupIDInfoManageListTotCnt_S(String groupCode) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.selectGroupIDInfoManageListTotCnt_S(groupCode);
	}

	
	public int selectGroupInfoManageListTotCnt_S(GroupInfoVO searchVO) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.selectGroupInfoManageListTotCnt_S(searchVO);
	}
	
	
	public String selectLastInsertGroup() {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.selectLastInsertGroup();
	}

    @Transactional(readOnly = false)
	public int insertGroupInfoManage(GroupInfo vo) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.insertGroupInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateGroupInfoManage(GroupInfo vo) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.updateGroupInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteGroupInfoManage(String groupCode) {
		// TODO Auto-generated method stub
		return groupInfoManagerMapper.deleteGroupInfoManage(groupCode);
	}
	
	
	
}
