package com.common.backoffice.uat.hri.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

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

import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.uat.hri.models.dto.ManagerInfoReqDto;
import com.common.backoffice.uat.hri.models.dto.ManagerInfoResDto;
import com.common.backoffice.uat.hri.service.ManagerInfoManagerService;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.PaginationHelper;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자(TB_MANAGERINFO) 관리 API.
 * basic/backend(com.common.backoffice.uat.hri.web.ManagerInfoManagerController)를 참조해서
 * did_emart 스키마 기준으로 등록/수정/삭제/idCheck/비밀번호 변경을 이식함
 * (리스트 조회는 2026-07-26에 이미 이식되어 있었음).
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

			PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
					propertiesService.getInt(Globals.PAGE_UNIT), propertiesService.getInt(Globals.PAGE_SIZE));
			List<Map<String, Object>> managerList = managerService.selectManagerManageListByPagination(searchVO);
			PaginationHelper.setResult(resultVO, managerList, paginationInfo, searchVO);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectUserManagerList error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 상세 정보", description = "성공시 상세 정보를 표출합니다.")
	@GetMapping("/{managerId}.do")
	public ResultVO selectManagerView(@Parameter(description = "관리자 아이디") @PathVariable String managerId,
									   HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			Optional<ManagerInfoResDto> managerVO = managerService.selectManagerManageDetail(managerId);
			if (managerVO.isPresent()) {
				ResultHelper.setSuccess(resultVO, managerVO, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.request.msg"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectManagerView error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 등록/수정", description = "성공시 관리자 정보를 등록/수정합니다.")
	@PostMapping("/managerUpdate.do")
	public ResultVO updateManger(@RequestBody ManagerInfoReqDto vo, HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setUserId(loginVO.getManagerId());

			if (Globals.SAVE_MODE_INSERT.equals(vo.getMode()) && !"Y".equals(vo.getIdCheck())) {
				resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.user.idcheck"));
				return resultVO;
			}

			int ret = managerService.updateManagerManage(vo);
			String messageKey = Globals.SAVE_MODE_INSERT.equals(vo.getMode())
					? (ret > 0 ? "sucess.common.insert" : "fail.common.insert")
					: (ret > 0 ? "sucess.common.update" : "fail.common.update");
			ResultHelper.setCudMsgResult(resultVO, ret, messageKey, egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "managerUpdate error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 삭제", description = "관리자 삭제 처리합니다.")
	@DeleteMapping("/{managerId}.do")
	public ResultVO deleteManger(@Parameter(description = "삭제할 관리자 아이디") @PathVariable("managerId") String managerId,
								  HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = managerService.deleteManagerMange(managerId);
			int returnCode = ret > 0 ? ResponseCode.SUCCESS.getCode() : ResponseCode.SAVE_ERROR.getCode();
			String status = ret > 0 ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = ret > 0 ? "success.request.msg" : "fail.request.msg";

			resultVO.setResultCode(returnCode);
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(egovMessageSource.getMessage(message));

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteManger error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 아이디 중복체크", description = "아이디 중복 여부를 확인합니다.")
	@GetMapping("/idCheck/{managerId}.do")
	public ResultVO selectUserMangerIDCheck(@Parameter(description = "체크할 아이디") @PathVariable("managerId") String managerId,
											 HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			int idCheck = managerService.selectManagerUserMangerIDCheck(managerId);
			String status = idCheck > 0 ? Globals.STATUS_FAIL : Globals.STATUS_SUCCESS;
			String message = idCheck > 0 ? "member.idcheck.fail" : "member.idcheck.success";

			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(egovMessageSource.getMessage(message));
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectUserMangerIDCheck error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 현재 비밀번호 확인", description = "비밀번호 변경 전 현재 비밀번호를 확인합니다.")
	@PostMapping("/passChangeCheck.do")
	public ResultVO updatePasswordCheck(@RequestBody Map<String, Object> searchVO, HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.put("managerId", loginVO.getManagerId());

			int ret = managerService.selectManagerPasswordCheck(searchVO);
			String status = ret > 0 ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = ret > 0 ? "비밀번호 확인 했습니다." : "현재 비밀번호가 아닙니다.";

			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(message);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updatePasswordCheck error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 비밀번호 변경", description = "비밀번호를 변경합니다.")
	@PostMapping("/passChange.do")
	public ResultVO updatePasswordChange(@RequestBody ManagerInfoReqDto vo, HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setManagerId(loginVO.getManagerId());
			vo.setUserId(loginVO.getManagerId());

			int ret = managerService.updatePassChange(vo);
			ResultHelper.setCudMsgResult(resultVO, ret, "info.user.passwordChange.ok", egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updatePasswordChange error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 상태 변경", description = "관리자 상태(재직/휴직/퇴사 등)를 변경합니다.")
	@GetMapping("/StateChange/{managerId}.do")
	public ResultVO updateStateChangeMessage(@RequestParam Map<String, Object> commandMap,
											  @Parameter(description = "관리자 아이디") @PathVariable("managerId") String managerId,
											  HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			ManagerInfoReqDto vo = new ManagerInfoReqDto();
			vo.setUserId(loginVO.getManagerId());
			vo.setManagerId(managerId);
			vo.setManagerStatus(UtilInfoService.NVLObj(commandMap.get("managerStatus"), ""));

			int ret = managerService.updagteManageState(vo);
			ResultHelper.setCudMsgResult(resultVO, ret, "success.common.msg", egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateStateChangeMessage error", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "관리자 사용유무 변경", description = "관리자 사용유무를 변경합니다.")
	@GetMapping("/useyn/{managerId}.do")
	public ResultVO updateUseYnChangeMessage(@RequestParam Map<String, Object> commandMap,
											  @Parameter(description = "관리자 아이디") @PathVariable("managerId") String managerId,
											  HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			ManagerInfoReqDto vo = new ManagerInfoReqDto();
			vo.setUserId(loginVO.getManagerId());
			vo.setManagerId(managerId);
			vo.setUseYn(UtilInfoService.NVLObj(commandMap.get("useYn"), "N"));

			int ret = managerService.updateUseYn(vo);
			ResultHelper.setCudMsgResult(resultVO, ret, "success.common.msg", egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateUseYnChangeMessage error", e, egovMessageSource);
		}
		return resultVO;
	}
}
