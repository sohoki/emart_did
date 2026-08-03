package com.common.backoffice.sym.did.web;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import com.common.backoffice.bas.cnt.service.CenterInfoManageService;
import com.common.backoffice.bas.code.service.EgovCcmCmmnDetailCodeManageService;
import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.sts.snd.modals.SendMsgInfo;
import com.common.backoffice.sts.snd.service.SendMsgInfoManageService;
import com.common.backoffice.sts.xml.service.XmlInfoManageService;
import com.common.backoffice.sym.did.modals.DidInfo;
import com.common.backoffice.sym.did.modals.DidInfoVO;
import com.common.backoffice.sym.did.service.DidInfoManageService;
import com.common.backoffice.sym.grp.modals.GroupDidInfo;
import com.common.backoffice.sym.grp.modals.GroupInfoVO;
import com.common.backoffice.sym.grp.service.GroupDidInfoManageService;
import com.common.backoffice.sym.grp.service.GroupInfoManageService;
import com.common.backoffice.sym.sch.service.ScheduleInfoManageService;
import com.common.backoffice.use.modals.GroupVo;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * DID(단말기) 관리 API.
 * did_emart(REST + JWT + tb_didinfo, postgresql) 기준으로 리팩토링함.
 *
 * 원본(1193줄) 대비 변경된 부분:
 * - 원본은 클래스에 @RestController가 붙어 있었으나 메서드 본문은 세션 기반(HttpSession의
 *   LoginVO), ModelMap/ModelAndView, JSP 뷰 이름 반환이 뒤섞인 반쯤 변환된 상태였음(예:
 *   selectDidManagerInfoManageListByPagination은 반환 타입이 ResultVO인데 본문 마지막엔
 *   JSP 경로 문자열을 return하고 있었음 — 컴파일 자체가 안 되는 상태). 전부 JWT(AuthHelper)/
 *   ResultVO(JSON) 기준으로 전면 재작성
 * - 원본 세션 LoginVO의 getAuthorCode()/getMberId()/getGroupId()/getParentGroupId()는 각각
 *   loginVO.getRoleId()/getManagerId()/getPartId()로 매핑. parentGroupId는 새 JWT LoginVO에
 *   대응 개념이 없어(13절 SendMsgInfoManageController와 동일 판단) 항상 미적용으로 둠
 * - @Slf4j가 생성하는 필드명은 log인데 원본 전체가 LOGGER.debug/info를 쓰고 있어 컴파일이
 *   안 됐음 — 전부 log로 교체
 * - CenterInfoVO/GroupInfoVO/GroupVo/GroupDidInfo import가 아예 빠져 있어 컴파일이 안 됐음 —
 *   추가
 * - `/backoffice/test.do`(아무 것도 안 하는 테스트용 더미 엔드포인트, DidInfoManageService.test()가
 *   0을 리턴할 뿐)는 제외
 * - `/backoffice/sub/equiManage/integrate.do`는 실제 조회 로직이 전부 주석 처리된 죽은 코드였고
 *   (통합관리 화면의 역할/매장/장비 리스트는 이미 별도 AJAX 엔드포인트로 대체되어 있음), 매장/그룹
 *   콤보만 중복 제공하던 부분이라 제외 — 아래 4개 엔드포인트(roleCenterInfo/integrateCenterList/
 *   integrateEquipList/integrateEquipInfo)가 이미 그 실제 데이터를 각각 담당함
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/sub/equiManage/did")
@Tag(name = "DidInfoManageController", description = "DID(단말기) 관리")
public class DidInfoManageController {

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	private final DidInfoManageService didInfoManageService;
	private final EgovCcmCmmnDetailCodeManageService cmmnDetailCodeManageService;
	private final GroupInfoManageService groupInfoManageService;
	private final GroupManagerService groupManagerService;
	private final SendMsgInfoManageService sendMsgInfo;
	private final CenterInfoManageService centerInfoManageService;
	private final ScheduleInfoManageService scheduleManagerService;
	private final XmlInfoManageService xmlService;
	private final GroupDidInfoManageService groupDidInfoManageService;

