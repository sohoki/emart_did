package com.common.backoffice.sts.cnt.web;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.sts.cnt.modals.ContentFileInfo;
import com.common.backoffice.sts.cnt.modals.ContentFileInfoVO;
import com.common.backoffice.sts.cnt.service.ContentFileInfoManageService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.backoffice.util.service.fileMultiService;

/**
 * 콘텐츠 파일(이미지/영상/음원) 라이브러리 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.cnt.web.ContentFileInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_contentfileinfo/lettnfiledetail, postgresql) 기준으로
 * 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증
 * - 원본은 같은 조회 로직(mediaType/notConType 필터 페이징 조회)을 4개 엔드포인트로 중복 구현했음
 *   (mediaLst.do, jsonContentLst.do, jsonDetailContentLst.do, playContentList.do/brodContentPlayList.do)
 *   → `POST /list.do` 하나로 통합(파라미터로 mediaType/notConType/fileGubun/searchCondition/
 *   searchKeyword 전달)
 * - mediaUpadateUseYn(단건 useYn 변경)과 playContentStateChange(다건 useYn 변경)도 다건 처리
 *   하나(`POST /useYnBulk.do`)로 통합(단건은 배열 크기 1로 호출)
 * - 존재하지 않는 egovframework.let.utl.fcc.service.FileUpladController 의존 제거,
 *   fileMultiService.deleteFile로 통일
 * - fileView.do(JSP 뷰 전용, fileDetail.do와 데이터 동일)는 제외
 * - contentTotalCnt.do(리스트 API의 totalCnt와 중복)는 제외
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/conManage/file")
@Tag(name = "ContentFileInfoManageController", description = "콘텐츠 파일(이미지/영상/음원) 라이브러리 관리")
public class ContentFileInfoManageController {

	@Value("${Common.filePath}")
	private String filePath;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ContentFileInfoManageService conFileService;
	private final fileMultiService uploadFile;

	@Operation(summary = "콘텐츠 파일 리스트 조회",
            description = "mediaType(IMAGE/MEDIA/MUSIC)/notConType/fileGubun/검색조건으로 페이징 조회합니다.",
            tags = {"ContentFileInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
	@PostMapping("/list.do")
	public ResultVO selectFilePageListByPagination(@RequestBody ContentFileInfoVO searchVO,
                                                   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            LoginVO vo = AuthHelper.getLoginVO();
            searchVO.setAuthorCode(vo.getRoleId());
            searchVO.setAuthorCode("ROLE_ADMIN");
            searchVO.setMberId(vo.getManagerId());

            searchVO.setMediaType(UtilInfoService.NVLObj(searchVO.getMediaType(),""));
            searchVO.setNotConType(UtilInfoService.NVLObj(searchVO.getNotConType(),""));
            searchVO.setFileGubun(UtilInfoService.NVLObj(searchVO.getFileGubun(),""));


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

			List<ContentFileInfoVO> list = conFileService.selectFilePageListByPagination(searchVO);
			int totCnt = conFileService.selectFilePageListByPaginationTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectFilePageListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 상세페이지에 연결된 파일 목록 조회", description = "detailSeq(콘텐츠 상세페이지 순번)에 연결된 파일 목록을 순서대로 반환합니다.")
	@GetMapping("/byDetail.do")
	public ResultVO selectFileContentPageList(@RequestParam("detailSeq") String detailSeq, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, conFileService.selectFileContentPageList(detailSeq), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectFileContentPageList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 상세 조회")
	@GetMapping("/{atchFileId}.do")
	public ResultVO selectFileDetail(@Parameter(description = "첨부파일 ID") @PathVariable("atchFileId") String atchFileId,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ContentFileInfoVO detail = conFileService.selectFileDetail(atchFileId);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectFileDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 연결 현황 확인", description = "해당 파일을 사용 중인 콘텐츠(멀티페이지) 목록/건수를 반환합니다.")
	@GetMapping("/connCheck.do")
	public ResultVO selectMediaConnList(@RequestParam("atchFileId") String atchFileId, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, conFileService.selectMediaConnList(atchFileId), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMediaConnList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 상세정보(재생시간/크기) 갱신", description = "브라우저에서 측정한 재생시간(playTime)/가로세로 크기를 저장합니다.")
	@PostMapping("/detailUpdate.do")
	public ResultVO updateFileDetailInfo(@RequestBody ContentFileInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			if (vo.getPlayTime() != null && !vo.getPlayTime().isBlank()) {
				double playTimeSec = Double.parseDouble(vo.getPlayTime());
				vo.setPlayTime(Integer.toString((int) playTimeSec));
			}
			int ret = conFileService.updateFileDetailInfo(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateFileDetailInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 다건 업로드", description = "미디어 라이브러리에 새 파일을 등록합니다. mediaType은 저장 안 하고 조회 시 확장자 기준으로 자동 판별됩니다.")
	@PostMapping("/upload.do")
	public ResultVO uploadFileManage(@RequestParam("files") List<MultipartFile> files, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			// 기존 데이터와 동일한 관례: /EMART_DID/did/upload/{yyyyMM}/ 아래 저장
			// (물리 저장 경로 = filePath + 관례 경로, DB엔 이 관례 경로를 그대로 fileStreCours로 저장)
			String yyyyMM = new SimpleDateFormat("yyyyMM").format(new Date());
			String relDir = "/EMART_DID/did/upload/" + yyyyMM + "/";
			String targetDir = Paths.get(filePath, "EMART_DID", "did", "upload", yyyyMM).toString();

			int successCount = 0;
			for (MultipartFile file : files) {
				fileMultiService.FileUploadResult uploaded = uploadFile.uploadFile(file, targetDir);
				if (uploaded == null) {
					continue;
				}

				String savedName = uploaded.savedFileName();
				int dot = savedName.lastIndexOf('.');
				String atchFileId = (dot != -1) ? savedName.substring(0, dot) : savedName;
				String fileExtsn = (dot != -1) ? savedName.substring(dot + 1) : "";

				ContentFileInfo vo = new ContentFileInfo();
				vo.setMode(Globals.SAVE_MODE_INSERT);
				vo.setAtchFileId(atchFileId);
				vo.setFileStreCours(relDir);
				vo.setStreFileNm(savedName);
				vo.setOrignlFileNm(uploaded.originalFileName());
				vo.setFileExtsn(fileExtsn);
				vo.setFileSize(String.valueOf(file.getSize()));
				vo.setConSeq("0");
				vo.setGroupId(loginVO.getPartId());
				vo.setFrstRegisterId(loginVO.getManagerId());

				int ret = conFileService.insertFileManage(vo);
				if (ret > 0) {
					successCount++;
				}
			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("successCount", successCount);
			resultMap.put("totalCount", files.size());
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "uploadFileManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 사용여부 일괄 변경", description = "atchFileIds에 담긴 파일들의 사용여부(useYn)를 한 번에 변경합니다. 단건 변경도 배열 크기 1로 호출합니다.")
	@PostMapping("/useYnBulk.do")
	public ResultVO updateFileManageUseYnBulk(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			@SuppressWarnings("unchecked")
			List<String> atchFileIds = (List<String>) body.getOrDefault("atchFileIds", new ArrayList<>());
			String useYn = String.valueOf(body.get("useYn"));

			int ret = 0;
			for (String atchFileId : atchFileIds) {
				ContentFileInfo fileInfo = new ContentFileInfo();
				fileInfo.setAtchFileId(atchFileId);
				fileInfo.setUseYn(useYn);
				ret = conFileService.updateFileManageUseYn(fileInfo);
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateFileManageUseYnBulk", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 단건 삭제", description = "DB 레코드와 실제 파일(+썸네일)을 함께 삭제합니다.")
	@DeleteMapping("/{atchFileId}.do")
	public ResultVO deleteFileManage(@Parameter(description = "첨부파일 ID") @PathVariable("atchFileId") String atchFileId,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ContentFileInfo fileInfo = conFileService.selectFileDetail(atchFileId);
			int ret = conFileService.deleteFileManage(atchFileId);
			if (ret > 0 && fileInfo != null) {
				deletePhysicalFile(fileInfo);
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteFileManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 파일 일괄 삭제(음원 리스트 등)", description = "atchFileIds에 담긴 파일들을 DB+실파일 함께 삭제합니다.")
	@PostMapping("/deleteBulk.do")
	public ResultVO deleteFileManageBulk(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			@SuppressWarnings("unchecked")
			List<String> atchFileIds = (List<String>) body.getOrDefault("atchFileIds", new ArrayList<>());

			int ret = 0;
			for (String atchFileId : atchFileIds) {
				ContentFileInfo fileInfo = conFileService.selectFileDetail(atchFileId);
				ret = conFileService.deleteFileManage(atchFileId);
				if (ret > 0 && fileInfo != null) {
					deletePhysicalFile(fileInfo);
				}
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteFileManageBulk", e, egovMessageSource);
		}
		return resultVO;
	}

	private void deletePhysicalFile(ContentFileInfo fileInfo) {
		try {
			uploadFile.deleteFile(fileInfo.getFileStreCours() + fileInfo.getStreFileNm(), filePath);
			log.info("콘텐츠 파일 삭제: {}{}", fileInfo.getFileStreCours(), fileInfo.getStreFileNm());
			if ("MEDIA".equals(fileInfo.getMediaType()) && fileInfo.getFileThumnail() != null) {
				uploadFile.deleteFile(fileInfo.getFileStreCours() + fileInfo.getFileThumnail(), filePath);
			}
		} catch (Exception e) {
			log.debug("콘텐츠 파일 실파일 삭제 실패: {}", e.toString());
		}
	}
}
