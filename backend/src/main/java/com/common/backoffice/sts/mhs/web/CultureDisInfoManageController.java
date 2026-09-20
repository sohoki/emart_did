package com.common.backoffice.sts.mhs.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import com.common.backoffice.sts.mhs.modals.MhsClassInfo;
import com.common.backoffice.sts.mhs.modals.MhsClassInfoVO;
import com.common.backoffice.sts.mhs.modals.MhsMonitorInfo;
import com.common.backoffice.sts.mhs.modals.MhsMonitorInfoVO;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfo;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfoVO;
import com.common.backoffice.sts.mhs.service.MhsCenterInfoManageService;
import com.common.backoffice.sts.mhs.service.MhsClassInfoManageService;
import com.common.backoffice.sts.mhs.service.MhsMonitorInfoManageService;
import com.common.backoffice.sts.mhs.service.MhsViewConnInfoManageService;
import com.common.backoffice.use.modals.Group;
import com.common.backoffice.use.service.GroupManagerService;
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
 * 문화센터(MHS) 룸/강의/모니터/편성 관리 API.
 * 레거시(emart_cms3.2.1) egovframework.let.sts.mhs.web.CultureDisInfoManageController를
 * 참조해서 did_emart(REST + JWT + tb_mhs*, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 축소/변경된 부분:
 * - 세션 기반(LoginVO in HttpSession) → JWT(AuthHelper) 기반 인증. 원본의 authorCode/groupId는
 *   각각 loginVO.getRoleId()/loginVO.getPartId()로 대체(TB_ROLEINFO/TB_PARTINFO 마이그레이션 반영)
 * - JSP 뷰 반환 → ResultVO(JSON) 반환
 * - 신규 모니터(MHS_MONITORCD) 채번: DB 함수 FN_MHSMONITERID(2026-07-27 생성 확인)를 그대로 사용
 * - 신규 편성(MHS_CONNSEQ) 채번: mhsconn_seq 시퀀스(2026-07-27 생성) 기준 MyBatis
 *   selectKey/NEXTVAL로 채번
 * - 신규 강의(MHS_CLASSCD) 채번: 원본 EgovIdGnrService 빈이 미구성이라
 *   MhsClassInfoManageService.generateMhsClasscd()에서 애플리케이션 레벨로 대체 채번
 * - 신규 부서(GROUP_ID) 채번: DB 함수 FN_GROUPCODE()(2026-07-27 생성 확인, LETTNAUTHORGROUPINFO
 *   기준 — GroupManagerMapper의 INSERT 대상 테이블과 일치 확인)를 그대로 사용
 * - parentCenterInfo.do/centerUpdate.do/centerDelete.do/actionMhsCenter.do는 제외함 —
 *   MhsCenterManageMapper의 selectMhsComboList/selectMhsComboListMeber/insertMhsCenter/
 *   updateMhsCenter/deleteMhsCenter는 레거시 매퍼 XML에도 "미사용" 주석과 함께 구현되어 있지
 *   않았던(원본에서부터 죽어있던) 코드라서 신규 포팅 대상에서 제외함. 매장(센터) 자체의 CRUD는
 *   이미 CenterInfoManageController(/api/backoffice/sub/basicManage/cnt)에서 담당함
 * - monitorDetail.do(JSP 뷰 전용, monitorInfo.do와 동일 데이터)와 preView.do(JSP 뷰 전용,
 *   preViewJson.do와 동일 데이터의 뷰 버전)는 JSON API인 monitorInfo.do/preViewJson.do로 통합
 * - "문화센터 콘텐츠 관련 임시(수정예정)" 표시가 있던 mediaLst/conMutiList/conMutiDetail/
 *   conMutiUpdate/conMutiDel/conMutiView는 이번 작업에서 제외함 — sts.cnt 도메인(콘텐츠 관리)은
 *   별도로 sts/cnt/web 하위 전용 컨트롤러들(ContentFileInfoManageController 등)에서 이미
 *   온전한 형태로 다루고 있어 중복이며, 별도 작업으로 진행함
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/backoffice/sub/roomManage/mhs")
@Tag(name = "CultureDisInfoManageController", description = "문화센터(MHS) 룸/강의/모니터/편성 관리")
public class CultureDisInfoManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final MhsMonitorInfoManageService mhsMonitorInfoManageService;
	private final MhsClassInfoManageService mhsClassInfoManageService;
	private final MhsCenterInfoManageService mhsCenterInfoManageService;
	private final MhsViewConnInfoManageService viewConn;
	private final GroupManagerService groupManagerService;

	// ===================== 브랜드/매장 조회(콤보/필터용, 읽기전용) =====================

	@Operation(summary = "문화센터 브랜드(부서) 콤보 조회", description = "로그인 사용자 권한 범위의 문화센터 브랜드 계층 목록을 반환합니다.")
	@GetMapping("/brand/list.do")
	public ResultVO selectMhsBrandList(HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			CenterInfoVO searchVO = new CenterInfoVO();
			searchVO.setAuthorCode(loginVO.getRoleId());
			searchVO.setGroupId(loginVO.getPartId());

			ResultHelper.setSuccess(resultVO, mhsCenterInfoManageService.selectMhsBrandList(searchVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsBrandList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "문화센터 매장 콤보 조회", description = "브랜드코드(mhsBrandcd)에 속한 매장 목록을 반환합니다.")
	@GetMapping("/center/list.do")
	public ResultVO selectMhsCenterList(@RequestParam("mhsBrandcd") String mhsBrandcd,
										 @RequestParam(value = "mhsCentercd", required = false, defaultValue = "") String mhsCentercd,
										 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			CenterInfoVO searchVO = new CenterInfoVO();
			searchVO.setMhsBrandcd(mhsBrandcd);
			searchVO.setMhsCentercd(mhsCentercd);

			ResultHelper.setSuccess(resultVO, mhsCenterInfoManageService.selectMhsCenterList(searchVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsCenterList", e, egovMessageSource);
		}
		return resultVO;
	}

	// ===================== 모니터(재생 화면) 관리 =====================

	@Operation(summary = "MHS 모니터 리스트 조회")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/monitor/list.do")
	public ResultVO selectMhsMonitorListByPagination(@RequestBody MhsMonitorInfoVO searchVO,
													  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthorCode(loginVO.getRoleId());
			// ROLE_MHS_USER(개별 매장 담당자)만 자기 매장/부서 범위를 벗어날 수 없도록 로그인 정보로
			// 강제 고정한다(클라이언트가 보낸 mhsBrandcd/mhsCentercd 무시 — 보안 스코프).
			// 그 외 권한(통합관리자 등)은 화면 상단에서 선택한 mhsBrandcd/mhsCentercd를 그대로 검색
			// 필터로 사용한다 — 이전에는 여기서 무조건 덮어써서 상단 브랜드/매장 선택이 목록 조회에
			// 전혀 반영되지 않았음(매퍼 selectMhsMonitorList의 ROLE_MHS_USER 전용 분기도 함께 수정).
			if ("ROLE_MHS_USER".equals(loginVO.getRoleId())) {
				searchVO.setGroupId(loginVO.getPartId());
				searchVO.setMhsCentercd(loginVO.getCenterId());
			}

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

			List<MhsMonitorInfoVO> list = mhsMonitorInfoManageService.selectMhsMonitorList(searchVO);
			int totCnt = mhsMonitorInfoManageService.selectMhsMonitorListCnt(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsMonitorListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 모니터 상세 조회")
	@GetMapping("/monitor/{mhsMonitorcd}.do")
	public ResultVO selectMhsMonitorInfo(@Parameter(description = "모니터 코드") @PathVariable("mhsMonitorcd") String mhsMonitorcd,
										  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			MhsMonitorInfoVO detail = mhsMonitorInfoManageService.selectMhsMonitorInfo(mhsMonitorcd);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsMonitorInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 모니터 콤보 조회", description = "매장코드(mhsCentercd)에 속한 모니터 목록을 반환합니다.")
	@GetMapping("/monitor/combo.do")
	public ResultVO selectMonitorCombo(@RequestParam("mhsCentercd") String mhsCentercd,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, mhsMonitorInfoManageService.selectMhsMonitorCombo(mhsCentercd), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMonitorCombo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 모니터 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/monitor/update.do")
	public ResultVO updateMhsMonitorInfo(@RequestBody MhsMonitorInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
			if (isInsert) {
				vo.setMhsMonitorcd(mhsMonitorInfoManageService.generateMhsMonitorcd(vo.getMhsCentercd()));
				vo.setMhsMregid(loginVO.getManagerId());
			} else {
				vo.setMhsMupdateid(loginVO.getManagerId());
			}

			int ret = mhsMonitorInfoManageService.updateMhsMonitorInfo(vo);
			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateMhsMonitorInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 모니터 삭제")
	@DeleteMapping("/monitor/{mhsMonitorcd}.do")
	public ResultVO deleteMoniter(@Parameter(description = "모니터 코드") @PathVariable("mhsMonitorcd") String mhsMonitorcd,
								   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = mhsMonitorInfoManageService.deleteMhsMonitorInfo(mhsMonitorcd);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteMoniter", e, egovMessageSource);
		}
		return resultVO;
	}

	// ===================== 강의(클래스) 관리 =====================

	@Operation(summary = "MHS 강의 리스트 조회")
	@PostMapping("/class/list.do")
	public ResultVO selectMhsClassListByPagination(@RequestBody MhsClassInfoVO searchVO,
													HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthorCode(loginVO.getRoleId());
			searchVO.setMhsCentercd(loginVO.getCenterId());

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

			List<MhsClassInfoVO> list = mhsClassInfoManageService.selectMhsClassList(searchVO);
			int totCnt = mhsClassInfoManageService.selectMhsClassListCnt(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			resultMap.put(Globals.PAGE_INFO, paginationInfo);
			ResultHelper.setSuccess(resultVO, resultMap);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsClassListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 강의 상세 조회")
	@GetMapping("/class/{mhsClasscd}.do")
	public ResultVO selectMhsClassInfo(@Parameter(description = "강의 코드") @PathVariable("mhsClasscd") String mhsClasscd,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			MhsClassInfo detail = mhsClassInfoManageService.selectMhsClassInfo(mhsClasscd);
			if (detail != null) {
				ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
			} else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsClassInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 강의실(모니터) 배정용 강의 콤보 조회", description = "브랜드/매장코드로 진행 중인(종료일 미경과) 강의 목록을 반환합니다.")
	@PostMapping("/class/combo.do")
	public ResultVO selectMhsMoniterClassList(@RequestBody MhsClassInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, mhsClassInfoManageService.selectMhsMoniterClassList(vo), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsMoniterClassList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 강의 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
	@PostMapping("/class/update.do")
	public ResultVO updateMhsClassInfo(@RequestBody MhsClassInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			boolean isInsert = "Ins".equals(vo.getMode());
			if (isInsert) {
				vo.setMhsClasscd(mhsClassInfoManageService.generateMhsClasscd());
				vo.setMhsRegid(loginVO.getManagerId());
			} else {
				vo.setMhsUpdateid(loginVO.getManagerId());
			}

			int ret = mhsClassInfoManageService.updateMhsClassInfo(vo);
			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateMhsClassInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 강의 삭제")
	@DeleteMapping("/class/{mhsClasscd}.do")
	public ResultVO deleteMhsClass(@Parameter(description = "강의 코드") @PathVariable("mhsClasscd") String mhsClasscd,
									HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = mhsClassInfoManageService.deleteMhsClassInfo(mhsClasscd);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteMhsClass", e, egovMessageSource);
		}
		return resultVO;
	}

	// ===================== 편성(모니터-강의 시간표 연결) 관리 =====================

	@Operation(summary = "MHS 편성 리스트 조회", description = "특정 모니터(mhsMonitorcd)에 편성된 강의 목록을 조회합니다. searchDay(YYYYMMDD) 지정 시 해당 요일에 편성된 강의만 반환합니다.")
	@PostMapping("/viewConn/list.do")
	public ResultVO selectViewConnInfo(@RequestBody MhsViewConnInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, viewConn.selectViewMoniterClassInfo(searchVO), Globals.JSON_RETURN_RESULT_LIST);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectViewConnInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 모니터 편성 미리보기(현재/다음 강의)", description = "특정 모니터의 현재 진행 중/다음 예정 강의와 모니터 정보를 함께 반환합니다.")
	@GetMapping("/viewConn/preview.do")
	public ResultVO preViewJson(@RequestParam("mhsMonitorcd") String mhsMonitorcd,
								 @RequestParam(value = "searchDay", required = false) String searchDay,
								 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, buildPreviewResult(mhsMonitorcd, searchDay));
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "preViewJson", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 모니터 단말 화면(공개 API, 인증 불필요)",
			description = "실제 문화센터 룸 단말(안드로이드 키오스크)이 로그인 없이 주기적으로 폴링하는 공개 API. "
					+ "preViewJson.do와 데이터는 동일하나 인증을 요구하지 않는다(SecurityConfig.AUTH_GET_WHITELIST에 등록 필요). "
					+ "DID 장비의 /equiManage/pic/capture.do와 동일한 취지의 기계 간 통신 API.")
	@GetMapping("/device/preview.do")
	public ResultVO devicePreview(@RequestParam("mhsMonitorcd") String mhsMonitorcd,
								   @RequestParam(value = "searchDay", required = false) String searchDay,
								   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			ResultHelper.setSuccess(resultVO, buildPreviewResult(mhsMonitorcd, searchDay));
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "devicePreview", e, egovMessageSource);
		}
		return resultVO;
	}

	// preViewJson.do(관리자 미리보기)/device/preview.do(단말 공개 API)가 공유하는 조회 로직.
	private Map<String, Object> buildPreviewResult(String mhsMonitorcd, String searchDay) throws Exception {
		if (searchDay == null || searchDay.isBlank()) {
			searchDay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
		}

		MhsViewConnInfoVO conn = new MhsViewConnInfoVO();
		conn.setSearchDay(searchDay);
		conn.setMhsMonitorcd(mhsMonitorcd);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("monitorInfo", mhsMonitorInfoManageService.selectMhsMonitorInfo(mhsMonitorcd));
		resultMap.put(Globals.JSON_RETURN_RESULT_LIST, viewConn.selectViewMoniterClassInfo(conn));
		resultMap.put("pageInfo", viewConn.selectViewMoniterClassUninPageInfo(conn));
		return resultMap;
	}

	@Operation(summary = "MHS 편성 등록", description = "모니터에 강의를 편성(연결)합니다.")
	@PostMapping("/viewConn/insert.do")
	public ResultVO insertViewConnInfo(@RequestBody MhsViewConnInfo vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			vo.setMhsDataregid(loginVO.getManagerId());

			int ret = viewConn.insertMoniterClassInfo(vo);
			ResultHelper.setCudResult(resultVO, ret, "success.common.insert", egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "insertViewConnInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 편성 삭제")
	@DeleteMapping("/viewConn/{mhsConnSeq}.do")
	public ResultVO viewConnDelete(@Parameter(description = "편성 일련번호") @PathVariable("mhsConnSeq") String mhsConnSeq,
									HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = viewConn.deleteMoniterClassInfo(mhsConnSeq);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "viewConnDelete", e, egovMessageSource);
		}
		return resultVO;
	}

	// ===================== 조직(부서) 관리 =====================

	@Operation(summary = "MHS 조직(부서) 상세 조회")
	@GetMapping("/group/{groupId}.do")
	public ResultVO selectMhsGroupInfo(@Parameter(description = "부서 ID") @PathVariable("groupId") String groupId,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, groupManagerService.selectGroupManageDetail(groupId), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectMhsGroupInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 조직(부서) 등록/수정", description = "mode=Ins면 신규 등록(MHSYN='Y'로 생성), 그 외에는 수정합니다.")
	@PostMapping("/group/update.do")
	public ResultVO updateMhsGroupInfo(@RequestBody Group vo, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			boolean isInsert = "Ins".equals(vo.getMode());
			int ret;
			if (isInsert) {
				vo.setGroupId(groupManagerService.generateGroupId());
				ret = groupManagerService.insertGroupManageMhs(vo);
			} else {
				ret = groupManagerService.updateGroupManage(vo);
			}

			String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
			ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
			}
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateMhsGroupInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "MHS 조직(부서) 삭제")
	@DeleteMapping("/group/{groupId}.do")
	public ResultVO deleteMhsGroupInfo(@Parameter(description = "부서 ID") @PathVariable("groupId") String groupId,
										HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = groupManagerService.deleteGroupManage(groupId);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteMhsGroupInfo", e, egovMessageSource);
		}
		return resultVO;
	}
}
