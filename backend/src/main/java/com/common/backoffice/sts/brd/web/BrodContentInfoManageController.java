package com.common.backoffice.sts.brd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.code.service.EgovCcmCmmnDetailCodeManageService;
import com.common.backoffice.sts.brd.modals.BrodAnniversary;
import com.common.backoffice.sts.brd.modals.BrodContentDetail;
import com.common.backoffice.sts.brd.modals.BrodContentDetailVO;
import com.common.backoffice.sts.brd.modals.BrodContentInfo;
import com.common.backoffice.sts.brd.modals.BrodContentInfoVO;
import com.common.backoffice.sts.brd.service.BasicBrodInfoManageService;
import com.common.backoffice.sts.brd.service.BrodAnniversaryManagerService;
import com.common.backoffice.sts.brd.service.BrodContentDetailManagerService;
import com.common.backoffice.sts.brd.service.BrodContentInfoManageService;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfoVO;
import com.common.backoffice.sts.brd.service.BrodOrganizationManagerService;
import com.common.backoffice.sts.brd.service.BrodScheduleManagerService;
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
 * 방송(음원) 콘텐츠 관리 API.
 * did_emart(REST + JWT + tb_brodschedule, postgresql) 기준으로 리팩토링함.
 *
 * 원본(1162줄) 대비 축소/변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증. getMberId() → getManagerId()로 매핑
 * - **"편성표 생성/조회/엑셀" 클러스터를 이번 범위에서 제외함**(`ContentBrodConfirm.do`,
 *   `ContentBrodReport.do`, `ContentBrodExcel.do`, `playCenterInfo.do`, private 헬퍼
 *   `brodReport()` — 약 410줄). 사유:
 *   1) `ContentBrodConfirm.do`(selectBrodLst)가 `UniSelectInfoManageService`라는, did_emart에
 *      아예 존재하지 않는 유틸리티에 의존함. 이 서비스는 테이블명/컬럼명을 자바 문자열로 조립해서
 *      동적 SQL을 실행하는 패턴이라(`fnBasic.setInTable("TB_BRODSCHEDULE a, LETTCCMMNDETAILCODE b")`
 *      식), 대충 짐작해서 새로 만들면 SQL 인젝션 위험이 있는 코드를 새로 들이는 셈이 됨
 *   2) `playCenterInfo.do`는 `CenterInfoManageService.selectCenterTimeInfo()`를 호출하는데,
 *      이 메서드는 9절(CenterInfoManageController) 작업 때 `FN_CENTERBRODINFO` DB 함수가 없어서
 *      이미 명시적으로 포팅 제외했던 것과 동일함
 *   3) 사용자에게 확인 후 "편성표 생성 제외하고 나머지만 진행"으로 범위를 확정함(후속 작업 필요)
 * - 신규 등록/복사 시 원본은 EgovIdGnrService(egovBrodIdGnrService, did_emart 미구성) 빈으로
 *   BROD_CODE를 채번했으나, 11절(BrodScheduleManagerController)에서 이미 추가한
 *   `BrodContentInfoManageService.generateBrodCode()`(MAX+1)를 재사용함
 * - `deleteRightContent`(rightbrodContentDetailDel.do)의 원본 파라미터는 "브로드시퀀스ㅣ구분"을
 *   콤마로 이어붙인 문자열(`ㅣ`를 구분자로 쓰는 손코딩 인코딩)이었는데, 이번엔 순수 REST API라
 *   그럴 필요가 없어 `{ id, gubun }` 객체 배열의 정상적인 JSON으로 정리함
 * - 매퍼 버그 1건 발견/수정: `BrodContentInfoManagerMapper.xml`의 `selectBrodContentLst`가
 *   존재하지 않는 DB 함수 `FN_DETAILCODENM(...)`을 호출하고 있어 `LETTCCMMNDETAILCODE` 서브쿼리로 대체
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/brodManage/content")
@Tag(name = "BrodContentInfoManageController", description = "방송(음원) 콘텐츠 관리")
public class BrodContentInfoManageController {

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	private final BrodContentInfoManageService brodContent;
	private final BrodScheduleManagerService brodSchedule;
	private final BrodContentDetailManagerService brodDetail;
	private final BrodAnniversaryManagerService anniverInfo;
	private final BrodOrganizationManagerService brodOrgService;
	private final BasicBrodInfoManageService basicInfo;
	private final ContentFileInfoManageService conFileService;
	private final EgovCcmCmmnDetailCodeManageService cmmnDetailCodeManageService;

