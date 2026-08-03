package com.common.backoffice.sts.snd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import com.common.backoffice.bas.cnt.service.CenterInfoManageService;
import com.common.backoffice.sts.snd.modals.SendMsgInfoVO;
import com.common.backoffice.sts.snd.service.SendMsgInfoManageService;
import com.common.backoffice.sts.xml.service.XmlInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DID 발송 이력(메시지 전송 결과) 조회 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.snd.web.SendMsgInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_messagehistory, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반(HttpSession의 LoginVO, mberId/authorCode/groupId/parentGroupId 필드) → JWT(AuthHelper)
 *   기반으로 전환. 신규 LoginVO에는 parentGroupId 개념이 없어 그 조건은 항상 미적용으로 남음
 *   (author_Code→roleId, groupCode→partId로 매핑)
 * - JSP 팝업/화면 2개(pop_sendLst.do, sendResultList.do) → 리스트 API 1개로 통합(둘 다 동일한
 *   조회 로직이었고 팝업 쪽은 didId 필터만 추가로 걸던 차이였음)
 * - SendMsgInfoManagerMapper.xml이 postgresql 폴더에 있었지만 실제로는 Oracle 문법
 *   (ROWNUM/NVL/SYSDATE/시퀀스.NEXTVAL) 그대로였음 — 이번에 전체 Postgres 문법으로 변환함
 *   (MSG_SEQ는 DB 시퀀스 msg_seq로 채번 — db-encoding-fix/08_create_missing_sequences.sql로 신규 생성)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/operManage/snd")
@Tag(name = "SendMsgInfoManageController", description = "DID 발송 이력 조회")
public class SendMsgInfoManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final SendMsgInfoManageService sendMsgInfoManageService;
	private final CenterInfoManageService centerInfoManageService;
	private final XmlInfoManageService xmlService;

	@Operation(summary = "DID 발송 이력 리스트 조회", description = "didId를 지정하면 특정 DID의 발송 이력만 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/list.do")
	public ResultVO selectSendResult(@RequestBody SendMsgInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthor_Code(loginVO.getRoleId());
			searchVO.setGroupCode(loginVO.getPartId());

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

			if (searchVO.getCenterId() == null) searchVO.setCenterId("");
			if (searchVO.getXmlProcessName() == null) searchVO.setXmlProcessName("");
			if (searchVO.getSchStartDay() == null) searchVO.setSchStartDay("");

			List<SendMsgInfoVO> list = sendMsgInfoManageService.selectSendMsgInfoManageListByPagination(searchVO);
			int totCnt = sendMsgInfoManageService.selectSendMsgInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectSendResult", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 콤보(발송이력 검색용)")
	@GetMapping("/centerCombo.do")
	public ResultVO selectCenterCombo(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			CenterInfoVO centerInfoVO = new CenterInfoVO();
			centerInfoVO.setAuthorCode(loginVO.getRoleId());
			centerInfoVO.setMberId(loginVO.getManagerId());
			centerInfoVO.setSearchKeyword("");

			ResultHelper.setSuccess(resultVO, centerInfoManageService.selectCenterInfoManageCombo(centerInfoVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterCombo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "XML 명령어 콤보(발송이력 검색용)")
	@GetMapping("/processCombo.do")
	public ResultVO selectProcessCombo(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, xmlService.selectXmlProcessCombo(), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectProcessCombo", e, egovMessageSource);
		}
		return resultVO;
	}
}
