package com.common.backoffice.use.web;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.backoffice.use.modals.Group;
import com.common.backoffice.use.modals.GroupVo;
import com.common.backoffice.use.service.GroupManagerService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 부서(GROUP, LETTNAUTHORGROUPINFO) 관리 API. 레거시 화면명은 "부서관리"
 * (emart_cms3.2.1의 /backoffice/sub/basicManage/selectGroupLst.do).
 * 기존 컨트롤러가 javax.* 패키지(Jakarta EE 이관 전 상태)와 @ModelAttribute 세션 바인딩을
 * 그대로 쓰고 있어 컴파일이 안 되던 것을 REST/JWT 기준으로 재작성함. Service/Mapper는
 * 이미 정상 포팅되어 있어 그대로 사용.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/sub/basicManage/group")
@Tag(name = "GroupManagerController", description = "부서(Group) 관리 API")
public class GroupManagerController {

	private final GroupManagerService groupManagerService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Operation(summary = "부서 목록 조회", description = "성공시 부서 목록(계층형)을 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/list.do")
	public ResultVO selectUserGroupManageListByPagination(@RequestBody GroupVo searchVO,
															HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("resultList", groupManagerService.selectUserGroupManageListByPagination(searchVO));
			map.put("totalCnt", groupManagerService.selectGroupManageListTotCnt_S(searchVO));
			ResultHelper.setSuccess(resultVO, map);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectUserGroupManageListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "부서 콤보박스", description = "성공시 전체 부서 목록을 콤보용으로 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/combo.do")
	public ResultVO selectGroupManageCombo(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			// selectGroupManageCombo(계층 제한 쿼리)는 "관리자 등록/부서 선택"처럼 전체 부서가
			// 필요한 화면에는 맞지 않아(부모부서 기준으로 좁혀짐) 목록 조회와 동일하게 전체를 내려준다.
			ResultHelper.setSuccess(resultVO, groupManagerService.selectUserGroupManageListByPagination(new GroupVo()), Globals.JSON_RETURN_RESULT_LIST);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectGroupManageCombo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "부서 상세 조회", description = "성공시 부서 상세 정보와 상위부서 콤보를 함께 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/{groupId}.do")
	public ResultVO getGroupManage(@Parameter(description = "부서 아이디") @PathVariable("groupId") String groupId,
									HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			GroupVo groupVo = new GroupVo();
			groupVo.setParentGroupId(loginVO.getPartId());
			groupVo.setGroupId(groupId);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("selectGroup", groupManagerService.selectGroupManageCombo(groupVo));
			map.put("regist", groupManagerService.selectGroupManageDetail(groupId));
			ResultHelper.setSuccess(resultVO, map);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "getGroupManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "부서 등록/수정", description = "성공시 부서 정보를 등록/수정합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/update.do")
	public ResultVO insertGroupManage(@RequestBody Group vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			if (isInsert && (vo.getGroupId() == null || vo.getGroupId().isBlank())) {
				vo.setGroupId(groupManagerService.generateGroupId());
			}

			int ret = isInsert ? groupManagerService.insertGroupManage(vo) : groupManagerService.updateGroupManage(vo);
			String messageKey = isInsert ? "sucess.common.insert" : "sucess.common.update";
			ResultHelper.setCudMsgResult(resultVO, ret, messageKey, egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "insertGroupManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "부서 삭제", description = "성공시 부서 정보를 삭제합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("/{groupId}.do")
	public ResultVO deleteGroupManage(@Parameter(description = "부서 아이디") @PathVariable("groupId") String groupId,
									   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = groupManagerService.deleteGroupManage(groupId);
			int res = ret > 0 ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
			String status = ret > 0 ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = ret > 0 ? egovMessageSource.getMessage("success.common.delete") : egovMessageSource.getMessage("fail.request.msg");

			resultVO.setResultCode(res);
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(message);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteGroupManage", e, egovMessageSource);
		}
		return resultVO;
	}
}
