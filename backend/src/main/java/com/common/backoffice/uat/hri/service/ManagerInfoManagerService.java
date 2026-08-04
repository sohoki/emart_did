package com.common.backoffice.uat.hri.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.uat.hri.mapper.ManagerInfoManagerMapper;
import com.common.backoffice.uat.hri.models.dto.ManagerInfoReqDto;
import com.common.backoffice.uat.hri.models.dto.ManagerInfoResDto;

import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ManagerInfoManagerService {

	private final ManagerInfoManagerMapper managerMapper;

	public List<Map<String, Object>> selectManagerManageListByPagination(Map<String, Object> params) {
		return managerMapper.selectManagerManageListByPagination(params);
	}

	public Optional<ManagerInfoResDto> selectManagerManageDetail(String managerId) {
		return managerMapper.selectManagerManageDetail(managerId);
	}

	public int selectManagerUserMangerIDCheck(String managerId) {
		return managerMapper.selectManagerUserMangerIDCheck(managerId);
	}

	// 로그인과 동일하게 현재는 평문 비교(패스워드 암호화는 프로젝트 정책상 맨 마지막 단계에서 일괄 적용 예정 —
	// EgovLoginService.actionLogin()의 암호화 호출도 같은 이유로 주석 처리되어 있음)
	public int selectManagerPasswordCheck(Map<String, Object> params) {
		return managerMapper.selectManagerPasswordCheck(params);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int updateManagerManage(ManagerInfoReqDto vo) {
		return Globals.SAVE_MODE_INSERT.equals(vo.getMode())
				? managerMapper.insertManagerManage(vo)
				: managerMapper.updateManagerManage(vo);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int updatePassChange(ManagerInfoReqDto vo) {
		return managerMapper.updatePassChange(vo);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int updagteManageState(ManagerInfoReqDto vo) {
		return managerMapper.updagteManageState(vo);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int updateUseYn(ManagerInfoReqDto vo) {
		return managerMapper.updateUseYn(vo);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int deleteManagerMange(String managerId) {
		return managerMapper.deleteManagerMange(managerId);
	}
}
