package com.common.backoffice.uat.hri.web;

import java.util.List;
import java.util.Map;

import com.common.backoffice.uat.hri.service.ManagerInfoManagerService;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.PaginationHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자(TB_MANAGERINFO) 리스트 조회 API.
 * basic/backend(com.common.backoffice.uat.hri.web.ManagerInfoManagerController)를
 * 참조해서 did_emart 스키마 기준으로 리스트 조회만 우선 포팅함
 * (등록/수정/삭제/비밀번호 변경 등은 별도 작업으로 진행 예정).
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/hr/manager")
@Tag(name = "ManagerInfoManagerController", description = "관리자 관련 연동 API")
public class ManagerInfoManagerController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ManagerInfoManagerService managerService;

	@Operation(
			summary = "관리자 리스트 조회",
			description = "성공시 관리자 리스트를 반환합니다.",
			tags = {"ManagerInfoManagerController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/empList.do")
	public ResultVO selectUserManagerList(@RequestBody Map<String, Object> searchVO,
										   HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.put(Globals.PAGE_LOGIN_ROLEID, loginVO.getRoleId());
			searchVO.put(Globals.PAGE_LOGIN_PARTID, loginVO.getPartId());

			// searchVO는 JSON 요청 본문이라 pageSize/pageUnit이 문자열이 아니라 숫자(Integer)로
			// 들어올 수 있음 — PaginationHelper.buildInfo가 내부에서 toString()으로 안전하게
			// 처리하니 여기서 (String)으로 미리 캐스팅하지 않고 기본값만 넘김
			PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
					propertiesService.getInt(Globals.PAGE_UNIT), propertiesService.getInt(Globals.PAGE_SIZE));
			List<Map<String, Object>> managerList = managerService.selectManagerManageListByPagination(searchVO);
			PaginationHelper.setResult(resultVO, managerList, paginationInfo, searchVO);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectUserManagerList error", e, egovMessageSource);
		}
		return resultVO;
	}
}
