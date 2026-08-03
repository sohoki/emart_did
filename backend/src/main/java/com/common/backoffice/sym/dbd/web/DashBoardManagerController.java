package com.common.backoffice.sym.dbd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sym.dbd.modals.DashBoardInfo;
import com.common.backoffice.sym.dbd.servie.DashBoardManagerService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영 대시보드(방송/DID 상태 현황) 조회 API.
 * did_emart(REST + JWT + tb_did, tb_brod 관련 테이블, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 → JWT(AuthHelper) 기반 인증(원본은 loginVO를 받기만 하고 실제로 쓰지 않았음)
 * - JSP 뷰 반환 → ResultVO(JSON) 반환
 * - 원본은 `egovframework.let.sym.dbd.service.{DashBoardInfo, DashBoardManagerService}`(존재하지
 *   않는 패키지)를 import하고 있어 컴파일 자체가 안 되는 상태였음. 실제 클래스는
 *   `com.common.backoffice.sym.dbd.{modals.DashBoardInfo, servie.DashBoardManagerService}`
 *   (서비스 패키지명 오타 "servie" 그대로 존재)에 이미 postgresql 기준으로 정상 포팅되어
 *   있었음 — 컨트롤러의 import만 잘못돼 있었던 것
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/equiManage/dashboard")
@Tag(name = "DashBoardManagerController", description = "운영 대시보드(방송/DID 상태) 조회")
public class DashBoardManagerController {

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	private final DashBoardManagerService dashboard;

	@Operation(summary = "대시보드 요약 현황 조회", description = "방송/DID 상태를 최신화(배치 갱신)한 뒤 요약 현황을 반환합니다.")
	@GetMapping("/state.do")
	public ResultVO selectDashBoardList(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			dashboard.dashStateUpdateStep01();
			dashboard.dashStateUpdateStep02();

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("resultListBrod", dashboard.selectBrodStatus());
			resultMap.put("resultListDid", dashboard.selectDidStatus());
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDashBoardList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 상태 페이징 목록 조회")
	@GetMapping("/state1.do")
	public ResultVO selectDashBoardListDid(DashBoardInfo searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
			}
			searchVO.setPageSize(propertiesService.getInt("pageSize"));

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			List<DashBoardInfo> resultList = dashboard.selectBrodStatusPage01(searchVO);
			int totCnt = dashboard.selectBrodStatusPage01Cnt();
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, resultList);
			resultMap.put("regist", searchVO);
			resultMap.put("paginationInfo", paginationInfo);
			resultMap.put("totalCnt", totCnt);
			resultMap.put("resultListDid", dashboard.selectDidStatus());
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDashBoardListDid", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송 상태 페이징 목록 조회")
	@GetMapping("/state2.do")
	public ResultVO selectDashBoardListBrod(DashBoardInfo searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
			}
			searchVO.setPageSize(propertiesService.getInt("pageSize"));

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());

			int totCnt = dashboard.selectBrodStatusPage02Cnt();
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());
			searchVO.setOffCnt(String.valueOf(totCnt));

			List<DashBoardInfo> resultList = dashboard.selectBrodStatusPage02(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, resultList);
			resultMap.put("regist", searchVO);
			resultMap.put("paginationInfo", paginationInfo);
			resultMap.put("totalCnt", totCnt);
			resultMap.put("resultListBrod", dashboard.selectBrodStatus());
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDashBoardListBrod", e, egovMessageSource);
		}
		return resultVO;
	}
}