	@Operation(summary = "DID 종료시간 수정", description = "종료시간을 갱신하고 단말에 SP_DIDENDTIME 명령을 발송합니다.")
	@PatchMapping("/{didId}/endTime.do")
	public ResultVO updateDidTime(@Parameter(description = "DID ID") @PathVariable String didId,
								   @RequestParam String didMac,
								   @RequestParam String didEndTime,
								   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			DidInfo vo = new DidInfo();
			vo.setDidId(didId);
			vo.setDidMac(didMac);
			vo.setDidEndTime(didEndTime);
			int ret = didInfoManageService.updateDidEndTime(vo);

			SendMsgInfo sminfo = new SendMsgInfo();
			sminfo.setDidId(didId);
			sminfo.setDidMacAddress(didMac);
			sminfo.setXmlProcessName("SP_DIDENDTIME");
			sminfo.setSendResult("N");
			int ret1 = sendMsgInfo.insertSendMsgInfoManage(sminfo);

			ResultHelper.setCudResult(resultVO, (ret > 0 && ret1 > 0) ? 1 : 0, "success.common.update", "fail.common.update", egovMessageSource);
		} catch (Exception e) {
			log.error("updateDidTime error: {}", e.toString());
			ResultHelper.setFailResult(resultVO, "updateDidTime", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 다건 재시작/재다운로드 명령 발송", description = "restartInfo는 선행 구분자 1자 + \"didId|didMac\" 콤마 목록입니다.")
	@PostMapping("/restart.do")
	public ResultVO didRestInfo(@RequestParam String restartInfo,
								 @RequestParam String xmlProceNm,
								 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = 0;
			String[] didIdArray = restartInfo.substring(1).split(",");
			SendMsgInfo sminfo = new SendMsgInfo();
			for (String entry : didIdArray) {
				String[] didInfo = entry.split("\\|");
				try {
					sminfo.setDidId(didInfo[0]);
					sminfo.setDidMacAddress(didInfo[1]);
					sminfo.setXmlProcessName("RESTART".equals(xmlProceNm) ? "SP_DIDREBOOT" : "SP_REDOWN");
					sminfo.setSendResult("N");
					ret = sendMsgInfo.insertSendMsgInfoManage(sminfo);
				} catch (Exception e) {
					log.error("didRestInfo entry error: {}", e.toString());
				}
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", "fail.common.update", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "didRestInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "그룹/스케줄 변경 재전송", description = "선택한 DID들에 대해 ON/OFF/스케줄변경/SW변경 명령을 재전송합니다.")
	@PostMapping("/didChange.do")
	public ResultVO xmlUpdateDid(@RequestBody Map<String, String> params, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String schCode = params.getOrDefault("didIds", "");
			String updateCode = params.getOrDefault("updateCode", "");
			String procesGubun = params.getOrDefault("procesGubun", "");

			int ret = 0;
			for (String didId : schCode.split(",")) {
				SendMsgInfo sminfo = new SendMsgInfo();
				sminfo.setDidId(didId.trim());
				String didMac = didInfoManageService.selectDIDMac(didId.trim());

				if (didMac != null && !"N".equals(didMac)) {
					sminfo.setDidMacAddress(didMac);
					switch (procesGubun) {
						case "ON" -> sminfo.setXmlProcessName("SP_DIDREBOOT");
						case "OFF" -> sminfo.setXmlProcessName("SP_DIDOFF");
						case "SCH", "SWF" -> sminfo.setXmlProcessName(xmlService.selectDIDProcessNm(updateCode));
						default -> { }
					}
					sminfo.setSendResult("N");
					ret = sendMsgInfo.insertSendMsgInfoManage(sminfo);
					if (ret == 0) break;
				}
			}
			ResultHelper.setCudResult(resultVO, ret, "success.common.update", "fail.common.update", egovMessageSource);
		} catch (Exception e) {
			log.error("xmlUpdateDid error: {}", e.toString());
			ResultHelper.setFailResult(resultVO, "xmlUpdateDid", e, egovMessageSource);
		}
		return resultVO;
	}

	private ResultVO sendSimpleCommand(String didId, String didMac, String xmlProcessName) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			SendMsgInfo sminfo = new SendMsgInfo();
			sminfo.setDidId(didId);
			sminfo.setDidMacAddress(didMac);
			sminfo.setXmlProcessName(xmlProcessName);
			sminfo.setSendResult("N");

			int ret1 = sendMsgInfo.insertSendMsgInfoManage(sminfo);
			ResultHelper.setCudResult(resultVO, ret1, "success.common.update", "fail.common.update", egovMessageSource);
		} catch (Exception e) {
			log.error("{} error: {}", xmlProcessName, e.toString());
			ResultHelper.setFailResult(resultVO, xmlProcessName, e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 캡처화면 요청 명령 발송")
	@PostMapping("/capture.do")
	public ResultVO updateDidCapture(@RequestParam String didId, @RequestParam String didMac, HttpServletRequest request) {
		return sendSimpleCommand(didId, didMac, "SP_DIDMONITER");
	}

	@Operation(summary = "DID 재부팅 명령 발송")
	@PostMapping("/reboot.do")
	public ResultVO updateDidReboot(@RequestParam String didId, @RequestParam String didMac, HttpServletRequest request) {
		return sendSimpleCommand(didId, didMac, "SP_DIDREBOOT");
	}

	@Operation(summary = "DID 재시작 명령 발송")
	@PostMapping("/reset.do")
	public ResultVO updateDidReStart(@RequestParam String didId, @RequestParam String didMac, HttpServletRequest request) {
		return sendSimpleCommand(didId, didMac, "SP_DIDRESTART");
	}

	@Operation(summary = "DID 콘텐츠 재다운로드 명령 발송")
	@PostMapping("/contentRedown.do")
	public ResultVO updateContentRedown(@RequestParam String didId, @RequestParam String didMac, HttpServletRequest request) {
		return sendSimpleCommand(didId, didMac, "SP_REDOWN");
	}

	@Operation(summary = "DID 종료 명령 발송")
	@PostMapping("/shutdown.do")
	public ResultVO updateDidShutdown(@RequestParam String didId, @RequestParam String didMac, HttpServletRequest request) {
		return sendSimpleCommand(didId, didMac, "SP_DIDSHUTDOWN");
	}

	@Operation(summary = "DID 원격 전원켜기(Wake-on-LAN)")
	@PostMapping("/wakeOn.do")
	public ResultVO pcWakeOn(@RequestParam String didId, @RequestParam String didMac, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String macStrseq = "";
			if (didMac.length() == 12) {
				for (int i = 0; i < (didMac.length() / 2); i++) {
					macStrseq += didMac.substring((i * 2), ((i * 2) + 2)) + "-";
				}
			}

			byte[] macBytes = getMacBytes(macStrseq.substring(0, (macStrseq.length() - 1)));
			byte[] bytes = new byte[6 + 16 * macBytes.length];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) 0xff;
			}
			for (int i = 6; i < bytes.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
			}

			DidInfo didInfo = didInfoManageService.selectDidrInfoManageDetail(didId);
			InetAddress address = InetAddress.getByName(didInfo.getDidIpaddr());
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
			try (DatagramSocket socket = new DatagramSocket()) {
				socket.send(packet);
			}
			ResultHelper.setSuccess(resultVO);
		} catch (Exception e) {
			log.error("Failed to send Wake-on-LAN packet: {}", e.getMessage());
			ResultHelper.setFailResult(resultVO, "pcWakeOn", e, egovMessageSource);
		}
		return resultVO;
	}

	private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid hex digit in MAC address.");
		}
		return bytes;
	}

	@Operation(summary = "DID 관리자 목록 조회(관리자 전용 뷰)")
	@PostMapping("/didManagerList.do")
	public ResultVO selectDidManagerInfoManageListByPagination(@RequestBody DidInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();
			searchVO.setAuthor_Code(loginVO.getRoleId());

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
			}
			searchVO.setPageSize(propertiesService.getInt("pageSize"));

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			List<DidInfoVO> resultList = didInfoManageService.selectDidManagerInfoManageListByPagination(searchVO);
			int totCnt = didInfoManageService.selectDidManagerInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, resultList);
			resultMap.put("selectschLst", scheduleManagerService.selectScheduleInfoManageCombo());
			resultMap.put("selectSwver", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT005"));
			resultMap.put("paginationInfo", paginationInfo);
			resultMap.put("totalCnt", totCnt);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDidManagerInfoManageListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 목록 조회", description = "MHS(문화센터) 권한 사용자는 별도 모니터 목록 화면으로 안내합니다.")
	@PostMapping("/list.do")
	public ResultVO selectDidInfoManageListByPagination(@RequestBody DidInfoVO searchVO, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			if ("ROLE_MHS_ADMIN".equals(loginVO.getRoleId()) || "ROLE_MHS_USER".equals(loginVO.getRoleId())) {
				resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage("문화센터 계정은 모니터 목록 화면을 이용해 주세요.");
				return resultVO;
			}
			searchVO.setAuthor_Code(loginVO.getRoleId());
			searchVO.setGroupCode(loginVO.getPartId());

			if (searchVO.getPageUnit() <= 0) {
				searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
			}
			searchVO.setPageSize(propertiesService.getInt("pageSize"));
			if (searchVO.getCenterId() == null) searchVO.setCenterId("");
			if (searchVO.getDidModelType() == null) searchVO.setDidModelType("");

			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
			paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
			paginationInfo.setPageSize(searchVO.getPageSize());

			searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
			searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
			searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

			List<DidInfoVO> resultList = didInfoManageService.selectIntegrateManageListByPagination(searchVO);
			int totCnt = didInfoManageService.selectDidInfoManageListTotCnt_S(searchVO);
			paginationInfo.setTotalRecordCount(totCnt);

			CenterInfoVO centerInfoVO = new CenterInfoVO();
			centerInfoVO.setSearchKeyword("");

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, resultList);
			resultMap.put("paginationInfo", paginationInfo);
			resultMap.put("totalCnt", totCnt);
			resultMap.put("selectCenter", centerInfoManageService.selectCenterInfoManageCombo(centerInfoVO));
			resultMap.put("selectGroup", groupDidInfoManageService.selectComboLst());
			resultMap.put(Globals.STATUS_REGINFO, searchVO);
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDidInfoManageListByPagination", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 등록/수정 화면용 데이터 조회", description = "각종 콤보와, 수정 모드일 때 기존 DID 상세를 함께 반환합니다.")
	@GetMapping("/formData.do")
	public ResultVO selectDidrInfoManageDetail(@RequestParam(value = "didId", required = false, defaultValue = "") String didId,
												@RequestParam(value = "mode", required = false, defaultValue = "Ins") String mode,
												HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			GroupVo groupVo = new GroupVo();
			groupVo.setGroupId(loginVO.getPartId());

			CenterInfoVO centerInfoVO = new CenterInfoVO();
			centerInfoVO.setAuthorCode(loginVO.getRoleId());
			centerInfoVO.setMberId(loginVO.getManagerId());
			centerInfoVO.setSearchKeyword("");

			GroupInfoVO groupInfoVO = new GroupInfoVO();
			groupInfoVO.setAuthorCode(loginVO.getRoleId());
			groupInfoVO.setMberId(loginVO.getManagerId());

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("selectRole", groupManagerService.selectGroupManageCombo(groupVo));
			resultMap.put("selectGroup", groupInfoManageService.selectGroupInfoManageCombo(groupInfoVO));
			resultMap.put("selectCenter", centerInfoManageService.selectCenterInfoManageCombo(centerInfoVO));
			resultMap.put("selectType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT001"));
			resultMap.put("selectResolution", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT002"));
			resultMap.put("selectIpType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT003"));
			resultMap.put("selectModelType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT004"));
			resultMap.put("selectSwver", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT005"));
			resultMap.put("selectOs", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT011"));
			resultMap.put("selectSerialUse", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT012"));
			resultMap.put("selectComPort", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT013"));
			resultMap.put("selectMoniterCnt", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT014"));

			if ("Edt".equals(mode) && !didId.isBlank()) {
				resultMap.put("regist", didInfoManageService.selectDidrInfoManageDetail(didId));
			}

			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDidrInfoManageDetail", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 상세보기(뷰)")
	@GetMapping("/{didId}/view.do")
	public ResultVO selectDidrInfoManageView(@Parameter(description = "DID ID") @PathVariable String didId, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, didInfoManageService.selectDidrInfoManageDetailView(didId), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDidrInfoManageView", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 연결 콘텐츠/편성 조회")
	@GetMapping("/{didId}/schList.do")
	public ResultVO selectDidConSchInfoList(@Parameter(description = "DID ID") @PathVariable String didId, HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			ResultHelper.setSuccess(resultVO, didInfoManageService.selectDidDetailContentInfo(didId), Globals.JSON_RETURN_RESULT);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectDidConSchInfoList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 삭제")
	@DeleteMapping("/{didId}.do")
	public ResultVO deleteDidInfoManage(@Parameter(description = "DID ID") @PathVariable String didId, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = didInfoManageService.deleteDidInfoManage(didId);
			ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteDidInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "DID 등록/수정", description = "신규 등록 시 FN_DIDID()로 채번하고, 그룹이 지정되면 그룹-DID 연결도 함께 갱신합니다.")
	@PostMapping("/update.do")
	public ResultVO updateDidInfoManage(@RequestBody DidInfo vo, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret;
			boolean isInsert = "Ins".equals(vo.getMode());
			if (isInsert) {
				String insertDid = didInfoManageService.selectLastInsertDid(vo.getCenterId());
				vo.setDidId(insertDid);
				ret = didInfoManageService.insertDidInfoManage(vo);

				if (ret > 0 && vo.getGroupId() != null && !vo.getGroupId().isEmpty()) {
					GroupDidInfo groupDidInfo = new GroupDidInfo();
					groupDidInfo.setDidId(vo.getDidId());
					groupDidInfo.setGroupCode(vo.getGroupId());
					groupDidInfoManageService.insertGroupInfoManage(groupDidInfo);
				}
			} else {
				if (vo.getGroupId() != null && !vo.getGroupId().isEmpty()) {
					GroupDidInfo groupDidInfo = new GroupDidInfo();
					groupDidInfo.setDidId(vo.getDidId());
					groupDidInfo.setGroupCode(vo.getGroupId());
					groupDidInfoManageService.deleteGroupInfoManage(vo.getDidId());
					groupDidInfoManageService.insertGroupInfoManage(groupDidInfo);
				}
				ret = didInfoManageService.updateDidInfoManage(vo);
			}

			if (ret > 0) {
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put(Globals.STATUS_REGINFO, vo);
				resultVO.setResult(resultMap);
				resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
				resultVO.setResultMessage(egovMessageSource.getMessage(isInsert ? "success.common.insert" : "success.common.update"));
			} else {
				throw new Exception("Update failed");
			}
		} catch (Exception e) {
			log.error("updateDidInfoManage error: {}", e.toString());
			ResultHelper.setFailResult(resultVO, "updateDidInfoManage", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "통합관리 - 역할별 그룹 목록 조회")
	@GetMapping("/roleCenterInfo.do")
	public ResultVO selectRoleCenterInfo(@RequestParam(value = "groupId", required = false, defaultValue = "") String groupId,
										  @RequestParam(value = "firstIdx", required = false, defaultValue = "0") int firstIndex,
										  @RequestParam(value = "lastIdx", required = false, defaultValue = "0") int lastIndex,
										  @RequestParam(value = "recordCnt", required = false, defaultValue = "10") int recordCountPerPage,
										  @RequestParam(value = "systemType", required = false, defaultValue = "SIGNAGE") String requestSystemType,
										  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			DidInfoVO didInfoVO = new DidInfoVO();
			didInfoVO.setRequestSystemType(requestSystemType);
			didInfoVO.setFirstIndex(firstIndex);
			didInfoVO.setLastIndex(lastIndex);
			didInfoVO.setRecordCountPerPage(recordCountPerPage);
			didInfoVO.setGroupId(groupId);

			ResultHelper.setSuccess(resultVO, didInfoManageService.selectIntegrateRoleList(didInfoVO), "roleList");
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectRoleCenterInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "통합관리 - 그룹별 매장 목록 조회")
	@GetMapping("/integrateCenterList.do")
	public ResultVO selectIntegrateCetnerList(@RequestParam(value = "groupId", required = false, defaultValue = "") String groupId,
											   @RequestParam(value = "firstIdx", required = false, defaultValue = "0") int firstIndex,
											   @RequestParam(value = "lastIdx", required = false, defaultValue = "0") int lastIndex,
											   @RequestParam(value = "recordCnt", required = false, defaultValue = "10") int recordCountPerPage,
											   @RequestParam(value = "systemType", required = false, defaultValue = "SIGNAGE") String requestSystemType,
											   HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			DidInfoVO didInfoVO = new DidInfoVO();
			didInfoVO.setRequestSystemType(requestSystemType);
			didInfoVO.setFirstIndex(firstIndex);
			didInfoVO.setLastIndex(lastIndex);
			didInfoVO.setRecordCountPerPage(recordCountPerPage);
			didInfoVO.setGroupId(groupId);

			ResultHelper.setSuccess(resultVO, didInfoManageService.selectIntegrateCenterList(didInfoVO), "centerList");
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectIntegrateCetnerList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "통합관리 - 매장별 장비 목록 조회")
	@GetMapping("/integrateEquipList.do")
	public ResultVO selectIntegrateEquipList(@RequestParam(value = "groupId", required = false, defaultValue = "") String groupCode,
											  @RequestParam(value = "centerId", required = false, defaultValue = "") String centerId,
											  @RequestParam(value = "firstIdx", required = false, defaultValue = "0") int firstIndex,
											  @RequestParam(value = "lastIdx", required = false, defaultValue = "0") int lastIndex,
											  @RequestParam(value = "recordCnt", required = false, defaultValue = "10") int recordCountPerPage,
											  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			DidInfoVO didInfoVO = new DidInfoVO();
			didInfoVO.setFirstIndex(firstIndex);
			didInfoVO.setLastIndex(lastIndex);
			didInfoVO.setRecordCountPerPage(recordCountPerPage);
			didInfoVO.setGroupCode(groupCode);
			didInfoVO.setCenterId(centerId);

			ResultHelper.setSuccess(resultVO, didInfoManageService.selectIntegrateDeviceList(didInfoVO), "equipList");
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectIntegrateEquipList", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "통합관리 - 장비 상세 + 편성 조회")
	@GetMapping("/integrateEquipInfo.do")
	public ResultVO selectIntegrateEquipInfo(@RequestParam(value = "didId", required = false, defaultValue = "") String didId,
											  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			if (didId.isBlank()) {
				ResultHelper.setSuccess(resultVO, Map.of());
				return resultVO;
			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("equipInfo", didInfoManageService.selectDidrInfoManageDetailView(didId));
			resultMap.put("equipSchList", didInfoManageService.selectDidDetailContentInfo(didId));
			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectIntegrateEquipInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "통합관리 - 등록 폼 콤보 조회", description = "callType(basicInfo/centerInfo)에 따라 다른 콤보 세트를 반환합니다.")
	@GetMapping("/equipRegistCombo.do")
	public ResultVO selectEquipRegistComboData(@RequestParam(value = "callType", required = false, defaultValue = "0") String callType,
												@RequestParam(value = "callDetail", required = false, defaultValue = "") String callDetail,
												HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			Map<String, Object> resultMap = new HashMap<>();
			if ("basicInfo".equals(callType)) {
				GroupVo groupVo = new GroupVo();
				groupVo.setGroupId(loginVO.getPartId());

				resultMap.put("selectType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT001"));
				resultMap.put("selectResolution", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT002"));
				resultMap.put("selectIpType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT003"));
				resultMap.put("selectModelType", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT004"));
				resultMap.put("selectOs", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT011"));
				resultMap.put("selectRole", groupManagerService.selectGroupManageCombo(groupVo));
			} else if ("centerInfo".equals(callType)) {
				CenterInfoVO centerInfoVO = new CenterInfoVO();
				centerInfoVO.setSearchKeyword("");
				centerInfoVO.setSelectRoleCode(callDetail);
				resultMap.put("selectCenter", centerInfoManageService.selectCenterInfoManageCombo(centerInfoVO));
			}

			ResultHelper.setSuccess(resultVO, resultMap);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectEquipRegistComboData", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "통합관리 - 장비 등록/수정/삭제(폼 파라미터 방식)", description = "work=insert/modify/delete로 동작을 구분하는 대체 등록 화면용 엔드포인트입니다.")
	@PostMapping("/equipDetail.do")
	public ResultVO modifyEquipDetail(@RequestBody Map<String, String> params, HttpServletRequest request) {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String work = params.getOrDefault("work", "");
			if (work.isBlank()) {
				resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage("작업 구분이 없습니다.");
				return resultVO;
			}

			DidInfoVO didInfoVO = new DidInfoVO();
			if ("insert".equals(work)) {
				String centerId = params.getOrDefault("centerId", "");
				didInfoVO.setCenterId(centerId);
				didInfoVO.setDidNm(params.getOrDefault("didNm", ""));
				didInfoVO.setDidIptype(params.getOrDefault("didIptype", ""));
				didInfoVO.setDidOs(params.getOrDefault("didOs", ""));
				didInfoVO.setRoleCode(params.getOrDefault("roleId", ""));
				didInfoVO.setGroupId(params.getOrDefault("groupId", ""));
				didInfoVO.setDidStartTime(params.getOrDefault("openTime", ""));
				didInfoVO.setDidEndTime(params.getOrDefault("closeTime", ""));
				didInfoVO.setDidModelType(params.getOrDefault("systemType", ""));
				didInfoVO.setDidType(params.getOrDefault("deviceType", ""));
				didInfoVO.setDidWidth(params.getOrDefault("width", ""));
				didInfoVO.setDidHeight(params.getOrDefault("height", ""));
				didInfoVO.setDidResolution(params.getOrDefault("resolution", ""));
				didInfoVO.setDidUseYn("on".equals(params.get("useYn")) ? "Y" : "N");
				didInfoVO.setDidSwver("DIDSW02");

				if (centerId.isBlank()) {
					resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
					resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
					resultVO.setResultMessage("매장 정보가 없습니다.");
					return resultVO;
				}

				String insertDid = didInfoManageService.selectLastInsertDid(centerId);
				didInfoVO.setDidId(insertDid);
				int ret = didInfoManageService.insertDidInfoManage(didInfoVO);

				if (ret > 0 && didInfoVO.getGroupId() != null && !didInfoVO.getGroupId().isEmpty()) {
					GroupDidInfo groupDidInfo = new GroupDidInfo();
					groupDidInfo.setDidId(didInfoVO.getDidId());
					groupDidInfo.setGroupCode(didInfoVO.getGroupId());
					groupDidInfoManageService.insertGroupInfoManage(groupDidInfo);
				}
				ResultHelper.setCudResult(resultVO, ret, "success.common.insert", "fail.common.insert", egovMessageSource);
			} else if ("delete".equals(work)) {
				String didId = params.getOrDefault("didId", "");
				int ret = didId.isBlank() ? 0 : didInfoManageService.deleteDidInfoManage(didId);
				ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);
			} else {
				resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage("알 수 없는 작업 구분입니다: " + work);
			}
		} catch (Exception e) {
			log.error("modifyEquipDetail error: {}", e.toString());
			ResultHelper.setFailResult(resultVO, "modifyEquipDetail", e, egovMessageSource);
		}
		return resultVO;
	}
}
