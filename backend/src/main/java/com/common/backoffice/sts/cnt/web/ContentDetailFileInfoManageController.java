package com.common.backoffice.sts.cnt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.cnt.modals.ContentDetailFileInfo;
import com.common.backoffice.sts.cnt.modals.ContentDetailFileInfoVO;
import com.common.backoffice.sts.cnt.service.ContentDetailFileInfoManageService;
import com.common.backoffice.sts.cnt.service.ContentDetailInfoService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 콘텐츠 상세페이지-파일 연결(멀티페이지 콘텐츠 편집기 내 파일 배치) 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.cnt.web.ContentDetailFileInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_contentfileinfo, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증
 * - 신규 등록 시 원본은 DB 시퀀스 contentfile_seq를 사용했으나 해당 시퀀스가 없어
 *   ContentDetailFileInfoManageService.generateFileSeq()에서 애플리케이션 레벨로 대체 채번
 * - "ContentUpdateOrder.do"(레거시 이름과 달리 실제로는 파일 순서가 아니라 재생시간(timeInterval)을
 *   갱신하고 합계를 돌려주는 API였음) → 이름을 실제 동작에 맞게 `timeIntervalUpdateAndSum.do`로
 *   변경. 순수 순서 변경은 원래도 별도 API(ContentUpdateOrderFile.do)였고 `orderUpdate.do`로 유지
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/conManage/detailFile")
@Tag(name = "ContentDetailFileInfoManageController", description = "콘텐츠 상세페이지-파일 연결 관리")
public class ContentDetailFileInfoManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ContentDetailFileInfoManageService conFileinfo;
	private final ContentDetailInfoService contentDetail;

	@Operation(summary = "상세페이지-파일 연결 리스트 조회", description = "conSeq/detailSeq로 연결된 파일 목록을 조회합니다.")
	@GetMapping("/list.do")
	public ResultVO conDetailInfoTable(@RequestParam("conSeq") String conSeq,
										@RequestParam("detailSeq") String detailSeq,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			ContentDetailFileInfoVO searchVO = new ContentDetailFileInfoVO();
			searchVO.setConSeq(conSeq);
			searchVO.setDetailSeq(detailSeq);

			List<ContentDetailFileInfoVO> list = conFileinfo.selectContentDetailFileLst(searchVO);
			ResultHelper.setSuccess(resultVO, list, Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "conDetailInfoTable", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "파일 ID로 상세 조회")
	@GetMapping("/byAtchFileId.do")
	public ResultVO contentFileDetailInfo(@RequestParam("atchFileId") String atchFileId, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, conFileinfo.selectContentDetailFileInfo(atchFileId), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentFileDetailInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "FILE_SEQ로 상세 조회")
	@GetMapping("/{fileSeq}.do")
	public ResultVO contentFileDetailInfofileSeq(@Parameter(description = "파일 순번") @PathVariable("fileSeq") String fileSeq,
												  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ContentDetailFileInfoVO detail = conFileinfo.selectContentDetailFileInfoFileSeq(fileSeq);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentFileDetailInfofileSeq", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 재생시간 미입력 파일 존재여부 확인", description = "미리보기 전 시간이 비어있는 파일이 있는지 확인합니다.")
	@GetMapping("/timeCheck.do")
	public ResultVO contentTimeCheck(@RequestParam("conSeq") String conSeq, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = conFileinfo.selectTimeIntevalNullCheck(conSeq);
			ResultHelper.setSuccess(resultVO, ret > 0, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentTimeCheck", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "상세페이지에 파일 연결 등록", description = "동영상/음원 파일이면 실제 재생시간으로 timeInterval을 보정하고, 상세페이지 총 재생시간을 갱신합니다.")
	@PostMapping("/insert.do")
	public ResultVO insertContentReg(@RequestBody ContentDetailFileInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = conFileinfo.insertContentDetailFileManage(vo);
			if (ret <= 0) {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.insert"));
				return resultVO;
			}

			ContentDetailFileInfoVO fileInfo = conFileinfo.selectContentDetailFileInfoFileSeq(vo.getFileSeq());
			if (fileInfo != null && !"IMAGE".equals(fileInfo.getMediaType()) && fileInfo.getPlayTime() != null
					&& fileInfo.getPlayTime().length() > 8) {
				fileInfo.setPlayTime(fileInfo.getPlayTime().substring(0, 8));
			}
			if (fileInfo != null && !"IMAGE".equals(fileInfo.getMediaType())) {
				vo.setTimeInterval(fileInfo.getPlayTime());
				conFileinfo.updateContentDetailFileTimeIntervalManage(vo);
			}
			int timeUpdateRet = contentDetail.updateContentDetailTimeManage(vo.getDetailSeq());

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("fileSeq", vo.getFileSeq());
			resultMap.put("timeUpdateRet", timeUpdateRet);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "insertContentReg", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "상세페이지 총 재생시간 합계 조회")
	@GetMapping("/sumTime.do")
	public ResultVO contentTotalTimeInterval(@RequestParam("detailSeq") String detailSeq, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, conFileinfo.selectDetailContentSumTime(detailSeq), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentTotalTimeInterval", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "파일 재생시간(timeInterval)만 갱신")
	@PostMapping("/timeIntervalUpdate.do")
	public ResultVO jsonFileTimeIntervalUpdate(@RequestBody ContentDetailFileInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = conFileinfo.updateContentDetailFileTimeIntervalManage(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "jsonFileTimeIntervalUpdate", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "파일 재생시간 갱신 후 상세페이지 총합 반환", description = "timeInterval이 10자를 넘으면 8자로 보정 후 저장하고, 갱신된 상세페이지 총 재생시간을 반환합니다.")
	@PostMapping("/timeIntervalUpdateAndSum.do")
	public ResultVO contentTimeInterval(@RequestBody ContentDetailFileInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (vo.getTimeInterval() != null && vo.getTimeInterval().length() > 10) {
				vo.setTimeInterval(vo.getTimeInterval().substring(0, 8));
			}
			int ret = conFileinfo.updateContentDetailFileTimeIntervalManage(vo);
			if (ret > 0) {
				ResultHelper.setSuccess(resultVO, conFileinfo.selectDetailContentSumTime(vo.getDetailSeq()), Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.update"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentTimeInterval", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "파일 순서(fileOrder)만 갱신")
	@PostMapping("/orderUpdate.do")
	public ResultVO updateOrder(@RequestBody ContentDetailFileInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = conFileinfo.updateContentOrderDetailFileManage(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateOrder", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "상세페이지-파일 연결정보 전체 수정")
	@PostMapping("/update.do")
	public ResultVO updateContentReg(@RequestBody ContentDetailFileInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = conFileinfo.updateContentDetailFileManage(vo);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("fileSeq", vo.getFileSeq());
				ResultHelper.setSuccess(resultVO, resultMap);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.update"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateContentReg", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "상세페이지-파일 연결 삭제", description = "삭제 후 갱신된 상세페이지 총 재생시간을 반환합니다.")
	@DeleteMapping("/{fileSeq}.do")
	public ResultVO deleteContentReg(@Parameter(description = "파일 순번") @PathVariable("fileSeq") String fileSeq,
									  @RequestParam("detailSeq") String detailSeq,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = conFileinfo.deleteContentDetailFileManage(fileSeq);
			if (ret > 0) {
				contentDetail.updateContentDetailTimeManage(detailSeq);
				ResultHelper.setSuccess(resultVO, conFileinfo.selectDetailContentSumTime(detailSeq), Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.delete"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteContentReg", e, egovMessageSource);
		}
		return resultVO;
	}
}
