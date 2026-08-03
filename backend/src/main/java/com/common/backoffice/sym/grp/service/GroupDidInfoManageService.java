package com.common.backoffice.sym.grp.service;


import java.util.List;

import com.common.backoffice.sym.grp.mapper.GroupDidInfoManagerMapper;
import com.common.backoffice.sym.grp.modals.GroupDidInfo;
import com.common.backoffice.sym.grp.modals.GroupDidInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class GroupDidInfoManageService {

	
	private final GroupDidInfoManagerMapper groupDidInfoManagerMapper;
	
	
	public List<GroupDidInfoVO> selectGroupInfoManageListByPagination(
			String groupCode) {
		// TODO Auto-generated method stub
		return groupDidInfoManagerMapper.selectGroupInfoManageListByPagination(groupCode);
	}

	
	public int selectGroupIDidCheckInfoManageListTotCnt_S(String groupId,
			String didId) {
		// TODO Auto-generated method stub
		return groupDidInfoManagerMapper.selectGroupIDidCheckInfoManageListTotCnt_S(groupId, didId);
	}

	
	public int selectGroupInfoManageListTotCnt_S(String groupCode) {
		// TODO Auto-generated method stub
		return groupDidInfoManagerMapper.selectGroupInfoManageListTotCnt_S(groupCode);
	}

    @Transactional(readOnly = false)
	public int insertGroupInfoManage(GroupDidInfo vo) {
		// TODO Auto-generated method stub
		return groupDidInfoManagerMapper.insertGroupInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteGroupInfoManage(String didId) {
		// TODO Auto-generated method stub
		return groupDidInfoManagerMapper.deleteGroupInfoManage(didId);
	}

	
	public List<GroupDidInfo> selectComboLst() {
		// TODO Auto-generated method stub
		return groupDidInfoManagerMapper.selectComboLst();
	}
	
	
}
