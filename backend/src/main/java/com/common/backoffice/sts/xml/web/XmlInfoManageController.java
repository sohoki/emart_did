package com.common.backoffice.sts.xml.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.code.service.EgovCcmCmmnDetailCodeManageService;
import com.common.backoffice.sts.xml.modals.XmlInfo;
import com.common.backoffice.sts.xml.modals.XmlInfoVO;
import com.common.backoffice.sts.xml.service.XmlInfoManageService;
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
 * XML(장비 통신 명령) 정의 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.xml.web.XmlInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_sendmessagetypr, postgresql) 기준으로 리팩토링함.
 *
 * ⚠️ 범위 안내: 원본 컨트롤러(2,355줄)는 두 가지 서로 다른 기능이 섞여 있었음.
 * 1) "XML 정보" 관리 CRUD(본 컨트롤러가 다루는 부분, ~350줄) — 관리자 화면에서 XML/JSON 명령
 *    정의를 등록/조회/삭제하는 일반적인 관리 기능
 * 2) DID 장비 통신 프로토콜 처리기(jsonAuth.do/xmlAuth.do, ~2,000줄, SP_DIDAUTH/SP_BRODSTATE 등
 *    40개+ 명령어 분기) — 매장에 설치된 DID 장비가 서버로 호출하는 기계 간 통신 API이며
 *    DidInfoManageService/BrodScheduleManagerService/MhsMonitorInfoManageService 등
 *    아직 포팅되지 않은 15개+ 서비스에 의존함. 관리자 화면이 필요 없는 영역이라 이번 작업
 *    범위에서 제외했고(사용자 확인받음), 별도 작업으로 진행 필요.
 *
 * 원본 대비 변경된 부분(1번 CRUD):
 * - 세션 기반 → JWT(AuthHelper) 기반 인증
 * - JSP 뷰 반환 → ResultVO(JSON) 반환
 * - 신규 등록 시 XML_SEQ는 DB 시퀀스 xml_seq로 채번함(db-encoding-fix/
 *   08_create_missing_sequences.sql로 신규 생성 — 마이그레이션 과정에서 누락되어 있었음)
 * - jsonDoc/xmlDocument(JSON/XML 미리보기 생성)는 그대로 포팅해서 /preview 엔드포인트로 제공
 * - xmlAuthReq.do/jsonAuthReq.do/serverDate.do(JSP 뷰 렌더링용 라우트)는 REST 구조에서는
 *   불필요해서 제외함 — 프론트에서 /preview 응답을 그대로 표시하면 됨
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/operManage/xml")
@Tag(name = "XmlInfoManageController", description = "XML(장비 통신 명령) 정의 관리")
public class XmlInfoManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final XmlInfoManageService xmlInfoManageService;
	private final EgovCcmCmmnDetailCodeManageService cmmnDetailCodeManageService;

	@Operation(summary = "XML 정보 리스트 조회")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/list.do")
	public ResultVO selectXmlLst(@RequestBody XmlInfoVO searchVO, HttpServletRequest request) throws Exception {
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

			List<XmlInfoVO> list = xmlInfoManageService.selectXmlInfoManageListByPagination(searchVO);
			int totCnt = xmlInfoManageService.selectXmlInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectXmlLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "업무구분 콤보(EMT006 공통코드)")
	@GetMapping("/workGubunCombo.do")
	public ResultVO selectWorkGubunCombo(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT006"), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectWorkGubunCombo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "XML 정보 상세 조회")
	@GetMapping("/{xmlSeq}.do")
	public ResultVO selectDetailXml(@Parameter(description = "XML 일련번호") @PathVariable("xmlSeq") String xmlSeq,
									 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			XmlInfoVO detail = xmlInfoManageService.selectXmlrInfoManageDetail(xmlSeq);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDetailXml", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "XML 명령명 중복 체크")
	@GetMapping("/processCheck.do")
	public ResultVO selectProcessCheck(@RequestParam(value = "xmlProcessName", required = false, defaultValue = "") String xmlProcessName,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int cnt = xmlInfoManageService.selectXmlProcessCount(xmlProcessName);
			ResultHelper.setSuccess(resultVO, cnt, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectProcessCheck", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "XML 정보 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/update.do")
	public ResultVO updateXml(@RequestBody XmlInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setFrstRegisterId(loginVO.getManagerId());
			vo.setLastRegisterId(loginVO.getManagerId());

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			int ret = isInsert
					? xmlInfoManageService.insertXmlInfoManage(vo)
					: xmlInfoManageService.updateXmlInfoManage(vo);

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateXml", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "XML 정보 삭제")
	@DeleteMapping("/{xmlSeq}.do")
	public ResultVO deleteXmlInfoManage(@Parameter(description = "XML 일련번호") @PathVariable("xmlSeq") String xmlSeq,
										 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = xmlInfoManageService.deleteXmlInfoManage(xmlSeq);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteXmlInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "JSON 미리보기", description = "등록된 XML 정보로 테스트용 JSON 문서를 생성합니다.")
	@GetMapping("/preview/json/{xmlSeq}.do")
	public ResultVO selectJsonPreview(@Parameter(description = "XML 일련번호") @PathVariable("xmlSeq") String xmlSeq,
									   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			XmlInfoVO info = xmlInfoManageService.selectXmlrInfoManageDetail(xmlSeq);
			ResultHelper.setSuccess(resultVO, jsonDoc(info), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectJsonPreview", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "XML 미리보기", description = "등록된 XML 정보로 테스트용 XML 문서를 생성합니다.")
	@GetMapping("/preview/xml/{xmlSeq}.do")
	public ResultVO selectXmlPreview(@Parameter(description = "XML 일련번호") @PathVariable("xmlSeq") String xmlSeq,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			XmlInfoVO info = xmlInfoManageService.selectXmlrInfoManageDetail(xmlSeq);
			ResultHelper.setSuccess(resultVO, xmlDocument(info), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectXmlPreview", e, egovMessageSource);
		}
		return resultVO;
	}

	// jsonDoc/xmlDocument: 원본 로직 그대로 포팅(입력 파라미터 CSV를 name/sample 매핑해서 문서 생성)
	private String jsonDoc(XmlInfo vo) {
		Map<String, Object> obj = new HashMap<>();
		obj.put("command_type", vo.getXmlProcessName());
		try {
			String[] inputParamArrays = vo.getXmlInputParam().split(",");
			String[] inputParamSampleArrays = vo.getXmlInputParamSample().split(",");

			Map<String, Object> sObject = new HashMap<>();
			for (int i = 0; i < inputParamArrays.length; i++) {
				sObject.put(inputParamArrays[i].trim(), inputParamSampleArrays[i].trim());
			}
			obj.put("command_data", List.of(sObject));
		} catch (Exception e) {
			log.debug("jsonDoc error: {}", e.toString());
		}
		return toJsonString(obj);
	}

	private String toJsonString(Map<String, Object> obj) {
		// 별도 JSON 라이브러리 의존 없이 간단히 조립(값에 큰따옴표가 없다는 기존 전제를 그대로 따름)
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"command_type\":\"").append(obj.get("command_type")).append("\"");
		Object data = obj.get("command_data");
		if (data instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
			sb.append(",\"command_data\":[{");
			boolean first = true;
			for (Map.Entry<?, ?> e : map.entrySet()) {
				if (!first) sb.append(",");
				sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
				first = false;
			}
			sb.append("}]");
		}
		sb.append("}");
		return sb.toString();
	}

	private String xmlDocument(XmlInfo vo) {
		String[] inputParamArrays = vo.getXmlInputParam().split(",");
		String[] inputParamSampleArrays = vo.getXmlInputParamSample().split(",");

		StringBuilder xmlDoc = new StringBuilder();
		xmlDoc.append("<?xml version='1.0' encoding='UTF-8'?>\r\n");
		xmlDoc.append("<CM_Document protocol = 'CMXML' version = '1.0'>\r\n");
		xmlDoc.append("   <command>\r\n ");
		xmlDoc.append("       <command_type>").append(vo.getXmlProcessName()).append("</command_type>\r\n");
		xmlDoc.append("       <command_data>\r\n");
		for (int i = 0; i < inputParamArrays.length; i++) {
			String name = inputParamArrays[i].trim();
			xmlDoc.append("            <").append(name).append(">")
					.append(inputParamSampleArrays[i].trim())
					.append("</").append(name).append(">\r\n");
		}
		xmlDoc.append("       </command_data>\r\n");
		xmlDoc.append("   </command>\r\n ");
		xmlDoc.append("</CM_Document>\r\n ");
		return xmlDoc.toString();
	}
}
