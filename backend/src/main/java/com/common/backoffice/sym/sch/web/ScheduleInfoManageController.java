package com.common.backoffice.sym.sch.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.cnt.modals.ContentInfoVO;
import com.common.backoffice.sts.cnt.service.ContentInfoManageService;
import com.common.backoffice.sym.grp.modals.GroupDidInfoVO;
import com.common.backoffice.sym.grp.modals.GroupInfoVO;
import com.common.backoffice.sym.grp.service.GroupDidInfoManageService;
import com.common.backoffice.sym.grp.service.GroupInfoManageService;
import com.common.backoffice.sym.sch.modals.ContentSendHistoryInfo;
import com.common.backoffice.sym.sch.modals.ScheduleInfoVO;
import com.common.backoffice.sym.sch.service.ContentSendHistoryInfoManagerService;
import com.common.backoffice.sym.sch.service.ScheduleInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * DID 발송 스케줄(방송 예약) 관리 API.
 * did_emart(REST + JWT + tb_schedule 등, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반(LoginVO in HttpSession) → JWT(AuthHelper) 기반 인증. 원본의
 *   mberId/authorCode/groupId/centerId는 loginVO.getManagerId()/getRoleId()/getPartId()/
 *   getCenterId()로 대체
 * - JSP 뷰 반환 → ResultVO(JSON) 반환
 * - 원본은 컴파일이 아예 안 되는 상태였음(참조하는 서비스/VO 클래스에 import 문 자체가 없었음
 *   — 같은 패키지가 아니라서 실제로는 컴파일 에러). 5개 서비스(ScheduleInfoManageService,
 *   ContentSendHistoryInfoManagerService, GroupDidInfoManageService, GroupInfoManageService,
 *   ContentInfoManageService)를 직접 확인한 결과 메서드 시그니처가 전부 일치하는 순수
 *   레거시 포트였음
 * - schUpdate.do(등록/수정)의 "등록 후 그룹 내 전체 DID에 발송이력(ContentSendHistoryInfo) 생성"
 *   로직은 그대로 포팅
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/equiManage/sch")
@Tag(name = "ScheduleInfoManageController", description = "DID 발송 스케줄 관리")
public class ScheduleInfoManageController {

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	private final ScheduleInfoManageService scheduleInfoManageService;
	private final ContentSendHistoryInfoManagerService sendHistoryService;
	private final GroupDidInfoManageService groupDidInfoManageService;
	private final GroupInfoManageService groupInfoManageService;
	private final ContentInfoManageService conManageService;

	@Operation(summary = "발송 스케줄 목록 조회")
	@PostMapping("/list.do")
	public ResultVO selectScheduleInfoManageListByPagination(@RequestBody ScheduleInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthorCode(loginVO.getRoleId());
			searchVO.setGroupId(loginVO.getPartId());
			searchVO.setCenterId(loginVO.getCenterId());

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

			List<ScheduleInfoVO> resultList = scheduleInfoManageService.selectScheduleInfoManageListByPagination(searchVO);
			int totCnt = scheduleInfoManageService.selectScheduleInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, resultList);
			resultMap.put("paginationInfo", paginationInfo);
			resultMap.put("totalCnt", totCnt);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectScheduleInfoManageListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "발송 스케줄 등록/수정 화면용 데이터 조회", description = "발송대상 그룹 콤보/콘텐츠 콤보와, 수정 모드일 때 기존 스케줄 상세를 함께 반환합니다.")
	@GetMapping("/formData.do")
	public ResultVO selectScheduleInfoManageDetail(@RequestParam(value = "groupSearchKeyword", required = false, defaultValue = "") String groupSearchKeyword,
													@RequestParam(value = "mode", required = false, defaultValue = "Ins") String mode,
													@RequestParam(value = "schCode", required = false, defaultValue = "") String schCode,
													HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			GroupInfoVO groupInfoVO = new GroupInfoVO();
			groupInfoVO.setAuthorCode(loginVO.getRoleId());
			groupInfoVO.setMberId(loginVO.getManagerId());
			groupInfoVO.setSearchKeyword(groupSearchKeyword);

			ContentInfoVO conInfoVO = new ContentInfoVO();
			conInfoVO.setAuthorCode(loginVO.getRoleId());
			conInfoVO.setMberId(loginVO.getManagerId());

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("selectGroup", groupInfoManageService.selectGroupInfoManageCombo(groupInfoVO));
			resultMap.put("selectContent", conManageService.selectNextCombo(conInfoVO));

			if ("Edt".equals(mode) && !schCode.isBlank()) {
				resultMap.put("regist", scheduleInfoManageService.selectScheduleInfoManageDetail(schCode));
			}

			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectScheduleInfoManageDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 검색 콤보 조회")
	@GetMapping("/contentSearch.do")
	public ResultVO selectScheduleInfoManageDetailSearch(@RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
														  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			ContentInfoVO conInfoVO = new ContentInfoVO();
			conInfoVO.setAuthorCode(loginVO.getRoleId());
			conInfoVO.setMberId(loginVO.getManagerId());
			conInfoVO.setSearchKeyword(searchKeyword);

			ResultHelper.setSuccess(resultVO, conManageService.selectSearcHCombo(conInfoVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectScheduleInfoManageDetailSearch", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "발송 스케줄 상세보기(뷰)")
	@GetMapping("/{schCode}/view.do")
	public ResultVO selectScheduleInfoManageView(@Parameter(description = "스케줄 코드") @PathVariable String schCode, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, scheduleInfoManageService.selectScheduleInfoManageDetailView(schCode), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectScheduleInfoManageView", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "발송 스케줄 단건 삭제", description = "삭제 후 해당 스케줄의 단말 발송이력도 함께 삭제합니다.")
	@DeleteMapping("/{schCode}.do")
	public ResultVO deleteScheduleInfoManage(@Parameter(description = "스케줄 코드") @PathVariable String schCode, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = scheduleInfoManageService.deleteScheduleInfoManage(schCode);
			if (ret > 0) {
				sendHistoryService.deleteContentSendHistoryInfoManage(schCode);
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteScheduleInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "발송 스케줄 일괄 삭제")
	@DeleteMapping("/deleteBulk.do")
	public ResultVO delSchedule(@RequestBody Map<String, String> params, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String schCode = params.getOrDefault("schCode", "");
			int ret = 0;
			if (!schCode.isBlank()) {
				for (String code : schCode.split(",")) {
					ret = scheduleInfoManageService.deleteScheduleInfoManage(code);
					if (ret > 0) {
						sendHistoryService.deleteContentSendHistoryInfoManage(code);
					}
				}
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "delSchedule", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "발송 스케줄 등록/수정", description = "등록/수정 후 해당 그룹에 속한 전체 DID에 발송이력(ContentSendHistoryInfo)을 생성합니다.")
	@PostMapping("/update.do")
	public ResultVO updateScheduleInfoManage(@RequestBody ScheduleInfoVO vo, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setAuthorCode(loginVO.getRoleId());
			vo.setGroupId(loginVO.getPartId());
			vo.setCenterId(loginVO.getCenterId());
			vo.setMberId(loginVO.getManagerId());

			boolean isInsert = "Ins".equals(vo.getMode());
			int ret = isInsert
					? scheduleInfoManageService.insertScheduleInfoManage(vo)
					: scheduleInfoManageService.updateScheduleInfoManage(vo);

			if (ret > 0) {
				if (isInsert) {
					vo.setSchCode(scheduleInfoManageService.selectScheduleMaxInfo());
				} else {
					sendHistoryService.deleteContentSendHistoryInfoManage(vo.getSchCode());
				}

				List<GroupDidInfoVO> resultLst = groupDidInfoManageService.selectGroupInfoManageListByPagination(vo.getGroupCode());
				for (GroupDidInfoVO didInfo : resultLst) {
					ContentSendHistoryInfo sendHistory = new ContentSendHistoryInfo();
					sendHistory.setDidId(didInfo.getDidId());
					sendHistory.setHisSeq("");
					sendHistory.setSchCode(vo.getSchCode());
					sendHistoryService.insertContentSendHistoryInfoManage(sendHistory);
				}

				String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
				resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
				resultVO.setResultMessage(egovMessageSource.getMessage(successMsgKey));
			} else {
				throw new Exception("Update failed");
			}
		} catch (Exception e) {
			log.error("updateScheduleInfoManage error: {}", e.getMessage());
			ResultHelper.setFailResult(resultVO, "updateScheduleInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}
}
