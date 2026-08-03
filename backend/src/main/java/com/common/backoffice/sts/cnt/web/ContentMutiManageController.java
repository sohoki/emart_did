package com.common.backoffice.sts.cnt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.code.modals.dto.CmmnDetailCodeDto;
import com.common.backoffice.bas.code.service.EgovCcmCmmnDetailCodeManageService;
import com.common.backoffice.sts.cnt.modals.ContentDetailInfo;
import com.common.backoffice.sts.cnt.modals.ContentMutiInfoVO;
import com.common.backoffice.sts.cnt.service.ContentDetailInfoService;
import com.common.backoffice.sts.cnt.service.ContentFileInfoManageService;
import com.common.backoffice.sts.cnt.service.ContentMutimanageService;
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
import org.apache.commons.lang3.StringUtils;
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
 * 멀티페이지 콘텐츠(문화센터 외 일반 DID 콘텐츠) 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.cnt.web.ContentMutiManageController를
 * 참조해서 did_emart(REST + JWT + tb_contentmutil, postgresql) 기준으로 리팩토링함.
 *
 * 원본(1645줄) 대비 축소/변경된 부분:
 * - 원본은 관리자 CRUD(목록/상세/등록/수정/삭제/페이지 구성)와 DID 장비용 정적 HTML 렌더링·
 *   전송 로직(contentPreview.do, contentScheduleSend.do, MainPageView/ContentFileCreate*
 *   /conPageDetail/conTentPage/conTentPageFile — 약 1120줄, 파일의 2/3 이상)이 한 컨트롤러에
 *   섞여 있었음. XmlInfoManageController 때와 동일한 기준으로 이번엔 관리자 CRUD만 포팅하고
 *   장비용 HTML 렌더링·전송 클러스터는 제외함(후속 작업 필요, 별도로 다룰 때 이 파일의 라인
 *   536~1645를 참고)
 * - 신규 등록 시 원본은 DB 함수 FN_SEQMAX('TB_CONTENTMUTIL','CON_SEQ')로 CON_SEQ를,
 *   DB 시퀀스 condetail_seq로 DETAIL_SEQ를 채번했으나 둘 다 없어 ContentMutimanageService.
 *   generateConSeq()/ContentDetailInfoService.generateDetailSeq()로 애플리케이션 레벨 대체 채번
 *   (TB_CONTENT/TB_CONTENTMUTIL이 CON_SEQ 채번 공간을 공유하므로 두 테이블 모두 확인)
 * - filePageUpdate(2페이지 고정 레이아웃 저장)에서 원본은 conTentPage()(장비용 HTML
 *   프리렌더링, 제외 대상)를 호출해 결과를 CON_REMARK에 캐싱했으나, 이번 범위에서는 페이지
 *   순서/내용만 저장하고 HTML 프리렌더링 캐싱은 하지 않음(후속 장비 렌더링 모듈 작업 시 함께 처리)
 * - conMutiView.do/conMutiView_back.do(동일 데이터의 JSP 뷰 2종)는 `GET /{conSeq}.do` 하나로 통합
 * - contentJsonTest.do는 원본에서도 주석 처리된 죽은 코드라 제외
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/conManage/muti")
@Tag(name = "ContentMutiManageController", description = "멀티페이지 콘텐츠 관리")
public class ContentMutiManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ContentMutimanageService contentMuti;
	private final ContentFileInfoManageService conFileService;
	private final ContentDetailInfoService contentDetail;
	private final EgovCcmCmmnDetailCodeManageService cmmnDetailCodeManageService;

	@Operation(summary = "멀티페이지 콘텐츠 리스트 조회")
	@PostMapping("/list.do")
	public ResultVO selectContentMutilLst(@RequestBody ContentMutiInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthorCode(loginVO.getRoleId());
			searchVO.setGroupId(loginVO.getPartId());
			searchVO.setCenterId(loginVO.getCenterId());

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

			List<ContentMutiInfoVO> list = contentMuti.selectContentMutiInfoManageListByPagination(searchVO);
			int totCnt = contentMuti.selectContentMutiInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectContentMutilLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "멀티페이지 콘텐츠 상세(뷰) 조회")
	@GetMapping("/{conSeq}.do")
	public ResultVO selectConmultiView(@Parameter(description = "콘텐츠 순번") @PathVariable("conSeq") String conSeq,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ContentMutiInfoVO detail = contentMuti.selectContentMutiInfoManageView(conSeq);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectConmultiView", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 등록/수정 화면용 콤보/상세 조회",
			description = "콘텐츠 유형/화면분할/재생방식/URL방식 콤보와 다음콘텐츠 콤보, (수정 모드일 때) 상세 데이터를 함께 반환합니다.")
	@GetMapping("/formData.do")
	public ResultVO selectFormData(@RequestParam(value = "conSeq", required = false, defaultValue = "") String conSeq,
									@RequestParam(value = "mode", required = false, defaultValue = "Ins") String mode,
									HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("selectConType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT001"));
			resultMap.put("selectScreenType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT010"));
			resultMap.put("selectPlayType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT015"));
			resultMap.put("selectUrlType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT017"));

			ContentMutiInfoVO vo;
			if (!"Ins".equals(mode) && !conSeq.isBlank()) {
				resultMap.put("selectNextSeq", contentMuti.selectNextSeqList(conSeq));
				vo = contentMuti.selectContentMutiInfoManageDetail(conSeq);
				vo.setMode("Edt");
			} else {
				vo = new ContentMutiInfoVO();
				vo.setConWidth("1080");
				vo.setConHeight("1980");
				vo.setConMid("540");
				vo.setConTime("0");
				vo.setMode("Ins");
			}
			resultMap.put("regist", vo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectFormData", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 상세페이지 콤보 조회", description = "콘텐츠 유형 공통코드(code)의 페이지 수(codeDc)와 실제 상세페이지 행 수가 다르면 상세페이지 행을 재구성한 뒤 콤보를 반환합니다.")
	@GetMapping("/detailCombo.do")
	public ResultVO selectComboView(@RequestParam("code") String code, @RequestParam("conSeq") String conSeq,
									 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			CmmnDetailCodeDto cmDetail = cmmnDetailCodeManageService.selectCmmnDetail(code);
			String pageCnt = (cmDetail == null || cmDetail.getCodeDc() == null || cmDetail.getCodeDc().isBlank())
					? "1" : cmDetail.getCodeDc();

			int pageConDetailCnt = contentDetail.selectConDetailCnt(conSeq);
			if (Integer.parseInt(pageCnt) != pageConDetailCnt) {
				createDetailTable(conSeq, pageCnt);
			}

			ResultHelper.setSuccess(resultVO, contentDetail.selectConDetailCombo(conSeq), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectComboView", e, egovMessageSource);
		}
		return resultVO;
	}

	/** 상세페이지 행을 conSeq 기준으로 전부 지우고 pageCnt만큼 새로 생성한다(기존 페이지별 설정은 초기화됨). */
	private void createDetailTable(String conSeq, String pageCnt) throws Exception {
		contentDetail.deleteContentDetailConSeq(conSeq);
		for (int i = 0; i < Integer.parseInt(pageCnt); i++) {
			ContentDetailInfo vo = new ContentDetailInfo();
			vo.setConSeq(conSeq);
			vo.setDetailOrder(Integer.toString(i));
			contentDetail.insertContentDetailManage(vo);
		}
	}

	@Operation(summary = "고정 2단 레이아웃 - 1페이지 조회/생성", description = "DETAIL_ORDER=1인 상세페이지가 없으면 새로 만들고, 있으면 그대로 반환합니다.")
	@PostMapping("/page01.do")
	public ResultVO filePage01(@RequestBody ContentDetailInfo vo, HttpServletRequest request) throws Exception {
		return fetchOrCreateFixedPage(vo, "1", "Page01");
	}

	@Operation(summary = "고정 2단 레이아웃 - 2페이지 조회/생성", description = "DETAIL_ORDER=2인 상세페이지가 없으면 새로 만들고, 있으면 그대로 반환합니다.")
	@PostMapping("/page02.do")
	public ResultVO filePage02(@RequestBody ContentDetailInfo vo, HttpServletRequest request) throws Exception {
		return fetchOrCreateFixedPage(vo, "2", "Page02");
	}

	private ResultVO fetchOrCreateFixedPage(ContentDetailInfo vo, String detailOrder, String pageGubun) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int cnt = "1".equals(detailOrder)
					? contentDetail.selectPageSeqCheckPage01Cnt(vo.getConSeq())
					: contentDetail.selectPageSeqCheckPage02Cnt(vo.getConSeq());

			ContentDetailInfo detail;
			if (cnt == 0) {
				vo.setDetailOrder(detailOrder);
				int ret = contentDetail.insertContentDetailManage(vo);
				if (ret <= 0) {
					resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
					resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
					resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.insert"));
					return resultVO;
				}
				detail = contentDetail.selectContentDetail(vo.getDetailSeq());
			} else {
				int existingSeq = "1".equals(detailOrder)
						? contentDetail.selectPageSeqCheckPage01(vo.getConSeq())
						: contentDetail.selectPageSeqCheckPage02(vo.getConSeq());
				detail = contentDetail.selectContentDetail(String.valueOf(existingSeq));
			}
			detail.setPageGubun(pageGubun);
			ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "fetchOrCreateFixedPage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "고정 2단 레이아웃 페이지 저장", description = "pageGubun(Page01/Page02)에 따라 DETAIL_ORDER를 1 또는 2로 저장합니다.")
	@PostMapping("/pageUpdate.do")
	public ResultVO filePageUpdate(@RequestBody ContentDetailInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			vo.setDetailOrder("Page01".equals(vo.getPageGubun()) ? "1" : "2");
			int ret = contentDetail.updateContentDetailManage(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "filePageUpdate", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "멀티페이지 콘텐츠 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/update.do")
	public ResultVO selectConMutiUpdate(@RequestBody ContentMutiInfoVO vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setGroupId(loginVO.getPartId());
			vo.setCenterId(loginVO.getCenterId());
			vo.setMberId(loginVO.getManagerId());

			if (vo.getConNextSeq() == null || vo.getConNextSeq().isBlank()) {
				vo.setConNextSeq("0");
			}

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			int ret;
			if (isInsert) {
				vo.setFrstRegisterId(loginVO.getManagerId());
				ret = contentMuti.insertContentMutiInfoManage(vo);
			} else {
				vo.setLastRegisterId(loginVO.getManagerId());
				ret = contentMuti.updateContentMutiInfoManage(vo);
			}

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectConMutiUpdate", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "멀티페이지 콘텐츠 삭제", description = "연결된 파일/상세페이지도 함께 삭제합니다.")
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

			int ret = contentMuti.deleteContentMutiInfoManage(conSeq);
			if (ret > 0) {
				conFileService.deleteFileConSeq(conSeq);
				contentDetail.deleteContentDetailConSeq(conSeq);
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectConDelete", e, egovMessageSource);
		}
		return resultVO;
	}
}
