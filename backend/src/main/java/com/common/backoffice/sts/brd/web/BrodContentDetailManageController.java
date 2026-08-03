package com.common.backoffice.sts.brd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.brd.modals.BrodContentDetail;
import com.common.backoffice.sts.brd.modals.BrodContentDetailTime;
import com.common.backoffice.sts.brd.modals.BrodContentDetailVO;
import com.common.backoffice.sts.brd.modals.BrodContentInfo;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfo;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfoVO;
import com.common.backoffice.sts.brd.service.BrodContentDetailManagerService;
import com.common.backoffice.sts.brd.service.BrodContentDetailTimeManagerService;
import com.common.backoffice.sts.brd.service.BrodContentInfoManageService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 방송 콘텐츠 상세(편성 파일) 관리 API.
 * did_emart(REST + JWT + tb_brodcontentdetail, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 세션 기반 LoginVO → JWT(AuthHelper) 기반 인증. getMberId() → getManagerId(), getAuthorCode()
 *   → getRoleId()로 매핑(기존 컨트롤러들과 동일 패턴)
 * - JSP 뷰/팝업 반환 → ResultVO(JSON) 반환
 * - BROD_SEQ(TB_BRODCONTENTDETAIL)/IMSI_SEQ(TB_BRODIMSI) 채번용 시퀀스(brodcondetail_seq/
 *   brodconimsi_seq)가 DB에 없어 db-encoding-fix/08_create_missing_sequences.sql로 신규
 *   생성함. 매퍼 SQL 자체는 원본처럼 NEXTVAL(...)을 그대로 사용하고 있어(다건 복사 INSERT...SELECT
 *   포함) 애플리케이션 코드 변경은 필요 없었음
 * - schUpdate()(콘텐츠 변경 시 배포 스케줄 재계산)는 방송 배포(스케줄) 도메인 핵심 로직이라
 *   그대로 포팅함(BrodScheduleManagerService 연동)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/brodManage")
@Tag(name = "BrodContentDetailManageController", description = "방송 콘텐츠 상세(편성 파일) 관리")
public class BrodContentDetailManageController {

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	private final BrodContentDetailManagerService brodDetail;
	private final BrodContentInfoManageService brodContent;
	private final ContentFileInfoManageService conFileService;
	private final BrodScheduleManagerService brodSchedule;
	private final BrodContentDetailTimeManagerService brodTime;

	@Operation(summary = "편성용 음원 파일 검색", description = "MUSIC 타입 파일을 이름으로 검색합니다(최대 100건).")
	@GetMapping("/contentDetail/fileSearch.do")
	public ResultVO selectFileSearch(@RequestParam(value = "orgFileNm", required = false, defaultValue = "") String orgFileNm,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			ContentFileInfoVO searchVO = new ContentFileInfoVO();
			searchVO.setAuthorCode(loginVO.getRoleId());
			searchVO.setMberId(loginVO.getManagerId());
			searchVO.setSearchCondition("orignlFileNm");
			searchVO.setSearchKeyword(orgFileNm);
			searchVO.setMediaType("MUSIC");
			searchVO.setFirstIndex(0);
			searchVO.setRecordCountPerPage(100);
			searchVO.setNotConType("");
			searchVO.setFileGubun("");

			ResultHelper.setSuccess(resultVO, conFileService.selectFilePageListByPagination(searchVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectFileSearch", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 편성 등록/수정 화면용 데이터 조회", description = "시간 콤보/음원 콤보와, 수정 모드일 때 기존 편성 상세를 함께 반환합니다.")
	@GetMapping("/contentDetail/form.do")
	public ResultVO selectBrodContentDetail(@RequestParam("brodCode") String brodCode,
											 @RequestParam(value = "brodSeq", required = false, defaultValue = "") String brodSeq,
											 @RequestParam(value = "mode", required = false, defaultValue = "Ins") String mode,
											 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("timeInfo", brodDetail.selectTimeCombo(brodContent.selectBrodContentTimeInfoChar(brodCode)));
			resultMap.put("fileInfo", conFileService.selectFileListCombo());

			if ("Edt".equals(mode)) {
				resultMap.put("regist", brodDetail.selectBrodContenDetailt(brodSeq));
			}
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodContentDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "동일 시간대 중복 편성 건수 확인")
	@GetMapping("/contentDetail/checkCnt.do")
	public ResultVO selectContentRegCheck(@RequestParam("brodCode") String brodCode,
										   @RequestParam("timeInterval") String timeInterval,
										   @RequestParam("atchFileId") String atchFileId,
										   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			BrodContentDetail vo = new BrodContentDetail();
			vo.setBrodCode(brodCode);
			vo.setIntervalSection(timeInterval);
			vo.setAtchFileId(atchFileId);

			ResultHelper.setSuccess(resultVO, brodDetail.selectContentRegCnt(vo), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectContentRegCheck", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "다른 방송의 편성 복사용 콤보 조회")
	@GetMapping("/contentDetail/copy/{brodCode}.do")
	public ResultVO viewBrodContentDetailCopy(@Parameter(description = "방송 코드") @PathVariable String brodCode,
											   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, brodContent.selectBrodContentCopy(brodCode), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "viewBrodContentDetailCopy", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "다른 방송의 편성 복사 실행", description = "기존 편성을 지우고 prebrodCode의 편성을 복사해 넣습니다.")
	@PostMapping("/contentDetail/copyInsert.do")
	public ResultVO contentDetailCopy(@RequestBody BrodContentDetailVO vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			brodDetail.deleteBrodContentBrodCode(vo.getBrodCode());
			int ret = brodDetail.insertBrodContentCopy(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.insert", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "contentDetailCopy", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "분 간격 자동 편성 시간대 계산",
			description = "지정한 간격/횟수만큼 겹치지 않는 시간대를 자동으로 찾아 임시 편성(TB_BRODIMSI)에 등록하고, 확정된 시간대 목록(콤마구분)을 반환합니다.")
	@PostMapping("/contentDetail/timeCheck.do")
	public ResultVO selectContentTimeCheck(@RequestBody Map<String, String> body, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String brodCode = body.getOrDefault("brodCode", "");
			String timeInterval = body.getOrDefault("timeInterval", "");
			int timeIntervalInsertCnt = Integer.parseInt(body.getOrDefault("timeIntervalInsertCnt", "0"));
			String contentInsertInterval = body.getOrDefault("contentInsertInterval", "0");
			String contentStartDay = body.getOrDefault("contentStartDay", "");
			String contentEndDay = body.getOrDefault("contentEndDay", "");
			String atchFileId = body.getOrDefault("atchFileId", "");

			BrodContentDetail vo = new BrodContentDetail();
			vo.setBrodCode(brodCode);
			vo.setAtchFileId(atchFileId);
			String timeIntervalResult = "";

			for (int i = 0; i < timeIntervalInsertCnt; i++) {
				timeInterval = (i == 0) ? timeInterval : String.valueOf(Integer.parseInt(timeInterval) + Integer.parseInt(contentInsertInterval));
				if (Integer.parseInt(timeInterval) > 50) {
					break;
				}
				vo.setIntervalSection(lenReplace(timeInterval, 3));

				if (brodDetail.selectContentRegTimeImsiOverTableCheck(vo) > 600) {
					vo.setIntervalSection(lenReplace(String.valueOf(Integer.parseInt(timeInterval) - 10), 3));
					if (brodDetail.selectContentRegTimeImsiOverTableCheck(vo) > 600 || Integer.parseInt(vo.getIntervalSection()) < 0) {
						vo.setIntervalSection(lenReplace(String.valueOf(Integer.parseInt(timeInterval) + 20), 3));
						if (brodDetail.selectContentRegTimeImsiOverTableCheck(vo) > 600 || Integer.parseInt(vo.getIntervalSection()) > 50) {
							log.debug("selectContentTimeCheck: 배정 가능한 시간대 없음(brodCode={})", brodCode);
							break;
						} else {
							timeIntervalResult = timeIntervalResult + lenReplace(vo.getIntervalSection(), 3) + ",";
						}
					} else {
						timeIntervalResult = timeIntervalResult + lenReplace(vo.getIntervalSection(), 3) + ",";
					}
				} else {
					vo.setIntervalSection(lenReplace(timeInterval, 3));
					timeIntervalResult = timeIntervalResult + lenReplace(timeInterval, 3) + ",";
				}

				BrodContentDetailTime imsiTime = new BrodContentDetailTime();
				imsiTime.setBrodCode(brodCode);
				imsiTime.setAtchFileId(atchFileId);
				imsiTime.setIntervalSection(vo.getIntervalSection());
				imsiTime.setContentStartDay(contentStartDay);
				imsiTime.setContentEndDay(contentEndDay);
				brodTime.insertBrodContentDetailTime(imsiTime);
			}

			brodTime.deleteBrodContentDetailTimeBrodCode(brodCode);
			ResultHelper.setSuccess(resultVO, timeIntervalResult, Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectContentTimeCheck", e, egovMessageSource);
		}
		return resultVO;
	}

	private String lenReplace(String txt, int len) {
		if (txt.length() == len) {
			return txt;
		} else if (txt.equals("0")) {
			return "0".repeat(len);
		} else {
			return "0" + txt;
		}
	}

	@Operation(summary = "콘텐츠 편성 삭제", description = "기본(연동) 편성이면 연동 관계도 함께 정리하고, 배포 스케줄을 재계산합니다.")
	@DeleteMapping("/contentDetail/{brodSeq}.do")
	public ResultVO deleteContentDetailDel(@Parameter(description = "편성 순번") @PathVariable String brodSeq,
											@RequestParam("brodCode") String brodCode,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String basicBrodCode = brodContent.selectBrodContentBasicBrodCode(brodCode);
			if (basicBrodCode != null) {
				brodContent.updateBrodBasicCodeCntMin(brodCode);
				BrodContentInfo contentInfo = brodContent.selectBrodContentInfo(brodCode);
				contentInfo.setBasicBrodCode("");
				contentInfo.setBrodName(contentInfo.getCenterNm());
				brodContent.updateBrodContentBasicInfoName(contentInfo);
			}

			BrodContentDetail vo = new BrodContentDetail();
			vo.setBrodSeq(brodSeq);
			vo.setBrodCode(brodCode);
			brodDetail.deleteContentDetailBasicContent(vo);

			int ret = brodDetail.deleteBrodContentDetail(brodSeq);
			schUpdate(brodCode);

			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteContentDetailDel", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "콘텐츠 편성 등록/수정",
			description = "mode=Ins면 timeIntervalResult(콤마구분 시간대들)마다 각각 등록, 그 외에는 단건 수정합니다. 등록/수정 후 배포 스케줄을 재계산합니다.")
	@PostMapping("/contentDetail/update.do")
	public ResultVO selectBrodContentDetailUpdate(@RequestBody BrodContentDetail vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			int ret;
			if ("Ins".equals(vo.getMode())) {
				String timeIntervalResult = vo.getTimeIntervalResult();
				if (timeIntervalResult != null && timeIntervalResult.endsWith(",")) {
					timeIntervalResult = timeIntervalResult.substring(0, timeIntervalResult.length() - 1);
				}
				String[] timeArray = (timeIntervalResult == null || timeIntervalResult.isBlank())
						? new String[0] : timeIntervalResult.split(",");

				ret = 0;
				for (String interval : timeArray) {
					vo.setIntervalSection(interval);
					ret = brodDetail.insertBrodContentDetail(vo);
				}

				String basicBrodCode = brodContent.selectBrodContentBasicBrodCode(vo.getBrodCode());
				if (basicBrodCode != null) {
					brodContent.updateBrodBasicCodeCntMin(vo.getBrodCode());
					BrodContentInfo contentInfo = brodContent.selectBrodContentInfo(vo.getBrodCode());
					contentInfo.setBasicBrodCode("");
					contentInfo.setBrodName(contentInfo.getCenterNm());
					brodContent.updateBrodContentBasicInfoName(contentInfo);
				} else {
					brodDetail.deleteBrodBasicBrod(vo.getBrodCode());
					brodDetail.insertBrodContentScheduleOtherCopy(vo.getBrodCode());
				}
			} else {
				vo.setLastUpdusrId(loginVO.getManagerId());
				ret = brodDetail.updateBrodContentDetail(vo);
			}

			String scheduleUpdateResult = schUpdate(vo.getBrodCode());

			String successMsgKey = "Ins".equals(vo.getMode()) ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultMap.put("scheduleUpdateResult", scheduleUpdateResult);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBrodContentDetailUpdate", e, egovMessageSource);
		}
		return resultVO;
	}

	/** 콘텐츠 등록/수정/삭제 시 연결된 배포 스케줄(TB_BRODSCHEDULE)을 재계산한다. */
	private String schUpdate(String brodCode) throws Exception {
		int ret = 0;
		List<BrodContentInfo> contentInfo = brodContent.selectBrodContentBasicContent(brodCode);

		for (BrodContentInfo info : contentInfo) {
			if (info.getCenterId() != null && !info.getBrodCode().equals("")) {
				BrodScheduleInfo schInfoCheck = new BrodScheduleInfo();
				schInfoCheck.setBrodCode(info.getBrodCode());
				schInfoCheck.setCenterId(info.getCenterId());
				if (brodSchedule.selectBrodScheduleCnt(schInfoCheck) < 1) {
					schInfoCheck.setCreateCheck("N");
					schInfoCheck.setBrodDay("20991231");
					ret = brodSchedule.insertBrodSchedule(schInfoCheck);
				}
			}

			List<BrodScheduleInfoVO> schedule = brodSchedule.selectBrodScheduleUpdateChanage(info.getBrodCode());
			BrodScheduleInfo scheduleInfo = new BrodScheduleInfo();
			for (int i = 0; i < schedule.size(); i++) {
				scheduleInfo.setBrodCode(info.getBrodCode());
				scheduleInfo.setCreateCheck("N");
				scheduleInfo.setCenterId(schedule.get(i).getCenterId());
				scheduleInfo.setBrodDay(schedule.get(i).getBrodDay());
				scheduleInfo.setScheduleSeq(schedule.get(i).getScheduleSeq());

				if ("N".equals(schedule.get(i).getCreateCheck())) {
					ret = brodSchedule.updateBrodSchedule(scheduleInfo);
				} else {
					ret = brodSchedule.deleteBrodScheduleSeq(schedule.get(i).getScheduleSeq());
					if (schedule.size() - 1 == i) {
						ret = brodSchedule.insertBrodSchedule(scheduleInfo);
					}
				}
			}

			scheduleInfo.setCreateCheck("N");
			if (brodSchedule.selectBrodScheduleStateCnt(scheduleInfo) > 0) {
				brodSchedule.deleteBrodScheduleState(scheduleInfo);
			}
		}

		if (ret > 0) {
			BrodContentInfo voinfo = new BrodContentInfo();
			voinfo.setBrodCode(brodCode);
			voinfo.setBrodChangeInfo("Y");
			brodContent.updateBrodContentSchChange(voinfo);
			return "O";
		} else {
			return "F";
		}
	}
}
