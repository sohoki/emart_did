package com.common.backoffice.sts.brd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.modals.CenterInfo;
import com.common.backoffice.bas.cnt.service.CenterInfoManageService;
import com.common.backoffice.sts.brd.modals.BrodAnniversary;
import com.common.backoffice.sts.brd.modals.BrodContentDetail;
import com.common.backoffice.sts.brd.modals.BrodContentInfo;
import com.common.backoffice.sts.brd.modals.BrodContentInfoVO;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfoVO;
import com.common.backoffice.sts.brd.service.BrodAnniversaryManagerService;
import com.common.backoffice.sts.brd.service.BrodContentDetailManagerService;
import com.common.backoffice.sts.brd.service.BrodContentInfoManageService;
import com.common.backoffice.sts.brd.service.BrodScheduleManagerService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 방송 배포 스케줄(매장별 편성 배포) 관리 API.
 * did_emart(REST + JWT + tb_brodschedule, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증. getMberId() → getManagerId()로 매핑
 * - JSP 뷰/팝업 반환 → ResultVO(JSON) 반환
 * - 매장에 신규로 방송을 배포(연결)할 때 원본은 EgovIdGnrService(egovBrodIdGnrService) 빈으로
 *   BROD_CODE를 채번했으나 did_emart에 해당 빈이 구성되어 있지 않아
 *   BrodContentInfoManageService.generateBrodCode()에서 애플리케이션 레벨로 대체 채번함
 *   (패턴: BROD_ + 10자리 zero-pad 일련번호, 기존 데이터 포맷과 일치 확인)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/brodManage")
@Tag(name = "BrodScheduleManagerController", description = "방송 배포 스케줄 관리")
public class BrodScheduleManagerController {

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	private final BrodContentInfoManageService brodContent;
	private final BrodScheduleManagerService brodSchedule;
	private final BrodContentDetailManagerService brodDetail;
	private final BrodAnniversaryManagerService anniverInfo;
	private final CenterInfoManageService centerInfoManageService;

	@Operation(summary = "배포 대상 콘텐츠(방송) 리스트 조회", description = "좌측 패널 - 음원 콘텐츠 배포 대상 목록을 조회합니다.")
	@PostMapping("/playSchedule/list.do")
	public ResultVO selectLeftContentLst(@RequestBody BrodContentInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt(Globals.PAGE_UNIT));
			}
			searchVO.setPageSize(propertiesService.getInt(Globals.PAGE_SIZE));
			searchVO.setSecGubun("SECGUBUN01");
			searchVO.setCenterGubun("content");

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
			ResultHelper.setFailResult(resultVO, "selectLeftContentLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "배포 현황 리스트 조회")
	@PostMapping("/playSchedule/statusList.do")
	public ResultVO selectplaySheduleStatus(@RequestBody BrodScheduleInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt(Globals.PAGE_UNIT));
			}
			searchVO.setPageSize(propertiesService.getInt(Globals.PAGE_SIZE));
			if (searchVO.getCreateCheck() == null) {
				searchVO.setCreateCheck("");
			}

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			List<BrodScheduleInfoVO> list = brodSchedule.selectBrodScheduleStatusLst(searchVO);
			int totCnt = brodSchedule.selectBrodScheduleStatusPageCnt(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectplaySheduleStatus", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "우측 배포 매장 목록 조회", description = "특정 방송(brodCode)이 배포된 매장 목록을 조회합니다.")
	@GetMapping("/playSchedule/right.do")
	public ResultVO selectRightLst(@RequestParam("brodCode") String brodCode,
									@RequestParam(value = "rightSearchKeyword", required = false, defaultValue = "") String rightSearchKeyword,
									HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			BrodScheduleInfoVO searchVO = new BrodScheduleInfoVO();
			searchVO.setBrodCode(brodCode);
			if (!rightSearchKeyword.isBlank()) {
				searchVO.setSearchKeyword(rightSearchKeyword);
			}

			ResultHelper.setSuccess(resultVO, brodSchedule.selectBrodRigthLst(searchVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectRightLst", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장별 배포 건수 확인")
	@GetMapping("/playSchedule/centerCnt.do")
	public ResultVO schCountCheck(@RequestParam("brodCode") String brodCode,
								   @RequestParam("centerId") String centerId,
								   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			BrodScheduleInfo vo = new BrodScheduleInfo();
			vo.setBrodCode(brodCode);
			vo.setCenterId(centerId);
			ResultHelper.setSuccess(resultVO, brodSchedule.selectBrodScheduleCnt(vo), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "schCountCheck", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "매장 배포 연결/해제", description = "checkVal=Y면 매장에 방송을 배포(전용 브랜치 콘텐츠 생성/연결)하고, N이면 배포를 해제(연결된 콘텐츠/기념일/방송 삭제 포함)합니다.")
	@PostMapping("/playSchedule/rightUpdate.do")
	public ResultVO scheduleUpdate(@RequestBody Map<String, String> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			String brodCode = body.getOrDefault("brodCode", "");
			String checkVal = body.getOrDefault("checkVal", "");
			String centerStartTime = body.getOrDefault("centerStartTime", "");
			String centerId = body.getOrDefault("centerId", "");
			String centerEndTime = body.getOrDefault("centerEndTime", "");

			BrodScheduleInfo vo = new BrodScheduleInfo();
			vo.setBrodCode(brodCode);
			vo.setCenterId(centerId);
			vo.setBrodDay("20991231");
			vo.setCenterStartTime(centerStartTime);
			vo.setCenterEndTime(centerEndTime);
			String preBrodCode = brodContent.selectBrodContentCenterPreBrodCode(centerId);

			int ret = 0;
			if ("Y".equals(checkVal)) {
				BrodContentInfo contentInfo = brodContent.selectBrodContentInfo(brodCode);
				contentInfo.setCenterId(centerId);

				List<BrodScheduleInfo> schInfoLst = brodSchedule.selectBrodScheduleCreateCheckList(vo);
				for (BrodScheduleInfo sch : schInfoLst) {
					BrodScheduleInfo voUpdate = new BrodScheduleInfo();
					voUpdate.setScheduleSeq(sch.getScheduleSeq());
					voUpdate.setCreateCheck("Y");
					brodSchedule.updateBrodSchedule(voUpdate);
				}

				String returnBrodCode = brodContentCheck(contentInfo, centerStartTime, centerEndTime, loginVO.getManagerId(), preBrodCode);
				if (!"E".equals(returnBrodCode)) {
					vo.setBrodCode(returnBrodCode);
					ret = brodSchedule.deleteBrodScheduleOther(vo);
					ret = brodSchedule.insertBrodSchedule(vo);
					ret = brodSchedule.updateCenterSchedule(vo);
				}
			} else {
				String targetBrodCode = brodContent.selectBrodContentCenterCheckBrodCode(centerId);
				if (targetBrodCode != null) {
					String basicBrodCode = brodContent.selectBrodContentBasicBrodCodePreBrodCode(targetBrodCode);
					if (!basicBrodCode.isEmpty()) {
						brodContent.updateBrodContentCenterCntMin(basicBrodCode);
					}
					brodDetail.deleteBrodContentBrodCode(targetBrodCode);
					anniverInfo.deleteBrodAnniverBrod(targetBrodCode);
					brodContent.deleteBrodContent(targetBrodCode);
				}
				ret = brodSchedule.deleteBrodSchedule(vo);
			}

			ResultHelper.setSuccess(resultVO, ret, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "scheduleUpdate", e, egovMessageSource);
		}
		return resultVO;
	}

	/**
	 * 매장 전용 방송(브랜치 콘텐츠)을 생성하거나 갱신한다.
	 * 이미 해당 매장에 연결된 방송이 없으면 신규 채번 후 복사, 있으면 기존 것을 갱신한다.
	 */
	private String brodContentCheck(BrodContentInfo vo, String centerStartTime, String centerEndTime, String managerId, String preBrodCode) throws Exception {
		String centerBrodCode = brodContent.selectBrodContentCenterCheckBrodCode(vo.getCenterId());

		try {
			CenterInfo centerInfo = centerInfoManageService.selectCenterInfoManageDetail(vo.getCenterId());
			String centerNm = centerInfo.getCenterNm();
			String oldBrodCode = vo.getBrodCode();

			int ret;
			if (centerBrodCode == null) {
				vo.setPrebrodCode(vo.getBrodCode());
				centerBrodCode = brodContent.generateBrodCode();
				vo.setBrodCode(centerBrodCode);
				vo.setBrodName(vo.getBrodName() + "_" + centerNm);
				ret = brodContent.insertBrodContentCenterBrodCodeCopy(vo);
			} else {
				vo.setBasicBrodCode(vo.getBrodCode());
				vo.setBrodCode(centerBrodCode);
				vo.setBrodName(vo.getBrodName() + "_" + centerNm);
				ret = brodContent.updateBrodContentCenter(vo);
				if (preBrodCode != null) {
					brodContent.updateBrodContentCenterCntMin(preBrodCode);
				}
			}
			ret = brodContent.updateBrodContentCenterCntPlus(oldBrodCode);

			if (ret > 0) {
				BrodContentDetail detail = new BrodContentDetail();
				detail.setPrebrodCode(oldBrodCode);
				detail.setBrodCode(centerBrodCode);
				detail.setFrstRegisterId(managerId);
				brodDetail.insertBrodContentCenterCopy(detail);

				BrodAnniversary anniver = new BrodAnniversary();
				anniver.setPrebrodCode(oldBrodCode);
				anniver.setBrodCode(centerBrodCode);
				anniver.setFrstRegisterId(managerId);
				anniverInfo.insertBrodAnniverCenterCopy(anniver);
				return centerBrodCode;
			}
			return "E";
		} catch (Exception e) {
			brodDetail.deleteBrodContentBrodCode(centerBrodCode);
			anniverInfo.deleteBrodAnniverBrod(centerBrodCode);
			log.debug("brodContentCheck error: {}", e.toString());
			return "E";
		}
	}
}
