package com.common.backoffice.sts.pic.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.pic.modals.DidMoniterPic;
import com.common.backoffice.sts.pic.modals.DidMoniterPicVO;
import com.common.backoffice.sts.pic.service.DidMoniterPicService;
import com.common.backoffice.sts.snd.modals.SendMsgInfo;
import com.common.backoffice.sts.snd.service.SendMsgInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.fileMultiService;
import egovframework.com.cmm.EgovMessageSource;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartRequest;

/**
 * DID 모니터링 캡처화면(스크린샷) 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.pic.web.DidMoniterPicController를
 * 참조해서 did_emart(REST + tb_didstatepic, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - /Capture.do(DID 장비가 캡처화면을 업로드하는 기계 간 통신 API)는 원본에도 인증 체크가
 *   없었음 — 그대로 유지하고 SecurityConfig의 AUTH_WHITELIST에 등록함(로그인 불필요)
 * - /didFileLst.do(원본은 JSP 뷰 이름만 반환하고 실제 목록 조회 로직이 없었음) → 실제로
 *   매퍼/서비스에 이미 있던 페이징 목록 조회를 붙여서 진짜 리스트 API로 만듦
 * - /didFileUpload.do(원본은 UUID로 직접 파일명 생성 후 File.transferTo)를 기존에 이미
 *   구축된 fileMultiService(다른 컨트롤러들과 동일한 업로드 유틸)로 통일
 * - 존재하지 않는 egovframework.let.utl.fcc.service.FileUpladController 의존 제거
 * - JSP 뷰 반환 → ResultVO(JSON) 반환, 세션 기반 → JWT(AuthHelper) 기반 인증(관리자용 2개
 *   엔드포인트만 — 장비용 캡처 업로드는 원본처럼 인증 없음)
 * - DidMoniterPicVO에 매퍼가 참조하던 strDate/endDate 검색 파라미터가 빠져 있어 추가함
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/equiManage/pic")
@Tag(name = "DidMoniterPicController", description = "DID 모니터링 캡처화면 관리")
public class DidMoniterPicController {

	// application.yml에는 Globals.fileStorePath가 없고 Common.filePath만 있음(업로드 경로 키)
	@Value("${Common.filePath}")
	private String filePath;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final DidMoniterPicService didMoniterService;
	private final SendMsgInfoManageService sendService;
	private final fileMultiService uploadFile;

	@Operation(
			summary = "DID 캡처화면 업로드(장비 API)",
			description = "DID 장비가 재생 화면을 캡처해서 업로드한다. 로그인 불필요(SecurityConfig 화이트리스트 등록됨)."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "처리 결과(O/0 문자열)")
	})
	@PostMapping("/capture.do")
	public String insertDidPictureXml(MultipartRequest mRequest, HttpServletRequest request) {
		DidMoniterPicVO vo = new DidMoniterPicVO();
		String message;
		String realFolder = filePath + "/didpic/";

		try {
			String didId = request.getParameter("DID_ID") != null ? request.getParameter("DID_ID") : "";
			String didMac = request.getParameter("DID_MAC") != null ? request.getParameter("DID_MAC") : "";
			String msgSeq = request.getParameter("MSG_SEQ") != null ? request.getParameter("MSG_SEQ") : "";

			vo.setDidId(didId);
			vo.setDidMac(didMac);
			vo.setMsgSeq(msgSeq);

			String fileNm = uploadFile.uploadFileNm(mRequest.getFiles("DID_PICTURE"), realFolder);
			vo.setDidFileNm(fileNm);

			int ret = didMoniterService.insertDidMoniterPicManage(vo);
			if (ret > 0) {
				SendMsgInfo sendInfo = new SendMsgInfo();
				sendInfo.setMsgSeq(msgSeq);
				sendInfo.setDidId(didId);
				sendInfo.setDidMacAddress(didMac);

				if (fileNm != null && !fileNm.isEmpty()) {
					sendInfo.setSendResult("Y");
					sendInfo.setErrorMessage("");
				} else {
					sendInfo.setSendResult("N");
					sendInfo.setErrorMessage("File Update Error");
				}
				sendService.updateSendMsgInfoManage(sendInfo);
				message = "O";
			} else {
				message = "0";
			}
		} catch (Exception e) {
			log.error("insertDidPictureXml error: {}", e.toString());
			message = "0";
		}
		return message;
	}

	@Operation(summary = "DID 캡처화면 리스트 조회", description = "등록일(strDate~endDate, YYYYMMDD) 범위로 조회 가능합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/list.do")
	public ResultVO listDidPicture(@RequestBody DidMoniterPicVO searchVO, HttpServletRequest request) throws Exception {
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

			List<DidMoniterPicVO> list = didMoniterService.selectDidMoniterPicManageListByPagination(searchVO);
			int totCnt = didMoniterService.selectDidMoniterPicManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "listDidPicture", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 캡처화면 수동 등록", description = "관리자가 직접 캡처화면을 등록합니다.")
	@PostMapping("/upload.do")
	public ResultVO insertDidPicture(MultipartRequest mRequest, DidMoniterPic vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			List<org.springframework.web.multipart.MultipartFile> files = mRequest.getFiles("didFile");
			if (files != null && !files.isEmpty()) {
				vo.setDidFileNm(uploadFile.uploadFileNm(files, filePath));
			}

			int ret = didMoniterService.insertDidMoniterPicManage(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.insert", egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "insertDidPicture", e, egovMessageSource);
		}
		return resultVO;
	}
}
