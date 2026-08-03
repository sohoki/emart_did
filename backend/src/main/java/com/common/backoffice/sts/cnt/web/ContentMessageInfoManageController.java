package com.common.backoffice.sts.cnt.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.cnt.modals.ContentMessageInfo;
import com.common.backoffice.sts.cnt.modals.ContentMessageInfoVO;
import com.common.backoffice.sts.cnt.service.ContentMessageInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.EgovMessageSource;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * DID 발송 메시지(자막) 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.cnt.web.ContentMessageInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_didsendmessage, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 축소/변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증
 * - 신규 등록 시 원본은 DB 함수 FN_DIDMESSAGEINFO(didId)로 SEND_DIDID를, EgovIdGnrService
 *   (egovMsgIdGnrService, did_emart에 미구성)로 SEND_MSGID를 채번했으나 둘 다 없어
 *   ContentMessageInfoManageService.generateSendDidId()/generateSendMsgId()로 애플리케이션
 *   레벨 대체 채번
 * - didSendMessage.do(조직별 DID 그룹 브라우징 후 대상 선택 화면)는 제외함 — 이 화면이 의존하는
 *   GroupInfoManageService.selectGroupInfoManageListByPagination()이 Map&lt;String,Object&gt;
 *   파라미터에 "params.roleId" 같은 중첩 키 구조를 기대하는 등 계약이 불명확해서, 잘못 짐작해서
 *   포팅하면 컴파일은 되지만 런타임에 조용히 깨질 위험이 있음. 대상 DID 선택은 이미 존재하는
 *   DID 관리 화면의 목록/검색 기능을 그대로 재사용하는 편이 안전해서 이번 범위에서 제외(후속 필요 시
 *   GroupInfoManageService 계약을 먼저 명확히 확인하고 별도 작업으로 진행 권장)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/equiManage/message")
@Tag(name = "ContentMessageInfoManageController", description = "DID 발송 메시지(자막) 관리")
public class ContentMessageInfoManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ContentMessageInfoManageService messageService;

	@Operation(summary = "DID 발송 메시지 리스트 조회")
	@PostMapping("/list.do")
	public ResultVO selectContentMessageInfoListByPagination(@RequestBody ContentMessageInfoVO searchVO,
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

			List<ContentMessageInfoVO> list = messageService.selectContentMessageInfoListByPagination(searchVO);
			int totCnt = messageService.selectContentMessageInfoListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectContentMessageInfoListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 발송 메시지 상세 조회")
	@GetMapping("/{sendMsgId}.do")
	public ResultVO selectContentMessageInfoDetail(@Parameter(description = "메시지 ID") @PathVariable("sendMsgId") String sendMsgId,
													 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			ContentMessageInfo detail = messageService.selectContentMessageInfoDetail(sendMsgId);
			if (detail == null) {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
				return resultVO;
			}
			if (detail.getSendMessageStartTime() != null && detail.getSendMessageStartTime().contains(":")) {
				String[] startArray = detail.getSendMessageStartTime().split(":");
				detail.setSendMessageStartHour(startArray[0]);
				detail.setSendMessageStartMin(startArray[1]);
			}
			if (detail.getSendMessageEndTime() != null && detail.getSendMessageEndTime().contains(":")) {
				String[] endArray = detail.getSendMessageEndTime().split(":");
				detail.setSendMessageEndHour(endArray[0]);
				detail.setSendMessageEndMin(endArray[1]);
			}
			ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectContentMessageInfoDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 발송 메시지 등록/수정",
			description = "mode=Ins면 didIds(다건)에 대해 각각 신규 등록, 그 외에는 sendMsgId 기준 단건 수정합니다.")
	@PostMapping("/update.do")
	public ResultVO insertSendMessage(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			ContentMessageInfo vo = new ContentMessageInfo();
			vo.setMode(String.valueOf(body.get("mode")));
			vo.setGroupCode(String.valueOf(body.getOrDefault("groupCode", "")));
			vo.setSendMessage(String.valueOf(body.getOrDefault("sendMessage", "")));
			vo.setSendMessageStartDay(String.valueOf(body.getOrDefault("sendMessageStartDay", "")));
			vo.setSendMessageEndDay(String.valueOf(body.getOrDefault("sendMessageEndDay", "")));
			vo.setSendFontType(String.valueOf(body.getOrDefault("sendFontType", "")));
			vo.setSendUseYn(String.valueOf(body.getOrDefault("sendUseYn", "Y")));
			vo.setSendMessageStartTime(body.get("sendMessageStartHour") + ":" + body.get("sendMessageStartMin"));
			vo.setSendMessageEndTime(body.get("sendMessageEndHour") + ":" + body.get("sendMessageEndMin"));

			int ret;
			if (Globals.SAVE_MODE_INSERT.equals(vo.getMode())) {
				vo.setSendMsgId(messageService.generateSendMsgId());
				@SuppressWarnings("unchecked")
				List<String> didIds = (List<String>) body.getOrDefault("didIds", new ArrayList<>());
				ret = 0;
				for (String didId : didIds) {
					vo.setDidId(didId);
					vo.setSendDidId(messageService.generateSendDidId(didId));
					ret = messageService.insertContentMessageInfo(vo);
				}
			} else {
				vo.setSendMsgId(String.valueOf(body.get("sendMsgId")));
				ret = messageService.updateContentMessageInfoMsgId(vo);
			}

			String successMsgKey = Globals.SAVE_MODE_INSERT.equals(vo.getMode()) ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "insertSendMessage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 발송 메시지 일괄 삭제", description = "sendDidIds에 담긴 발송건들을 삭제합니다.")
	@PostMapping("/deleteBulk.do")
	public ResultVO deleteDIDMessageInfo(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			@SuppressWarnings("unchecked")
			List<String> sendDidIds = (List<String>) body.getOrDefault("sendDidIds", new ArrayList<>());
			int ret = 0;
			for (String sendDidId : sendDidIds) {
				try {
					ret = messageService.deleteContentMessageInfo(sendDidId.trim());
				} catch (Exception e) {
					log.debug("deleteDIDMessageInfo error: {}", e.toString());
				}
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteDIDMessageInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 메시지 삭제(메시지ID 기준)")
	@DeleteMapping("/{sendMsgId}.do")
	public ResultVO deleteContentMessageInfoMsgId(@Parameter(description = "메시지 ID") @PathVariable("sendMsgId") String sendMsgId,
												   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = messageService.deleteContentMessageInfoMsgId(sendMsgId);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteContentMessageInfoMsgId", e, egovMessageSource);
		}
		return resultVO;
	}
}
