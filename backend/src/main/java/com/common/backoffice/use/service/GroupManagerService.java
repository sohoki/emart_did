package com.common.backoffice.use.service;


import java.util.List;


import com.common.backoffice.use.mapper.GroupManagerMapper;
import com.common.backoffice.use.modals.Group;
import com.common.backoffice.use.modals.GroupVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class GroupManagerService {

	
	private final GroupManagerMapper groupManagerMapper;

    @Transactional(readOnly = false)
	public int deleteGroupManage(String codeId) throws Exception {
		return groupManagerMapper.deleteGroupManage(codeId);
	}

    @Transactional(readOnly = false)
	public int insertGroupManage(Group vo) throws Exception {
		return groupManagerMapper.insertGroupManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateGroupManage(Group vo) throws Exception {
		return groupManagerMapper.updateGroupManage(vo);
	}

	
	public Group selectGroupManageDetail(String codeId) throws Exception {
		return groupManagerMapper.selectGroupManageDetail(codeId);
	}

	
	public List<GroupVo> selectUserGroupManageListByPagination(GroupVo searchVO)
			throws Exception {
		return groupManagerMapper.selectUserGroupManageListByPagination(searchVO);
	}

	
	public List<GroupVo> selectGroupManageCombo(GroupVo vo) throws Exception {
		return groupManagerMapper.selectGroupManageCombo(vo);
	}

	
	public int selectGroupManageListTotCnt_S(GroupVo searchVO) throws Exception {
		return groupManagerMapper.selectGroupManageListTotCnt_S(searchVO);
	}

    @Transactional(readOnly = false)
	public int insertGroupManageMhs(Group vo) throws Exception {
		// TODO Auto-generated method stub
		return groupManagerMapper.insertGroupManageMhs(vo);
	}


	/**
	 * 신규 부서(GROUP_ID) 채번. DB 함수 FN_GROUPCODE()를 그대로 사용함(LETTNAUTHORGROUPINFO
	 * 기준 MAX+1, EMART_+13자리).
	 */
	@Transactional(readOnly = false)
	public String generateGroupId() throws Exception {
		return groupManagerMapper.selectGroupCode();
	}

}
