package com.common.backoffice.bas.cnt.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.modals.CenterInfo;
import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import com.common.backoffice.bas.cnt.service.CenterInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.fileMultiService;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import egovframework.let.utl.fcc.service.EgovDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartRequest;

/**
 * 매장(센터) 정보 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sym.cnt.web.CenterInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_centerinfo, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 축소/변경된 부분:
 * - 세션 기반(LoginVO in HttpSession) → JWT(AuthHelper) 기반 인증으로 전환
 * - JSP 뷰 반환 → ResultVO(JSON) 반환
 * - 신규 등록 시 채번은 `FN_CENTERID()`(2026-07-27 DB에 생성 확인됨)를 그대로 사용
 * - 삭제 시 원본에 있던 방송 스케줄/기념일 연계 정리(CenterAnniManagerService,
 *   BrodScheduleManagerService 등)는 해당 모듈이 아직 포팅되지 않아 제외함(후속 작업 필요)
 * - selectCenterTimeInfo(`FN_CENTERBRODINFO` 필요)는 2026-07-27 함수 생성 확인 후
 *   `/{centerId}/timeInfo.do`로 신규 노출함
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/basicManage/cnt")
@Tag(name = "CenterInfoManageController", description = "매장(센터) 정보 관리")
public class CenterInfoManageController {

	// application.yml에는 webinfPath.url이 없고 Common.filePath만 있음(업로드 경로 키,
	// EgovConfigAppProperties의 fileStorePath와 동일한 키를 사용)
	@Value("${Common.filePath}")
	private String filePath;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final CenterInfoManageService centerInfoManageService;
	private final fileMultiService uploadFile;

	@Operation(summary = "매장 리스트 조회", description = "성공시 매장(센터) 리스트를 반환합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/list.do")
	public ResultVO selectCenterInfoManageListByPagination(@RequestBody CenterInfoVO searchVO,
															 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthor_Code(loginVO.getRoleId());
			searchVO.setGroupId(loginVO.getPartId());

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

			List<CenterInfoVO> list = centerInfoManageService.selectCenterInfoManageListByPagination(searchVO);
			int totCnt = centerInfoManageService.selectCenterInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterInfoManageListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 콤보 조회", description = "성공시 매장 콤보(id/명) 리스트를 반환합니다.")
	@GetMapping("/combo.do")
	public ResultVO selectCenterInfoManageCombo(CenterInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthor_Code(loginVO.getRoleId());
			searchVO.setMberId(loginVO.getManagerId());

			ResultHelper.setSuccess(resultVO, centerInfoManageService.selectCenterInfoManageCombo(searchVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterInfoManageCombo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 상세 조회", description = "성공시 매장(센터) 상세 정보를 반환합니다.")
	@GetMapping("/{centerId}.do")
	public ResultVO selectCenterInfoManageDetail(@Parameter(description = "매장 ID") @PathVariable("centerId") String centerId,
												  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			CenterInfoVO detail = centerInfoManageService.selectCenterInfoManageDetail(centerId);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterInfoManageDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 방송 시간대 조회", description = "특정 일자에 해당 매장이 방송 중인 시간대/방송코드를 조회합니다(기념일 우선, 없으면 정규 편성).")
	@GetMapping("/{centerId}/timeInfo.do")
	public ResultVO selectCenterTimeInfo(@Parameter(description = "매장 ID") @PathVariable("centerId") String centerId,
										  @Parameter(description = "조회 일자(YYYYMMDD)") @RequestParam(value = "centerSearchDay", required = false) String centerSearchDay,
										  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			CenterInfo vo = new CenterInfo();
			vo.setCenterId(centerId);
			vo.setCenterSearchDay(centerSearchDay != null && !centerSearchDay.isBlank()
					? centerSearchDay : EgovDateUtil.getCurrentDate(""));

			ResultHelper.setSuccess(resultVO, centerInfoManageService.selectCenterTimeInfo(vo), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCenterTimeInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/update.do")
	public ResultVO updateCenterInfoManage(MultipartRequest mRequest,
											CenterInfo vo,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setCenterUserId(loginVO.getManagerId());

			if (mRequest.getFiles("centerImg") != null && !mRequest.getFiles("centerImg").isEmpty()) {
				vo.setCenterImg(uploadFile.uploadFileNm(mRequest.getFiles("centerImg"), filePath));
			}
			if (mRequest.getFiles("centerImgMap") != null && !mRequest.getFiles("centerImgMap").isEmpty()) {
				vo.setCenterImgMap(uploadFile.uploadFileNm(mRequest.getFiles("centerImgMap"), filePath));
			}

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			int ret = isInsert
					? centerInfoManageService.insertCenterInfoManage(vo)
					: centerInfoManageService.updateCenterInfoManage(vo);

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateCenterInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 엑셀 일괄 등록", description = "엑셀 파일을 업로드해서 매장을 일괄 등록합니다. (1행은 헤더로 건너뜀)")
	@PostMapping("/excelUpload.do")
	public ResultVO uploadCenterExcel(@RequestParam("file") MultipartFile file,
									   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			if (file == null || file.isEmpty()) {
				resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.msg"));
				return resultVO;
			}

			int successCount = 0;
			int failCount = 0;
			CenterInfoExcelMapping mapping = new CenterInfoExcelMapping();

			try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
				Sheet sheet = wb.getSheetAt(0);
				// 1행(0)은 헤더이므로 2행(1)부터 읽음
				for (int i = 1; i <= sheet.getLastRowNum(); i++) {
					Row row = sheet.getRow(i);
					if (row == null) continue;

					CenterInfo centerInfo = mapping.mappingColumn(row);
					if (centerInfo.getCenterNm() == null || centerInfo.getCenterNm().isBlank()) {
						continue; // 빈 행 건너뜀
					}
					centerInfo.setCenterUserId(loginVO.getManagerId());

					try {
						centerInfoManageService.insertCenterInfoManage(centerInfo);
						successCount++;
					} catch (Exception rowEx) {
						log.error("엑셀 {}행 매장 등록 실패: {}", i + 1, rowEx.toString());
						failCount++;
					}
				}
			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("successCount", successCount);
			resultMap.put("failCount", failCount);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "uploadCenterExcel", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 삭제", description = "매장(센터) 정보를 삭제합니다.")
	@DeleteMapping("/{centerId}.do")
	public ResultVO deleteCenterInfoManage(@Parameter(description = "매장 ID") @PathVariable("centerId") String centerId,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = centerInfoManageService.deleteCenterInfoManage(centerId);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteCenterInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}
}
