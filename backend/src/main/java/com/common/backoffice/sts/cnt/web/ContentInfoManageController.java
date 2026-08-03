package com.common.backoffice.sts.cnt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.cnt.modals.ContentInfo;
import com.common.backoffice.sts.cnt.modals.ContentInfoVO;
import com.common.backoffice.sts.cnt.service.ContentInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.fileMultiService;
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
import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

/**
 * 콘텐츠(단일 페이지) 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.cnt.web.ContentInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_content, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증
 * - 신규 등록 시 원본은 DB 시퀀스 content_seq를 사용했으나 해당 시퀀스가 없어
 *   ContentInfoManageService.generateConSeq()에서 애플리케이션 레벨로 대체 채번(TB_CONTENT/
 *   TB_CONTENTMUTIL이 CON_SEQ 채번 공간을 공유하므로 두 테이블 모두의 최대값 기준)
 * - ContentInfoManagerMapper.java에 ContentInfo/ContentInfoVO import가 누락되어 있어
 *   컴파일 자체가 안 되던 기존 버그를 함께 수정함(이번 작업과 무관하게 존재하던 문제)
 * - 파일 업로드를 UUID 직접 생성 방식에서 기존에 구축된 fileMultiService로 통일
 * - 참고: 현재 운영 DB 기준 tb_content는 0건(TB_CONTENTMUTIL이 실사용 중인 멀티페이지 콘텐츠
 *   테이블) — 그대로 포팅은 하되 별도 프론트 화면은 우선순위를 낮춤(문서화함)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/conManage/content")
@Tag(name = "ContentInfoManageController", description = "콘텐츠(단일 페이지) 관리")
public class ContentInfoManageController {

	@Value("${Common.filePath}")
	private String filePath;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ContentInfoManageService contentService;
	private final fileMultiService uploadFile;

	@Operation(summary = "콘텐츠 리스트 조회")
	@PostMapping("/list.do")
	public ResultVO selectContentlLst(@RequestBody ContentInfoVO searchVO, HttpServletRequest request) throws Exception {
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

			List<ContentInfoVO> list = contentService.selectContentInfoManageListByPagination(searchVO);
			int totCnt = contentService.selectContentInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectContentlLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 상세 조회")
	@GetMapping("/{conSeq}.do")
	public ResultVO selectConDetail(@Parameter(description = "콘텐츠 순번") @PathVariable("conSeq") String conSeq,
									 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			ContentInfoVO detail = contentService.selectContentInfoManageDetail(conSeq);
			if (detail == null) {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
				return resultVO;
			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT, detail);
			if ("PLAY02".equals(detail.getConPlayType())) {
				resultMap.put("nextCombo", contentService.selectNextCombo(detail));
			}
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectConDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "다음 콘텐츠 콤보 조회", description = "다음 재생 콘텐츠 지정(PLAY02)용 콤보 리스트를 반환합니다.")
	@GetMapping("/nextCombo.do")
	public ResultVO selectNextCombo(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			ContentInfoVO vo = new ContentInfoVO();
			vo.setAuthorCode(loginVO.getRoleId());
			vo.setMberId(loginVO.getManagerId());

			ResultHelper.setSuccess(resultVO, contentService.selectNextCombo(vo), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectNextCombo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/update.do")
	public ResultVO selectConUpdate(MultipartRequest mRequest, ContentInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			List<MultipartFile> files = mRequest.getFiles("conFile");
			if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
				vo.setConFile(uploadFile.uploadFileNm(files, filePath));
			}
			List<MultipartFile> thumbFiles = mRequest.getFiles("conThumbnail");
			if (thumbFiles != null && !thumbFiles.isEmpty() && !thumbFiles.get(0).isEmpty()) {
				vo.setConThumbnail(uploadFile.uploadFileNm(thumbFiles, filePath));
			}

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			int ret;
			if (isInsert) {
				vo.setFrstRegisterId(loginVO.getManagerId());
				ret = contentService.insertContentInfoManage(vo);
			} else {
				vo.setFrstRegisterId(loginVO.getManagerId());
				ret = contentService.updateContentInfoManage(vo);
			}

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectConUpdate", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 삭제")
	@DeleteMapping("/{conSeq}.do")
	public ResultVO selectConDelete(@Parameter(description = "콘텐츠 순번") @PathVariable("conSeq") String conSeq,
									 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (StringUtils.equals(conSeq, Globals.LOGIN_CONNECT_SYSTEM)) {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.delete.system"));
				return resultVO;
			}

			int ret = contentService.deleteContentInfoManage(conSeq);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectConDelete", e, egovMessageSource);
		}
		return resultVO;
	}
}
