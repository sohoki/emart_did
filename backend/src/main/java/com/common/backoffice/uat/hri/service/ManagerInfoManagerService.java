package com.common.backoffice.uat.hri.service;

import java.util.List;
import java.util.Map;

import com.common.backoffice.uat.hri.mapper.ManagerInfoManagerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ManagerInfoManagerService {

	private final ManagerInfoManagerMapper managerMapper;

	public List<Map<String, Object>> selectManagerManageListByPagination(Map<String, Object> params) {
		return managerMapper.selectManagerManageListByPagination(params);
	}
}