	@Operation(summary = "방송(음원) 콘텐츠 리스트 조회")
	@PostMapping("/list.do")
	public ResultVO selectBrodContentLst(@RequestBody BrodContentInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt(Globals.PAGE_UNIT));
			}
			searchVO.setPageSize(propertiesService.getInt(Globals.PAGE_SIZE));
			if (searchVO.getSecGubun() == null) searchVO.setSecGubun("");
			if (searchVO.getCenterGubun() == null) searchVO.setCenterGubun("");

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			List<BrodContentInfoVO> list = brodContent.selectBrodContentLst(searchVO);
			int totCnt = brodContent.selectBrodContentPageCnt(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodContentLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "우측 편성 후보 리스트 조회", description = "특정 음원 파일(atchFileId)을 배치할 수 있는 방송 후보 목록을 조회합니다.")
	@GetMapping("/right.do")
	public ResultVO selectBrodPlayListRight(@RequestParam("atchFileId") String atchFileId, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			BrodContentInfo vo = new BrodContentInfo();
			vo.setAtchFileId(atchFileId);
			ResultHelper.setSuccess(resultVO, brodContent.selectBrodRight(vo), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodPlayListRight", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "편성 항목 일괄 삭제", description = "gubun=D면 일반 편성(상세) 삭제, 그 외(A)는 특정방송(기념일) 삭제로 처리합니다. 삭제 후 배포 스케줄을 재계산합니다.")
	@PostMapping("/rightDelete.do")
	public ResultVO deleteRightContent(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			@SuppressWarnings("unchecked")
			List<Map<String, String>> items = (List<Map<String, String>>) body.getOrDefault("items", new ArrayList<>());
			String insertBrodCode = String.valueOf(body.getOrDefault("insertBrodCode", ""));

			int ret = 0;
			for (Map<String, String> item : items) {
				if ("D".equals(item.get("gubun"))) {
					ret = brodDetail.deleteBrodContentDetail(item.get("id"));
				} else {
					ret = anniverInfo.deleteBrodAnniver(item.get("id"));
				}
			}
			scheduleUpdate(insertBrodCode);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteRightContent", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "일반 편성(음원) 다건 등록", description = "insertBrodCodes(콤마구분)에 담긴 방송들에 동일한 음원 편성을 각각 등록하고, 배포 스케줄을 재계산합니다.")
	@PostMapping("/detailCenterUpdate.do")
	public ResultVO updateFileBrodSchedule(@RequestBody BrodContentDetail vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String[] brodArray = vo.getInsert_brodCode().split(",");
			StringBuilder resultMessage = new StringBuilder();
			for (String brodCode : brodArray) {
				vo.setBrodCode(brodCode);
				String centerNm = brodContent.selectBrodContentCenterNm(brodCode);
				brodDetail.insertBrodContentDetail(vo);
				resultMessage.append(centerNm).append(" (").append(brodCode).append(") | 스케줄 등록 완료<br>");
			}
			scheduleUpdate(vo.getInsert_brodCode());
			ResultHelper.setSuccess(resultVO, resultMessage.toString(), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateFileBrodSchedule", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "특정방송(기념일) 다건 등록", description = "insertBrodCodes(콤마구분)에 담긴 방송들에 동일한 기념일 편성을 각각 등록하고, 배포 스케줄을 재계산합니다.")
	@PostMapping("/annDetailCenterUpdate.do")
	public ResultVO updateFileAnnBrodSchedule(@RequestBody BrodAnniversary vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String[] brodArray = vo.getInsert_brodCode().split(",");
			StringBuilder resultMessage = new StringBuilder();
			for (String brodCode : brodArray) {
				String centerNm = brodContent.selectBrodContentCenterNm(brodCode);
				vo.setBrodCode(brodCode);
				vo.setAnniverStartDay(vo.getContentStartDay());
				vo.setAnniverEndDay(vo.getContentEndDay());
				if ("ANNGUBUN02".equals(vo.getAnniversaryGubun())) {
					vo.setAnniversaryTime(vo.getAnniversaryTimeHour() + vo.getAnniversaryTimeTime());
				}
				anniverInfo.insertBrodAnniver(vo);
				resultMessage.append(centerNm).append(" (").append(brodCode).append("), 특정방송 등록 완료<br>");
			}
			scheduleUpdate(vo.getInsert_brodCode());
			ResultHelper.setSuccess(resultVO, resultMessage.toString(), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateFileAnnBrodSchedule", e, egovMessageSource);
		}
		return resultVO;
	}

	/** 콘텐츠 편성 변경 시(등록/수정/삭제) 연결된 배포 스케줄을 재계산한다. */
	private int scheduleUpdate(String insertBrodCode) {
		try {
			String[] brodArray = insertBrodCode.split(",");
			int ret = 0;
			for (String brodCode : brodArray) {
				BrodContentInfo contentInfo = brodContent.selectBrodContentInfo(brodCode);
				brodContent.updateBrodBasicCodeCntMin(brodCode);
				contentInfo.setBasicBrodCode("");
				brodContent.updateBrodContentBasicInfo(contentInfo);

				var scheduleInfo = new BrodScheduleInfo();
				scheduleInfo.setCreateCheck("N");
				scheduleInfo.setCenterId(contentInfo.getCenterId());
				scheduleInfo.setBrodCode(brodCode);
				brodSchedule.updateBrodScheduleCenter(scheduleInfo);
			}
			return 1;
		} catch (Exception e) {
			log.debug("scheduleUpdate error: {}", e.toString());
			return 0;
		}
	}

	@Operation(summary = "음원 등록 팝업 데이터 조회", description = "특정방송 구분 콤보/시간 콤보와 대상 파일명을 반환합니다.")
	@GetMapping("/copyPopupData.do")
	public ResultVO selectPopCenterReg(@RequestParam("atchFileId") String atchFileId, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			ContentFileInfoVO fileInfo = conFileService.selectFileDetail(atchFileId);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("anniversaryGubun", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT020"));
			resultMap.put("timeInfo1", brodDetail.selectTimeHourCombo());
			resultMap.put("timeInfo", brodDetail.selectTimeCombo("050"));
			resultMap.put("orignlFileNm", fileInfo != null ? fileInfo.getOrignlFileNm() : "");
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectPopCenterReg", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송 콘텐츠 등록/수정 화면용 데이터 조회", description = "재생간격 콤보/기본방송 콤보와, 수정 모드일 때 기존 방송 상세를 함께 반환합니다.")
	@GetMapping("/formData.do")
	public ResultVO selectBrodContentDetail(@RequestParam(value = "brodCode", required = false, defaultValue = "") String brodCode,
											 @RequestParam(value = "mode", required = false, defaultValue = "Ins") String mode,
											 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("brodInterval", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT019"));
			resultMap.put("basicInfo", basicInfo.selectBasicBrodCombo());
			if ("Edt".equals(mode) && !brodCode.isBlank()) {
				resultMap.put("regist", brodContent.selectBrodContentInfo(brodCode));
			}
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodContentDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송(음원) 콘텐츠 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다. 재생시간이 줄어들면 초과된 상세 편성을 함께 정리합니다.")
	@PostMapping("/update.do")
	public ResultVO updateBrodContent(@RequestBody BrodContentInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			String preInterval = null;
			int ret;
			if (isInsert) {
				vo.setBrodCode(brodContent.generateBrodCode());
				vo.setFrstRegisterId(loginVO.getManagerId());
				if (vo.getBasicFileId() == null || vo.getBasicFileId().isBlank()) {
					vo.setBasicFileId("FILE_000000000000001");
				}
				ret = brodContent.insertBrodContent(vo);
			} else {
				preInterval = brodContent.selectBrodContentTimeInfo(vo.getBrodCode());
				vo.setLastUpdusrId(loginVO.getManagerId());
				ret = brodContent.updateBrodContent(vo);
			}

			if (ret > 0 && preInterval != null) {
				String nowInterval = brodContent.selectBrodContentTimeInfo(vo.getBrodCode());
				if (Integer.parseInt(preInterval) > Integer.parseInt(nowInterval)) {
					BrodContentDetail detail = new BrodContentDetail();
					detail.setBrodCode(vo.getBrodCode());
					detail.setIntervalSection(nowInterval);
					brodDetail.deleteBrodContentTimeDel(detail);
				}
			}

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateBrodContent", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "배포 스케줄 재확정", description = "생성 대기(N) 상태인 배포 스케줄을 확정 반영합니다.")
	@PostMapping("/scheduleConfirm.do")
	public ResultVO batchSchChnage(@RequestParam("brodCode") String brodCode, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			List<BrodScheduleInfoVO> schedule = brodSchedule.selectBrodScheduleUpdateChanage(brodCode);
			int ret = 0;
			for (var sch : schedule) {
				var scheduleInfo = new BrodScheduleInfo();
				scheduleInfo.setBrodCode(brodCode);
				scheduleInfo.setCreateCheck("N");
				scheduleInfo.setCenterId(sch.getCenterId());
				scheduleInfo.setBrodDay(sch.getBrodDay());

				if ("N".equals(sch.getCreateCheck())) {
					ret = brodSchedule.updateBrodSchedule(scheduleInfo);
				} else {
					ret = brodSchedule.insertBrodSchedule(scheduleInfo);
				}
			}

			if (ret > 0) {
				BrodContentInfo vo = new BrodContentInfo();
				vo.setBrodCode(brodCode);
				vo.setBrodChangeInfo("Y");
				brodContent.updateBrodContentSchChange(vo);
			}
			ResultHelper.setSuccess(resultVO, ret > 0, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "batchSchChnage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송(음원) 콘텐츠 상세 조회", description = "방송 정보와 연결된 특정방송(기념일) 목록을 함께 반환합니다.")
	@GetMapping("/{brodCode}.do")
	public ResultVO viewBrodContent(@Parameter(description = "방송 코드") @PathVariable String brodCode, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			BrodContentInfo detail = brodContent.selectBrodContentInfo(brodCode);
			if (detail == null) {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
				return resultVO;
			}

			BrodAnniversary annBrod = new BrodAnniversary();
			annBrod.setBrodCode(brodCode);
			annBrod.setBrodDay("");

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("regist", detail);
			resultMap.put("brodAnniver", anniverInfo.selectBrodAnniverLst(annBrod));
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "viewBrodContent", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "시간대별 편성 리스트 조회")
	@GetMapping("/timeList.do")
	public ResultVO selectTimeContent(@RequestParam("brodCode") String brodCode,
									   @RequestParam(value = "timeInterval", required = false, defaultValue = "") String timeInterval,
									   @RequestParam(value = "brodDay", required = false, defaultValue = "") String brodDay,
									   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			BrodContentDetailVO detail = new BrodContentDetailVO();
			detail.setBrodCode(brodCode);
			detail.setIntervalSection(timeInterval);
			detail.setBrodDay(brodDay);
			ResultHelper.setSuccess(resultVO, brodDetail.selectBrodContentDetailLst(detail), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectTimeContent", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송 편성 건수 조회")
	@GetMapping("/copyCount.do")
	public ResultVO contentCnt(@RequestParam("brodCode") String brodCode, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			BrodContentDetailVO searchVO = new BrodContentDetailVO();
			searchVO.setBrodCode(brodCode);
			searchVO.setIntervalSection("");
			ResultHelper.setSuccess(resultVO, brodDetail.selectBrodContentDetailPageCnt(searchVO), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentCnt", e, egovMessageSource);
		}
		return resultVO;
	}

	/** 방송(음원) 콘텐츠를 편성/기념일까지 통째로 복사한다. 실패 시 부분 생성된 데이터를 정리한다. */
	private int publicContentCopy(BrodContentInfo vo, String managerId) throws Exception {
		String oldBrodCode = vo.getBrodCode();
		String brodCode = brodContent.generateBrodCode();
		try {
			vo.setPrebrodCode(vo.getBrodCode());
			vo.setBrodCode(brodCode);
			vo.setFrstRegisterId(managerId);

			int ret = brodContent.insertBrodContentCopy(vo);
			if (ret <= 0) {
				return 0;
			}

			BrodContentDetail detail = new BrodContentDetail();
			detail.setPrebrodCode(oldBrodCode);
			detail.setContentStartDay(vo.getContentStartDay());
			detail.setContentEndDay(vo.getContentEndDay());
			detail.setBrodCode(brodCode);
			detail.setFrstRegisterId(managerId);
			ret = brodDetail.insertBrodContentCopy(detail);
			if (ret <= 0) {
				brodContent.deleteBrodContent(brodCode);
				return 0;
			}

			BrodAnniversary anniver = new BrodAnniversary();
			anniver.setPrebrodCode(oldBrodCode);
			anniver.setAnniverStartDay(vo.getContentStartDay());
			anniver.setAnniverEndDay(vo.getContentEndDay());
			anniver.setBrodCode(brodCode);
			anniver.setFrstRegisterId(managerId);
			anniverInfo.insertBrodAnniverCopy(anniver);
			return 1;
		} catch (Exception e) {
			brodDetail.deleteBrodContentBrodCode(brodCode);
			brodContent.deleteBrodContent(brodCode);
			log.debug("publicContentCopy error: {}", e.toString());
			return 0;
		}
	}

	@Operation(summary = "방송(음원) 콘텐츠 복사 등록")
	@PostMapping("/copyInsert.do")
	public ResultVO contentCopy(@RequestBody BrodContentInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			int result = publicContentCopy(vo, loginVO.getManagerId());
			ResultHelper.setCudResult(resultVO, result, "success.common.insert", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentCopy", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송(음원) 콘텐츠 일괄 삭제", description = "편성/배치/기본음원/특정방송을 모두 함께 삭제합니다.")
	@PostMapping("/deleteBulk.do")
	public ResultVO deleteBrodContent(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			@SuppressWarnings("unchecked")
			List<String> brodCodes = (List<String>) body.getOrDefault("brodCodes", new ArrayList<>());
			int ret = 0;
			for (String brodCode : brodCodes) {
				brodOrgService.deleteContentToOrg(brodCode);
				ret = brodSchedule.deleteBrodScheduleAll(brodCode);
				brodDetail.deleteBrodContentBrodCodeALL(brodCode);
				anniverInfo.deleteBrodAnniverBrodAll(brodCode);
				ret = brodContent.deleteBrodContentAll(brodCode);
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteBrodContent", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "방송(음원) 콘텐츠 콤보 조회")
	@GetMapping("/combo.do")
	public ResultVO selectBrodCombo(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, brodContent.selectBrodContentCopy("000000000000000"), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodCombo", e, egovMessageSource);
		}
		return resultVO;
	}
}
