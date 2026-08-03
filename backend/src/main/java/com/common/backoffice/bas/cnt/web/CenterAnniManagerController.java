package com.common.backoffice.bas.cnt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.modals.CenterInfoAnniversary;
import com.common.backoffice.bas.cnt.modals.CenterInfoAnniversaryVO;
import com.common.backoffice.bas.cnt.service.CenterAnniManagerService;
import com.common.backoffice.sts.brd.modals.BrodContentInfoVO;
import com.common.backoffice.sts.brd.service.BrodContentInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매장(센터) 기념일 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sym.cnt.web.CenterAnniManagerController를
 * 참조해서 did_emart(REST + JWT + tb_centeranniversary, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 → JWT(AuthHelper) 기반 인증
 * - JSP/팝업 뷰 반환 → ResultVO(JSON) 반환
 * - 신규 등록 시 채번은 CenterAnniManagerService.generateCenterAnniday()에서 애플리케이션
 *   레벨로 처리함. 2026-07-27 DB 함수 FN_CENTERANNICODE() 생성이 확인됐으나, 실제 정의에
 *   매개변수 대신 특정 매장코드가 하드코딩된 버그가 있어(DB_FUNCTIONS_VERIFICATION.md 참고)
 *   전환하지 않고 기존 방식 유지
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/basicManage/cnt/anni")
@Tag(name = "CenterAnniManagerController", description = "매장(센터) 기념일 관리")
public class CenterAnniManagerController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final CenterAnniManagerService centerAnniService;
	private final BrodContentInfoManageService brodContentInfo;

	@Operation(summary = "매장 기념일 리스트 조회", description = "특정 매장(centerId)의 기념일 리스트를 반환합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/list.do")
	public ResultVO selectCenterAnniLst(@RequestBody CenterInfoAnniversaryVO searchVO,
										 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt(Globals.PAGE_UNIT));
			}
			searchVO.setPageSize(propertiesService.getInt(Globals.PAGE_SIZE));

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			List<CenterInfoAnniversaryVO> list = centerAnniService.selectCenterAnniManageListByPagination(searchVO);
			int totCnt = centerAnniService.selectCenterAnniManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterAnniLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 기념일 상세 조회")
	@GetMapping("/{centerAnniday}.do")
	public ResultVO selectCenterAnni(@Parameter(description = "매장 기념일 ID") @PathVariable("centerAnniday") String centerAnniday,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			CenterInfoAnniversaryVO detail = centerAnniService.selectCenterAnniManageDetail(centerAnniday);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterAnni", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송콘텐츠 콤보(기념일 등록용)", description = "기념일에 매핑할 방송콘텐츠 콤보 리스트를 반환합니다.")
	@GetMapping("/brodCombo.do")
	public ResultVO selectBrodComboForAnni(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, brodContentInfo.selectBrodContentComboAnn(), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodComboForAnni", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송콘텐츠 기간 조회", description = "brodCode의 시작일/종료일을 조회합니다.")
	@GetMapping("/brodDayInfo.do")
	public ResultVO selectAnnDayInfo(@RequestParam(value = "brodCode", required = false, defaultValue = "") String brodCode,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			BrodContentInfoVO brodInfo = brodContentInfo.selectBrodContentInfo(brodCode);
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("brodStartDay", brodInfo != null ? brodInfo.getBrodStartDay() : "");
			resultMap.put("brodEndDay", brodInfo != null ? brodInfo.getBrodEndDay() : "");
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectAnnDayInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "기념일 기간 중복 체크", description = "등록하려는 기간이 기존 기념일과 겹치는지 건수를 반환합니다.")
	@PostMapping("/cntCheck.do")
	public ResultVO selectAnnCnt(@RequestBody CenterInfoAnniversaryVO searchVO,
								  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int cnt = centerAnniService.selectCenterAnniRetgCheck(searchVO);
			ResultHelper.setSuccess(resultVO, cnt, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectAnnCnt", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 기념일 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/update.do")
	public ResultVO updateCenterAnni(@RequestBody CenterInfoAnniversary vo,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setFrstRegisterId(loginVO.getManagerId());
			vo.setLastUpdusrId(loginVO.getManagerId());

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			int ret = isInsert
					? centerAnniService.insertCenterAnniManage(vo)
					: centerAnniService.updateCenterAnniManage(vo);

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateCenterAnni", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 기념일 삭제")
	@DeleteMapping("/{centerAnniday}.do")
	public ResultVO deleteCenterAnniversary(@Parameter(description = "매장 기념일 ID") @PathVariable("centerAnniday") String centerAnniday,
											 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = centerAnniService.deleteCenterAnniManage(centerAnniday);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteCenterAnniversary", e, egovMessageSource);
		}
		return resultVO;
	}
}
