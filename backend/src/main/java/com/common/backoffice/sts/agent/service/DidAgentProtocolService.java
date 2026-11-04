package com.common.backoffice.sts.agent.service;

import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.common.backoffice.sts.brd.modals.BasicBrodFileInfoVO;
import com.common.backoffice.sts.brd.modals.BasicBrodFileIntervalInfoVO;
import com.common.backoffice.sts.brd.modals.BasicBrodScheduleInfoVO;
import com.common.backoffice.sts.brd.modals.BasicFileGroupPlayInfoVO;
import com.common.backoffice.sts.brd.modals.BrodContentDetail;
import com.common.backoffice.sts.brd.modals.BrodOrganization;
import com.common.backoffice.sts.brd.modals.BrodScheduleInfo;
import com.common.backoffice.sts.brd.service.BasicBrodFileInfoManageService;
import com.common.backoffice.sts.brd.service.BasicBrodFileIntervalInfoManageService;
import com.common.backoffice.sts.brd.service.BasicBrodScheduleInfoManageService;
import com.common.backoffice.sts.brd.service.BasicFileGroupPlayInfoManageService;
import com.common.backoffice.sts.brd.service.BrodContentDetailManagerService;
import com.common.backoffice.sts.brd.service.BrodOrganizationManagerService;
import com.common.backoffice.sts.brd.service.BrodScheduleManagerService;
import com.common.backoffice.sts.cnt.modals.ContentDetailInfo;
import com.common.backoffice.sts.cnt.modals.ContentFileInfoVO;
import com.common.backoffice.sts.cnt.modals.ContentMessageInfo;
import com.common.backoffice.sts.cnt.modals.ContentMutiInfoVO;
import com.common.backoffice.sts.cnt.service.ContentDetailInfoService;
import com.common.backoffice.sts.cnt.service.ContentFileInfoManageService;
import com.common.backoffice.sts.cnt.service.ContentMessageInfoManageService;
import com.common.backoffice.sts.cnt.service.ContentMutimanageService;
import com.common.backoffice.sts.mhs.modals.MhsMonitorInfo;
import com.common.backoffice.sts.mhs.modals.MhsViewConnInfoVO;
import com.common.backoffice.sts.mhs.service.MhsMonitorInfoManageService;
import com.common.backoffice.sts.mhs.service.MhsViewConnInfoManageService;
import com.common.backoffice.sts.pic.modals.DidMoniterPicVO;
import com.common.backoffice.sts.pic.service.DidMoniterPicService;
import com.common.backoffice.sts.snd.modals.SendMsgInfo;
import com.common.backoffice.sts.snd.modals.SendMsgInfoVO;
import com.common.backoffice.sts.snd.service.SendMsgInfoManageService;
import com.common.backoffice.sts.xml.modals.XmlInfoVO;
import com.common.backoffice.sts.xml.service.XmlInfoManageService;
import com.common.backoffice.sym.did.modals.DidInfo;
import com.common.backoffice.sym.did.service.DidInfoManageService;
import com.common.backoffice.sym.sch.service.ContentSendHistoryInfoManagerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class DidAgentProtocolService {

	private static final Pattern HIS_SEQ_SPLIT = Pattern.compile(Pattern.quote("|"));
	private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final XmlInfoManageService xmlInfoManageService;
	private final DidInfoManageService didInfoManageService;
	private final SendMsgInfoManageService sendMsgInfoManageService;
	private final ContentDetailInfoService detailService;
	private final ContentFileInfoManageService fileService;
	private final ContentMutimanageService mutiInfoService;
	private final ContentMessageInfoManageService messageService;
	private final ContentSendHistoryInfoManagerService sendHistory;
	private final DidMoniterPicService picService;
	private final BrodScheduleManagerService brodSchedule;
	private final BrodContentDetailManagerService brodDetail;
	private final BrodOrganizationManagerService brodOrgService;
	private final BasicBrodScheduleInfoManageService schService;
	private final BasicBrodFileInfoManageService basicFileService;
	private final BasicBrodFileIntervalInfoManageService fileIntervalInfoManageService;
	private final BasicFileGroupPlayInfoManageService groupPlayInfo;
	private final MhsMonitorInfoManageService moniterService;
	private final MhsViewConnInfoManageService viewConn;

	// ===================================================================
	// jsonAuth.do
	// ===================================================================

	/** 요청 파라미터 json(작은따옴표 허용)을 받아 command_type별로 처리한 결과 문자열을 돌려준다 */
	public String handleJson(String json) throws Exception {
		json = json.replace("'", "\"");
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
		String commandType = String.valueOf(jsonObject.get("command_type"));

		if (xmlInfoManageService.selectXmlProcessCount(commandType) < 1) {
			return "NO_JSON";
		}
		log.info("jsonAuth : {}", json);
		return jsonDocResult(commandType, jsonObject);
	}

	@SuppressWarnings("unchecked")
	private String jsonDocResult(String commandType, JSONObject jsonObject) throws Exception {
		JSONArray dataInfoArray = (JSONArray) jsonObject.get("command_data");
		JSONObject data = (JSONObject) dataInfoArray.get(0);

		DidInfo didinfo = new DidInfo();
		ContentMutiInfoVO conMutiInfo = new ContentMutiInfoVO();
		SendMsgInfo sminfo = new SendMsgInfo();
		String resultTxt = null;

		switch (commandType) {
			case "SP_DIDAUTH": {
				didinfo.setDidId(str(data, "DID_ID"));
				didinfo.setDidMac(str(data, "DID_MAC"));
				didinfo.setDidIpaddr(str(data, "DID_IP"));
				didinfo.setDidSendInterval(str(data, "DID_INTERVAL"));
				didinfo.setDidType(str(data, "DID_TYPE"));
				int ret = didInfoManageService.updateDidMac(didinfo);
				resultTxt = simpleResult(commandType, sendRed(str(data, "DID_ID"), commandType, ret, "", str(data, "DID_MAC")));
				break;
			}
			case "SP_MHSAUTH": {
				// 문화센터 모니터 IP/MAC 갱신
				MhsMonitorInfo moniter = new MhsMonitorInfo();
				moniter.setMhsMonitorcd(str(data, "DID_ID").trim());
				moniter.setMhsIpaddr(str(data, "DID_IP").trim());
				moniter.setMhsMacaddr(str(data, "DID_MAC").trim());
				moniter.setMhsMviewtype(str(data, "DID_TYPE").trim());
				int ret = moniterService.updateMhsMonitorInfoIpMac(moniter);
				resultTxt = simpleResult(commandType, sendRed(str(data, "DID_ID"), commandType, ret, "", str(data, "DID_MAC")));
				break;
			}
			case "SP_MHSSTATE": {
				// 문화센터 상태 변화
				int ret = viewConn.updateMoniterDidUpdateChange(str(data, "DID_ID").trim());
				moniterService.updateMhsMonitorInfoStatus(str(data, "DID_ID").trim());
				resultTxt = okOrFail(commandType, ret);
				break;
			}
			case "SP_MONSCHJSON": {
				// 문화센터 강의 정보 리스트(오늘 기준)
				MhsViewConnInfoVO searchVO = new MhsViewConnInfoVO();
				searchVO.setMhsMonitorcd(str(data, "DID_ID").trim());
				searchVO.setSearchDay(LocalDate.now().format(YMD));
				List<MhsViewConnInfoVO> list = viewConn.selectViewMoniterClassInfo(searchVO);

				JSONArray dataArray = new JSONArray();
				for (MhsViewConnInfoVO r : list) {
					JSONObject s = new JSONObject();
					s.put("MHS_CONNSEQ", r.getMhsConnSeq());
					s.put("MHS_MONITERCD", decode(r.getMhsMonitorcd()));
					s.put("MHS_CLASSNM", decode(r.getMhsClassnm()));
					s.put("MHS_TEACHERNM", decode(r.getMhsTeachernm()));
					s.put("MHS_CLASSSTARTDAY", r.getMhsClassstartday());
					s.put("MHS_CLASSENDDAY", r.getMhsClassendday());
					s.put("MHS_CLASSDAYOFWEEK", r.getMhsClassdayofweek());
					s.put("MHS_CLASSSTARTTIME", r.getMhsClassstarttime());
					s.put("MHS_CLASSENDTIME", r.getMhsClassendtime());
					s.put("MHS_CLASSINTRO", decode(r.getMhsClassintro()));
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			case "SP_MONJSONUPDATE":
			case "SP_MONJSONUPDATERESET": {
				int ret = viewConn.updateMoniterDidUpdateChange(str(data, "DID_ID").trim());
				resultTxt = okOrFail(commandType, ret);
				break;
			}
			case "SP_BRODSTATE":
			case "SP_BRODSTATE_NEW": {
				didinfo.setDidId(str(data, "DID_ID").trim());
				didinfo.setDidMac(str(data, "DID_MAC").trim());
				didinfo.setDidSttus("Y");
				didInfoManageService.updateDidState(didinfo);
				String id = didinfo.getDidId();
				String mac = didinfo.getDidMac();
				String basic = "SP_BRODSTATE_NEW".equals(commandType) ? ",'BASIC_CNT':'" + basicCnt(id, mac) + "'" : "";
				resultTxt = "{'command_type':'" + commandType + "','result':{'BROD_CNT':'" + brodCnt(id, mac)
						+ "','ORD_CNT':'" + orderCnt(id, mac) + "','MSG_CNT':'" + messageCnt(id, mac) + "'" + basic + "} }";
				break;
			}
			case "SP_NEXTCONTENTFILELST": {
				List<ContentFileInfoVO> list = fileService.selectFileContentList(str(data, "CON_SEQ"));
				JSONArray dataArray = new JSONArray();
				for (ContentFileInfoVO r : list) {
					JSONObject s = new JSONObject();
					s.put("CON_SEQ", r.getConSeq());
					s.put("DETAIL_SEQ", r.getDetailSeq());
					s.put("FILE_STRE_COURS", r.getFileStreCours());
					s.put("ATCH_FILE_ID", r.getAtchFileId());
					s.put("STRE_FILE_NM", r.getStreFileNm());
					s.put("FILE_EXTSN", r.getFileExtsn());
					s.put("FILE_SIZE", r.getFileSize());
					s.put("FILE_ORDER", r.getFileOrder());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			case "SP_NEXTCONTENTINFO": {
				List<ContentMutiInfoVO> list = mutiInfoService.selectNextContentMutiInfo(str(data, "CON_SEQ"));
				JSONArray dataArray = new JSONArray();
				for (ContentMutiInfoVO r : list) {
					JSONObject s = new JSONObject();
					s.put("CON_SEQ", r.getConSeq());
					s.put("CON_SCREEN", r.getConScreen());
					s.put("CON_USEYN", r.getConUseYn());
					s.put("CON_WIDTH", r.getConWidth());
					s.put("CON_HEIGHT", r.getConHeight());
					s.put("CON_MID", r.getConMid());
					s.put("CON_NM", decode(r.getConNm()));
					s.put("CON_TIME", r.getConTime());
					s.put("CON_NEXTSEQ", r.getConNextSeq());
					s.put("CON_FILE", r.getConLocalfile());
					s.put("DETAIL_SEQ", r.getDetailSeq());
					s.put("DETAIL_ORDER", r.getDetailOrder());
					// 원본도 CON_TYPE을 두 번 넣어 상세 페이지 타입(conTypeD)이 최종값이었음
					s.put("CON_TYPE", r.getConTypeD());
					s.put("IMAGE_SLIDTYPE", r.getImageSlidType());
					s.put("TIME_INTERVAL", r.getTimeIntervalD());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			case "SP_MESSAGEINFO": {
				// 메세지 전문 확인 후 TB_MESSAGEHISTORY에 등록하고 리스트를 보여준다
				List<ContentMessageInfo> list = messageService.selectContentMessageInfoDidList(str(data, "DID_ID"));
				sminfo.setDidId(str(data, "DID_ID"));
				sminfo.setDidMacAddress(str(data, "DID_MAC"));

				JSONArray dataArray = new JSONArray();
				for (ContentMessageInfo r : list) {
					JSONObject s = new JSONObject();
					s.put("SEND_DIDID", r.getSendDidId());
					s.put("SEND_MESSAGE", decode(r.getSendMessage()));
					s.put("SEND_MESSAGESTARTDAY", r.getSendMessageStartDay());
					s.put("SEND_MESSAGEENDDAY", r.getSendMessageEndDay());
					s.put("SEND_MESSAGESTARTTIME", r.getSendMessageStartTime());
					s.put("SEND_MESSAGEENDTIME", r.getSendMessageEndTime());
					s.put("SEND_FONTTYPE", r.getSendFontType());
					dataArray.add(s);

					if (sendMsgInfoManageService.selectSendDidIDCheckCnt(r.getSendDidId()) < 1) {
						sminfo.setSendDidId(r.getSendDidId());
						sendMsgInfoManageService.insertSendMessageInsertManage(sminfo);
					}
				}
				resultTxt = listResult(commandType, "MESSAGEINFO", dataArray);
				break;
			}
			case "SP_MESSAGEUPDATE": {
				String sendDidid = str(data, "SEND_DIDID");
				String errorMessage = str(data, "ERROR_MESSAGE");
				sminfo.setDidId(str(data, "DID_ID"));
				sminfo.setDidMacAddress(str(data, "DID_MAC"));
				sminfo.setMsgSeq(sendMsgInfoManageService.selectSendDidIDMsgSeq(sendDidid));
				if ("OK".equals(errorMessage)) {
					messageService.updateContentMessageInfoClientManage(sendDidid);
					sminfo.setSendResult("OK");
				} else {
					sminfo.setSendResult("FALSE");
				}
				sminfo.setErrorMessage(errorMessage);
				int ret = sendMsgInfoManageService.updateSendMsgInfoManage(sminfo);
				resultTxt = okOrFail(commandType, ret);
				break;
			}
			case "SP_DIDSTATE": {
				didinfo.setDidId(str(data, "DID_ID"));
				didinfo.setDidMac(str(data, "DID_MAC"));
				didinfo.setDidSttus("Y");
				didInfoManageService.updateDidState(didinfo);
				String id = didinfo.getDidId();
				String mac = didinfo.getDidMac();
				resultTxt = "{'command_type':'" + commandType + "','result':{'SCH_CNT':'" + scheduleDID(id, mac)
						+ "','ORD_CNT':'" + orderCnt(id, mac) + "','MSG_CNT':'" + messageCnt(id, mac) + "'} }";
				break;
			}
			case "SP_BRODSCH":
			case "SP_BRODSCHNEW":
			case "SP_REBRODSCHNEW":
			case "SP_REBRODSCH": {
				BrodScheduleInfo vo = new BrodScheduleInfo();
				vo.setDidId(str(data, "DID_ID"));
				vo.setDidMac(str(data, "DID_MAC"));

				JSONObject obj = new JSONObject();
				obj.put("command_type", commandType);
				// 원본처럼 조회 실패/결과 없음이면 BRODINFO 없이 command_type만 돌려준다
				try {
					BrodScheduleInfo r = switch (commandType) {
						case "SP_BRODSCH" -> brodSchedule.selectBordScheduleCode(vo);
						case "SP_BRODSCHNEW" -> brodSchedule.selectBordScheduleCodeNew(vo);
						case "SP_REBRODSCHNEW" -> brodSchedule.selectBordScheduleCodeRedownNew(vo);
						default -> brodSchedule.selectBordScheduleCodeRedown(vo);
					};
					JSONObject s = new JSONObject();
					s.put("BROD_CODE", r.getBrodCode());
					s.put("CENTER_STARTTIME", r.getCenterStartTime());
					s.put("CENTER_ENDTIME", r.getCenterEndTime());
					s.put("CENTER_ID", r.getCenterId());
					// 파일 경로는 구버전 명령(SP_BRODSCH/SP_REBRODSCH)만 내려줌
					if ("SP_BRODSCH".equals(commandType) || "SP_REBRODSCH".equals(commandType)) {
						s.put("FILE_STRE_COURS", r.getFileStreCours());
						s.put("STRE_FILE_NM", r.getStreFileNm());
					}
					JSONArray dataArray = new JSONArray();
					dataArray.add(s);
					obj.put("BRODINFO", dataArray);
				} catch (Exception e) {
					log.debug("{} 조회 결과 없음: {}", commandType, e.toString());
				}
				resultTxt = obj.toJSONString();
				break;
			}
			case "SP_BRODSCHFILELST": {
				BrodContentDetail vo = new BrodContentDetail();
				vo.setDidId(str(data, "DID_ID"));
				vo.setDidMac(str(data, "DID_MAC"));
				vo.setBrodCode(str(data, "BROD_CODE"));
				JSONArray dataArray = new JSONArray();
				for (BrodContentDetail r : brodDetail.selectBrodFileList(vo)) {
					JSONObject s = new JSONObject();
					s.put("ATCH_FILE_ID", r.getAtchFileId());
					s.put("FILESTRECOURS", r.getFileStreCours());
					s.put("STREFILENM", r.getStreFileNm());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "FILEINFO", dataArray);
				break;
			}
			case "SP_BASICSCHFILELST": {
				JSONArray dataArray = new JSONArray();
				for (BasicBrodFileInfoVO r : basicFileService.selectBasicBrodSchFileLstNew(str(data, "BASIC_CODE"))) {
					JSONObject s = new JSONObject();
					s.put("ATCH_FILE_ID", r.getAtchFileId());
					s.put("FILESTRECOURS", r.getFileStreCours());
					s.put("STREFILENM", r.getStreFileNm());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "FILEINFO", dataArray);
				break;
			}
			case "SP_BASICSCHFILELST_INFO": {
				BasicBrodFileInfoVO vo = new BasicBrodFileInfoVO();
				vo.setBasicCode(str(data, "BASIC_CODE"));
				vo.setSearchDay(LocalDate.now().format(YMD));
				JSONArray dataArray = new JSONArray();
				for (BasicBrodFileInfoVO r : basicFileService.selectBasicBrodSchFileLst(vo)) {
					JSONObject s = new JSONObject();
					s.put("BASIC_CODE", r.getBasicCode());
					s.put("ATCH_FILE_ID", r.getAtchFileId());
					s.put("FILESTRECOURS", r.getFileStreCours());
					s.put("STREFILENM", r.getStreFileNm());
					s.put("BASIC_TIMEDIV", r.getBasicTimeDiv());
					s.put("BASIC_STARTTIME", r.getBasicStartTime());
					s.put("BASIC_ENDTIME", r.getBasicEndTime());
					dataArray.add(s);
				}
				resultTxt = listResultWithOk(commandType, dataArray);
				break;
			}
			case "SP_BASICSCHLST_NEW": {
				// 신규 기초 파일 리스트(다운로드 체크 갱신 후 파일 목록)
				BasicBrodScheduleInfoVO vo = new BasicBrodScheduleInfoVO();
				vo.setCenterId(str(data, "CENTER_ID"));
				vo.setBasicCode(str(data, "BASIC_CODE"));
				vo.setDidDowncheck(str(data, "DID_DOWNCHECK"));
				int ret = schService.updateBasicBrodScheduleCenterDownCheck(vo);
				if (ret > 0) {
					JSONArray dataArray = new JSONArray();
					for (BasicBrodFileIntervalInfoVO r : fileIntervalInfoManageService.selectAgentFileList(str(data, "BASIC_CODE"))) {
						JSONObject s = new JSONObject();
						s.put("BROD_STARTDAY", r.getBrodStartday());
						s.put("BROD_ENDDAY", r.getBrodEndday());
						s.put("BROD_STARTTIME", r.getBrodStarttime());
						s.put("BROD_ENDTIME", r.getBrodEndtime());
						s.put("BASIC_CODE", r.getBasicCode());
						s.put("GROUP_TIMEGUBUN", r.getGroupTimeGubun());
						s.put("STREFILENM", r.getStreFileNm());
						s.put("FILESTRECOURS", r.getFileStreCours());
						s.put("ORIGNL_FILE_NM", nvl(r.getOrignlFileNm()).replace("'", " "));
						dataArray.add(s);
					}
					resultTxt = listResultWithOk(commandType, dataArray);
				} else {
					resultTxt = simpleResult(commandType, "F");
				}
				break;
			}
			case "SP_BASICPLAYUPDATE": {
				// 기초 음원 재생 횟수 갱신
				BasicFileGroupPlayInfoVO vo = new BasicFileGroupPlayInfoVO();
				vo.setCenterId(str(data, "DID_ID"));
				vo.setPlayDay(str(data, "PLAY_DAY"));
				vo.setFilePlayList(str(data, "FILEPLAYLIST"));

				JSONObject jsonPlay = (JSONObject) new JSONParser().parse("{\"playInfo\":" + str(data, "playInfo") + "}");
				JSONArray playArray = (JSONArray) jsonPlay.get("playInfo");
				for (Object o : playArray) {
					JSONObject play = (JSONObject) o;
					vo.setAtchFileId(str(play, "playFile").replace(".mp3", ""));
					vo.setPlayCnt(str(play, "playCnt"));
					groupPlayInfo.updateFilePlay(vo);
				}
				resultTxt = simpleResult(commandType, "O");
				log.info("SP_BASICPLAYUPDATE CENTER_ID : {}", vo.getCenterId());
				break;
			}
			case "SP_BASICSCHLST_NEWFILEINFO": {
				JSONArray dataArray = new JSONArray();
				for (BasicBrodFileIntervalInfoVO r : fileIntervalInfoManageService.selectAgentFileDownList(str(data, "BASIC_CODE"))) {
					JSONObject s = new JSONObject();
					s.put("STREFILENM", r.getStreFileNm());
					s.put("FILESTRECOURS", r.getFileStreCours());
					s.put("ORIGNL_FILE_NM", nvl(r.getOrignlFileNm()).replace("'", " "));
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "FILEINFO", dataArray);
				break;
			}
			case "SP_BRODSCHLST": {
				BrodOrganization vo = new BrodOrganization();
				vo.setDidId(str(data, "DID_ID"));
				vo.setDidMac(str(data, "DID_MAC"));
				vo.setBrodCode(str(data, "BROD_CODE"));
				JSONArray dataArray = new JSONArray();
				for (BrodOrganization r : brodOrgService.selectBrodOrgnizationDid(vo)) {
					JSONObject s = new JSONObject();
					s.put("BROD_TIME", r.getBrodTime());
					s.put("STRE_FILE_NM", r.getStreFileNm());
					s.put("BROD_SEQ", r.getBrodSeq());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "REPORTINFO", dataArray);
				break;
			}
			case "SP_BRODDOWNCHECK": {
				BrodScheduleInfo vo = new BrodScheduleInfo();
				vo.setCenterId(str(data, "CENTER_ID"));
				vo.setBrodCode(str(data, "BROD_CODE"));
				vo.setDidDownCheck(str(data, "DID_DOWNCHECK"));
				resultTxt = okOrFail(commandType, brodSchedule.updateBrodDidCenterID(vo));
				break;
			}
			case "SP_BASICDOWNCHECK": {
				BasicBrodScheduleInfoVO vo = new BasicBrodScheduleInfoVO();
				vo.setCenterId(str(data, "CENTER_ID"));
				vo.setBasicCode(str(data, "BASIC_CODE"));
				vo.setDidDowncheck(str(data, "DID_DOWNCHECK"));
				resultTxt = okOrFail(commandType, schService.updateBasicBrodScheduleCenterDownCheck(vo));
				break;
			}
			case "SP_DIDCONTENTLST": {
				conMutiInfo.setDidId(str(data, "DID_ID"));
				conMutiInfo.setDidMac(str(data, "DID_MAC"));
				JSONArray dataArray = new JSONArray();
				for (ContentMutiInfoVO r : mutiInfoService.selectDIDContentLst(conMutiInfo)) {
					JSONObject s = new JSONObject();
					s.put("HIS_SEQ", r.getHisSeq());
					s.put("SCH_CODE", r.getSchCode());
					s.put("CON_SEQ", r.getConSeq());
					s.put("CON_NM", decode(r.getConNm()));
					s.put("CON_SCREEN", r.getConScreen());
					s.put("CON_TYPE", r.getConType());
					s.put("CON_USEYN", r.getConUseYn());
					s.put("CON_WIDTH", r.getConWidth());
					s.put("CON_HEIGHT", r.getConHeight());
					s.put("CON_MID", r.getConMid());
					s.put("CON_TIME", r.getConTime());
					s.put("CON_NEXTSEQ", r.getConNextSeq());
					s.put("CON_FILE", r.getConFile());
					s.put("CON_LOCALFILE", r.getConLocalfile());
					s.put("SCH_EMERGUBUN", r.getSchEmerGubun());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			case "SP_DIDCONTENTLSTUPDATECHECK":
			case "SP_DIDCONTENTFILELSTUPDATECHECK": {
				String hisSeq = str(data, "HIS_SEQ");
				int resultUpdate = "SP_DIDCONTENTLSTUPDATECHECK".equals(commandType)
						? sendHistory.updateContentSendHistoryConInfoManage(hisSeq)
						: sendHistory.updateContentSendHistoryConFileInfoManage(hisSeq);
				sminfo.setDidId(str(data, "DID_ID"));
				sminfo.setDidMacAddress(str(data, "DID_MAC"));
				sminfo.setXmlProcessName(commandType);
				sminfo.setErrorMessage("HIS_SEQ:" + hisSeq);
				sminfo.setSendResult(resultUpdate > 0 ? "Y" : "N");
				if (resultUpdate > 0) {
					// 원본 응답 문자열 그대로(FILE 쪽은 'result' 앞에 공백이 있었음)
					resultTxt = "SP_DIDCONTENTFILELSTUPDATECHECK".equals(commandType)
							? "{'command_type':'" + commandType + "', 'result':'O'}"
							: simpleResult(commandType, "O");
				} else {
					resultTxt = simpleResult(commandType, "F");
				}
				sendMsgInfoManageService.insertSendMsgInfoManage(sminfo);
				break;
			}
			case "SP_SCHRESET": {
				resultTxt = okOrFail(commandType, sendHistory.updateContentSchUpdateCheckReset(str(data, "HIS_SEQ")));
				break;
			}
			case "SP_DIDCONTENTPAGELST": {
				ContentDetailInfo detailPage = new ContentDetailInfo();
				detailPage.setDidId(str(data, "DID_ID").trim());
				detailPage.setSchCode(str(data, "SCH_CODE").trim());
				JSONArray dataArray = new JSONArray();
				for (ContentDetailInfo r : detailService.selectContentDetailDidPage(detailPage)) {
					JSONObject s = new JSONObject();
					s.put("HIS_SEQ", r.getHisSeq());
					s.put("SCH_CODE", r.getSchCode());
					s.put("CON_SEQ", r.getConSeq());
					s.put("CON_FILE", r.getConFile());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			case "SP_DIDCONTENTPAGELSTUPDATECHECK": {
				resultTxt = okOrFail(commandType, updatePageHistory(str(data, "HIS_SEQ")));
				break;
			}
			case "SP_DIDCONTENTFILELST": {
				ContentFileInfoVO fileSearch = new ContentFileInfoVO();
				fileSearch.setDidId(str(data, "DID_ID"));
				fileSearch.setSchCode(str(data, "SCH_CODE"));
				JSONArray dataArray = new JSONArray();
				for (ContentFileInfoVO r : fileService.selectFileContentLstDid(fileSearch)) {
					JSONObject s = new JSONObject();
					s.put("HIS_SEQ", r.getHisSeq());
					s.put("SCH_CODE", r.getSchCode());
					s.put("CON_SEQ", r.getConSeq());
					s.put("DETAIL_SEQ", r.getDetailSeq());
					s.put("FILE_STRE_COURS", r.getFileStreCours());
					s.put("ATCH_FILE_ID", r.getAtchFileId());
					s.put("STRE_FILE_NM", decode(r.getStreFileNm()));
					s.put("FILE_EXTSN", r.getFileExtsn());
					s.put("FILE_SIZE", r.getFileSize());
					s.put("FILE_ORDER", r.getFileOrder());
					s.put("TIME_INTERVAL", r.getTimeInterval());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			case "SP_DIDMONITERRESULT": {
				int ret = insertMonitorPic(str(data, "DID_ID"), str(data, "DID_MAC"), str(data, "MSG_SEQ"), str(data, "FILENM"));
				resultTxt = okOrFail(commandType, updatePicSendResult(str(data, "MSG_SEQ"), ret));
				break;
			}
			case "SP_DIDREBOOTRESULT":
			case "SP_DIDENDTIMERESULT": {
				int ret = updateCommandResult(str(data, "DID_ID"), str(data, "DID_MAC"), str(data, "ERROR_MESSAGE"), str(data, "MSG_SEQ"));
				resultTxt = okOrFail(commandType, ret);
				break;
			}
			case "SP_DIDREBOOT":
			case "SP_DIDMONITER": {
				// 재부팅 / 화면 전송 요청 등록 → 등록된 MSG_SEQ 반환
				int ret = insertCommand(str(data, "DID_ID"), str(data, "DID_MAC"), commandType, null);
				resultTxt = ret > 0
						? simpleResult(commandType, Integer.toString(sendMsgInfoManageService.selectMaxSeq()))
						: simpleResult(commandType, "F");
				break;
			}
			case "SP_DIDENDTIME": {
				String endTime = str(data, "DID_ENDTIME");
				didinfo.setDidId(str(data, "DID_ID"));
				didinfo.setDidMac(str(data, "DID_MAC"));
				didinfo.setDidEndTime(endTime);
				int ret = didInfoManageService.updateDidEndTime(didinfo);
				int ret1 = insertCommand(str(data, "DID_ID"), str(data, "DID_MAC"), commandType, endTime);
				if (ret > 0 && ret1 > 0) {
					int maxSeq = sendMsgInfoManageService.selectMaxSeq();
					resultTxt = "{'command_type':'" + commandType + "','result':[{'MSG_SEQ':'" + maxSeq + "','END_TIME':'" + endTime + "'}]}";
				} else {
					resultTxt = simpleResult(commandType, "F");
				}
				break;
			}
			case "SP_ORDERLST": {
				SendMsgInfoVO sminfoVO = new SendMsgInfoVO();
				sminfoVO.setDidId(str(data, "DID_ID"));
				sminfoVO.setDidMacAddress(str(data, "DID_MAC"));
				JSONArray dataArray = new JSONArray();
				for (SendMsgInfoVO r : sendMsgInfoManageService.selectDIDOrderLst(sminfoVO)) {
					JSONObject s = new JSONObject();
					s.put("MSG_SEQ", r.getMsgSeq());
					s.put("XML_PROCESS_NAME", r.getXmlProcessName());
					s.put("SEND_REGDATE", r.getSendRegDate());
					dataArray.add(s);
				}
				resultTxt = listResult(commandType, "CONINFO", dataArray);
				break;
			}
			default:
				// TB_XMLINFO에 등록은 돼 있지만 처리 분기가 없는 명령 — 원본도 null(빈 응답)을 돌려줬음
				log.warn("jsonAuth 처리 분기 없음: {}", commandType);
				break;
		}
		return resultTxt;
	}

	// ===================================================================
	// xmlAuth.do
	// ===================================================================

	/** 요청 파라미터 xml을 받아 command_type별로 처리한 CM_Document XML 문자열을 돌려준다 */
	public String handleXml(String xml) throws Exception {
		Document doc = parseXml(xml);
		String processName = commandType(doc);
		if (xmlInfoManageService.selectXmlProcessCount(processName) < 1) {
			return "NO_XMLNM";
		}
		return xmlDocumentResult(doc, processName);
	}

	private String xmlDocumentResult(Document doc, String processName) {
		try {
			XmlInfoVO vo = xmlInfoManageService.selectXmlrInfoManageNameDetail(processName);

			// TB_XMLINFO.XML_INPUT_PARAM(콤마 구분 파라미터명) 순서대로 command_data 하위 값을 채운다
			String[] result = vo.getXmlInputParam().split(",");
			NodeList nodeList = doc.getElementsByTagName("command_data");
			for (int i = 0; i < nodeList.getLength(); i++) {
				for (Node node = nodeList.item(i).getFirstChild(); node != null; node = node.getNextSibling()) {
					for (int a = 0; a < result.length; a++) {
						if (result[a].trim().equals(node.getNodeName())) {
							result[a] = node.getFirstChild() != null ? node.getFirstChild().getNodeValue() : "";
						}
					}
				}
			}

			String name = vo.getXmlProcessName();
			StringBuilder xmlDoc = new StringBuilder();
			xmlDoc.append("<?xml version='1.0' encoding='UTF-8'?>\r\n");
			xmlDoc.append("<CM_Document protocol = 'CMXML' version = '1.0'>\r\n");
			xmlDoc.append("   <command>\r\n");
			xmlDoc.append("       <command_type>" + name + "</command_type>\r\n");
			xmlDoc.append("       <command_result>\r\n");

			DidInfo didinfo = new DidInfo();
			ContentMutiInfoVO conMutiInfo = new ContentMutiInfoVO();

			switch (name) {
				case "SP_DIDAUTH": {
					didinfo.setDidId(result[0]);
					didinfo.setDidMac(result[1]);
					didinfo.setDidIpaddr(result[2]);
					didinfo.setDidSendInterval(result[3]);
					didinfo.setDidType(result[4]);
					int ret = didInfoManageService.updateDidMac(didinfo);
					xmlDoc.append("           <result>" + sendRed(result[0], name, ret, "", result[1]) + "</result>\r\n");
					break;
				}
				case "SP_NEXTCONTENTFILELST": {
					List<ContentFileInfoVO> list = fileService.selectFileContentList(result[0]);
					xmlDoc.append("           <result>");
					for (ContentFileInfoVO r : list) {
						// 닫는 태그의 콤마/공백(</DETAIL_SEQ,> 등)은 원본 그대로 — 기존 에이전트 호환
						xmlDoc.append("   <CONINFO>\r\n");
						xmlDoc.append("           <CON_SEQ>" + r.getConSeq() + "</CON_SEQ>\r\n");
						xmlDoc.append("           <DETAIL_SEQ>" + r.getDetailSeq() + "</DETAIL_SEQ,>\r\n");
						xmlDoc.append("           <FILE_STRE_COURS>" + r.getFileStreCours() + "</FILE_STRE_COURS,>\r\n");
						xmlDoc.append("           <ATCH_FILE_ID>" + r.getAtchFileId() + "</ATCH_FILE_ID>\r\n");
						xmlDoc.append("           <STRE_FILE_NM>" + r.getStreFileNm() + "</STRE_FILE_NM>\r\n");
						xmlDoc.append("           <FILE_EXTSN>" + r.getFileExtsn() + "</FILE_EXTSN>\r\n");
						xmlDoc.append("           <FILE_SIZE>" + r.getFileSize() + "</FILE_SIZE>\r\n");
						xmlDoc.append("           <FILE_ORDER >" + r.getFileOrder() + "</FILE_ORDER >\r\n");
						xmlDoc.append("   </CONINFO>\r\n");
					}
					xmlDoc.append("           </result>\r\n");
					break;
				}
				case "SP_NEXTCONTENTINFO": {
					List<ContentMutiInfoVO> list = mutiInfoService.selectNextContentMutiInfo(result[0]);
					xmlDoc.append("           <result>");
					for (ContentMutiInfoVO r : list) {
						xmlDoc.append("   <CONINFO>\r\n");
						xmlDoc.append("           <CON_SEQ>" + r.getConSeq() + "</CON_SEQ>\r\n");
						xmlDoc.append("           <CON_SCREEN>" + r.getConScreen() + "</CON_SCREEN>\r\n");
						xmlDoc.append("           <CON_USEYN>" + r.getConUseYn() + "</CON_USEYN>\r\n");
						xmlDoc.append("           <CON_WIDTH>" + r.getConWidth() + "</CON_WIDTH>\r\n");
						xmlDoc.append("           <CON_HEIGHT>" + r.getConHeight() + "</CON_HEIGHT>\r\n");
						xmlDoc.append("           <CON_MID>" + r.getConMid() + "</CON_MID>\r\n");
						xmlDoc.append("           <CON_NM>" + decode(r.getConNm()) + "</CON_NM>\r\n");
						xmlDoc.append("           <CON_TIME>" + r.getConTime() + "</CON_TIME>\r\n");
						xmlDoc.append("           <CON_NEXTSEQ>" + r.getConNextSeq() + "</CON_NEXTSEQ>\r\n");
						xmlDoc.append("           <CON_FILE>" + r.getConFile() + "</CON_FILE>\r\n");
						xmlDoc.append("           <DETAIL_SEQ>" + r.getDetailSeq() + "</DETAIL_SEQ>\r\n");
						xmlDoc.append("           <DETAIL_ORDER>" + r.getDetailOrder() + "</DETAIL_ORDER>\r\n");
						xmlDoc.append("           <CON_TYPE>" + r.getConTypeD() + "</CON_TYPE>\r\n");
						xmlDoc.append("           <IMAGE_SLIDTYPE>" + r.getImageSlidType() + "</IMAGE_SLIDTYPE>\r\n");
						xmlDoc.append("           <TIME_INTERVAL>" + r.getTimeIntervalD() + "</TIME_INTERVAL>\r\n");
						xmlDoc.append("   </CONINFO>\r\n");
					}
					xmlDoc.append("           </result>\r\n");
					break;
				}
				case "SP_MHSAUTH": {
					// 이마트 문화센터 모니터 IP/MAC 갱신(원본은 sendRed의 MAC 자리에 IP(result[1])를 넘김 — 그대로 유지)
					MhsMonitorInfo moniter = new MhsMonitorInfo();
					moniter.setMhsMonitorcd(result[0]);
					moniter.setMhsIpaddr(result[1]);
					moniter.setMhsMacaddr(result[2]);
					int ret = moniterService.updateMhsMonitorInfoIpMac(moniter);
					xmlDoc.append("           <result>" + sendRed(result[0], name, ret, "", result[1]) + "</result>\r\n");
					break;
				}
				case "SP_DIDSTATE": {
					didinfo.setDidId(result[0]);
					didinfo.setDidMac(result[1]);
					didinfo.setDidSttus("Y");
					didInfoManageService.updateDidState(didinfo);
					xmlDoc.append("           <result>\r\n");
					xmlDoc.append("                    <SCH_CNT>" + scheduleDID(result[0], result[1]) + "</SCH_CNT>\r\n");
					xmlDoc.append("                    <ORD_CNT>" + orderCnt(result[0], result[1]) + "</ORD_CNT>\r\n");
					xmlDoc.append("                    <MSG_CNT>" + messageCnt(result[0], result[1]) + "</MSG_CNT>\r\n");
					xmlDoc.append("           </result>\r\n");
					break;
				}
				case "SP_DIDCONTENTLST": {
					conMutiInfo.setDidId(result[0]);
					conMutiInfo.setDidMac(result[1]);
					xmlDoc.append("           <result>");
					for (ContentMutiInfoVO r : mutiInfoService.selectDIDContentLst(conMutiInfo)) {
						xmlDoc.append("   <CONINFO>\r\n");
						xmlDoc.append("           <HIS_SEQ>" + r.getHisSeq() + "</HIS_SEQ>\r\n");
						xmlDoc.append("           <SCH_CODE>" + r.getSchCode() + "</SCH_CODE>\r\n");
						xmlDoc.append("           <CON_SEQ>" + r.getConSeq() + "</CON_SEQ>\r\n");
						xmlDoc.append("           <CON_NM>" + decode(r.getConNm()) + "</CON_NM>\r\n");
						xmlDoc.append("           <CON_SCREEN>" + r.getConScreen() + "</CON_SCREEN>\r\n");
						xmlDoc.append("           <CON_TYPE>" + r.getConType() + "</CON_TYPE>\r\n");
						xmlDoc.append("           <CON_USEYN>" + r.getConUseYn() + "</CON_USEYN>\r\n");
						xmlDoc.append("           <CON_WIDTH>" + r.getConWidth() + "</CON_WIDTH>\r\n");
						xmlDoc.append("           <CON_HEIGHT>" + r.getConHeight() + "</CON_HEIGHT>\r\n");
						xmlDoc.append("           <CON_MID>" + r.getConMid() + "</CON_MID>\r\n");
						xmlDoc.append("           <CON_TIME>" + r.getConTime() + "</CON_TIME>\r\n");
						xmlDoc.append("           <CON_NEXTSEQ>" + r.getConNextSeq() + "</CON_NEXTSEQ>\r\n");
						xmlDoc.append("           <CON_FILE>" + r.getConFile() + "</CON_FILE>\r\n");
						xmlDoc.append("   </CONINFO>\r\n");
					}
					xmlDoc.append("           </result>\r\n");
					break;
				}
				case "SP_DIDCONTENTLSTUPDATECHECK": {
					xmlDoc.append(xmlOkOrFail(sendHistory.updateContentSendHistoryConInfoManage(result[2])));
					break;
				}
				case "SP_DIDCONTENTPAGELST": {
					ContentDetailInfo detailPage = new ContentDetailInfo();
					detailPage.setDidId(result[0].trim());
					detailPage.setSchCode(result[1].trim());
					xmlDoc.append("           <result>\r\n");
					for (ContentDetailInfo r : detailService.selectContentDetailDidPage(detailPage)) {
						xmlDoc.append("   <CONINFO>\r\n");
						xmlDoc.append("           <HIS_SEQ>" + r.getHisSeq() + "</HIS_SEQ>\r\n");
						xmlDoc.append("           <SCH_CODE>" + r.getSchCode() + "</SCH_CODE>\r\n");
						xmlDoc.append("           <CON_SEQ>" + r.getConSeq() + "</CON_SEQ>\r\n");
						xmlDoc.append("           <DETAIL_SEQ>" + r.getDetailSeq() + "</DETAIL_SEQ>\r\n");
						xmlDoc.append("           <DETAIL_ORDER>" + r.getDetailOrder() + "</DETAIL_ORDER>\r\n");
						xmlDoc.append("           <CON_TYPE>" + r.getConType() + "</CON_TYPE>\r\n");
						xmlDoc.append("           <IMAGE_SLIDTYPE>" + r.getImageSlidtype() + "</IMAGE_SLIDTYPE>\r\n");
						xmlDoc.append("           <TIME_INTERVAL>" + r.getTimeInterval() + "</TIME_INTERVAL>\r\n");
						xmlDoc.append("   </CONINFO>\r\n");
					}
					xmlDoc.append("           </result>\r\n");
					break;
				}
				case "SP_DIDCONTENTPAGELSTUPDATECHECK": {
					xmlDoc.append(xmlOkOrFail(updatePageHistory(result[2])));
					break;
				}
				case "SP_DIDCONTENTFILELST": {
					ContentFileInfoVO fileSearch = new ContentFileInfoVO();
					fileSearch.setDidId(result[0]);
					fileSearch.setSchCode(result[1]);
					xmlDoc.append("           <result>\r\n");
					for (ContentFileInfoVO r : fileService.selectFileContentLstDid(fileSearch)) {
						xmlDoc.append("   <CONINFO>\r\n");
						xmlDoc.append("           <HIS_SEQ>" + r.getHisSeq() + "</HIS_SEQ>\r\n");
						xmlDoc.append("           <SCH_CODE>" + r.getSchCode() + "</SCH_CODE>\r\n");
						xmlDoc.append("           <CON_SEQ>" + r.getConSeq() + "</CON_SEQ>\r\n");
						xmlDoc.append("           <DETAIL_SEQ>" + r.getDetailSeq() + "</DETAIL_SEQ>\r\n");
						xmlDoc.append("           <FILE_STRE_COURS>" + r.getFileStreCours() + "</FILE_STRE_COURS>\r\n");
						xmlDoc.append("           <ATCH_FILE_ID>" + r.getAtchFileId() + "</ATCH_FILE_ID>\r\n");
						xmlDoc.append("           <STRE_FILE_NM>" + decode(r.getStreFileNm()) + "</STRE_FILE_NM>\r\n");
						xmlDoc.append("           <FILE_EXTSN>" + r.getFileExtsn() + "</FILE_EXTSN>\r\n");
						xmlDoc.append("           <FILE_SIZE>" + r.getFileSize() + "</FILE_SIZE>\r\n");
						xmlDoc.append("   </CONINFO>\r\n");
					}
					xmlDoc.append("           </result>\r\n");
					break;
				}
				case "SP_DIDCONTENTFILELSTUPDATECHECK": {
					xmlDoc.append(xmlOkOrFail(sendHistory.updateContentSendHistoryConFileInfoManage(result[2])));
					break;
				}
				case "SP_DIDMONITERRESULT": {
					// 화면 캡처 결과 등록
					int ret = insertMonitorPic(result[0], result[1], result[2], result[3]);
					int retUpdate = updatePicSendResult(result[2], ret);
					xmlDoc.append(xmlOkOrFail(ret > 0 && retUpdate > 0 ? 1 : 0));
					break;
				}
				case "SP_DIDREBOOTRESULT":
				case "SP_DIDENDTIMERESULT": {
					xmlDoc.append(xmlOkOrFail(updateCommandResult(result[0], result[1], result[2], result[3])));
					break;
				}
				case "SP_DIDREBOOT":
				case "SP_DIDMONITER": {
					int ret = insertCommand(result[0], result[1], name, null);
					xmlDoc.append(ret > 0
							? "           <RESULT>" + sendMsgInfoManageService.selectMaxSeq() + "</RESULT>"
							: "           <RESULT>F</RESULT>");
					break;
				}
				case "SP_DIDENDTIME": {
					didinfo.setDidId(result[0]);
					didinfo.setDidMac(result[1]);
					didinfo.setDidEndTime(result[2]);
					int ret = didInfoManageService.updateDidEndTime(didinfo);
					int ret1 = insertCommand(result[0], result[1], name, result[2]);
					xmlDoc.append(ret > 0 && ret1 > 0
							? "           <RESULT><MSG_SEQ>" + sendMsgInfoManageService.selectMaxSeq() + "</MSG_SEQ><END_TIME>" + result[2] + "</END_TIME></RESULT>"
							: "           <RESULT>F</RESULT>");
					break;
				}
				case "SP_ORDERLST": {
					SendMsgInfoVO sminfo = new SendMsgInfoVO();
					sminfo.setDidId(result[0]);
					sminfo.setDidMacAddress(result[1]);
					xmlDoc.append("           <result>\r\n");
					for (SendMsgInfoVO r : sendMsgInfoManageService.selectDIDOrderLst(sminfo)) {
						xmlDoc.append("                <CONINFO>\r\n");
						xmlDoc.append("                       <MSG_SEQ>" + r.getMsgSeq() + "</MSG_SEQ>\r\n");
						xmlDoc.append("                       <XML_PROCESS_NAME>" + r.getXmlProcessName() + "</XML_PROCESS_NAME>\r\n");
						xmlDoc.append("                       <SEND_REGDATE>" + r.getSendRegDate() + "</SEND_REGDATE>\r\n");
						xmlDoc.append("                </CONINFO>\r\n");
					}
					xmlDoc.append("           </result>\r\n");
					break;
				}
				default:
					log.warn("xmlAuth 처리 분기 없음: {}", name);
					break;
			}

			xmlDoc.append("       </command_result>\r\n");
			xmlDoc.append("   </command>\r\n ");
			xmlDoc.append("</CM_Document>\r\n ");
			return xmlDoc.toString();
		} catch (Exception ex) {
			log.error("xmlAuth error: {}", ex.toString());
			return "ERROR:" + ex.toString();
		}
	}

	// ===================================================================
	// 공통 보조 메서드(원본 sendRed/brodCnt/basicCnt/scheduleDID/orderCnt/messageCnt)
	// ===================================================================

	/** 처리 결과를 TB_MESSAGEHISTORY에 남기고 OK/FALSE 문자열을 돌려준다 */
	private String sendRed(String didId, String xmlProcessName, int rnt, String errorMessage, String didMac) throws Exception {
		SendMsgInfo sendinfo = new SendMsgInfo();
		sendinfo.setDidId(didId);
		sendinfo.setXmlProcessName(xmlProcessName);
		sendinfo.setErrorMessage(errorMessage);
		sendinfo.setDidMacAddress(didMac);
		String result = rnt > 0 ? "OK" : "FALSE";
		sendinfo.setSendResult(result);
		sendMsgInfoManageService.insertSendMsgInfoManage(sendinfo);
		return result;
	}

	/** 음원 방송 스케줄 건수 */
	private String brodCnt(String didId, String didMac) throws Exception {
		BrodScheduleInfo vo = new BrodScheduleInfo();
		vo.setDidId(didId);
		vo.setDidMac(didMac);
		return String.valueOf(brodSchedule.selectBordScheduleCount(vo));
	}

	/** 다운로드할 기초 음원 코드(없으면 N) */
	private String basicCnt(String didId, String didMac) throws Exception {
		BasicBrodScheduleInfoVO vo = new BasicBrodScheduleInfoVO();
		vo.setDidId(didId.trim());
		vo.setDidMac(didMac.trim());
		String basicCode = schService.selectBasicBrodContentlDownCheck(vo);
		return basicCode == null ? "N" : basicCode;
	}

	private String scheduleDID(String didId, String didMac) throws Exception {
		return Integer.toString(sendMsgInfoManageService.selectDIDScheduleCount(sendMsgInfo(didId, didMac)));
	}

	private String orderCnt(String didId, String didMac) throws Exception {
		return Integer.toString(sendMsgInfoManageService.selectDIDOrderCount(sendMsgInfo(didId, didMac)));
	}

	private String messageCnt(String didId, String didMac) throws Exception {
		return Integer.toString(sendMsgInfoManageService.selectDIDMessageCount(sendMsgInfo(didId, didMac)));
	}

	private SendMsgInfo sendMsgInfo(String didId, String didMac) {
		SendMsgInfo info = new SendMsgInfo();
		info.setDidId(didId);
		info.setDidMacAddress(didMac);
		return info;
	}

	/** 재부팅/화면전송/종료시간 등 단말 명령 등록(SEND_RESULT=N) */
	private int insertCommand(String didId, String didMac, String processName, String errorMessage) throws Exception {
		SendMsgInfo sminfo = sendMsgInfo(didId, didMac);
		sminfo.setXmlProcessName(processName);
		sminfo.setErrorMessage(errorMessage);
		sminfo.setSendResult("N");
		return sendMsgInfoManageService.insertSendMsgInfoManage(sminfo);
	}

	/** 단말이 보낸 명령 처리 결과(OK/그 외) 갱신 */
	private int updateCommandResult(String didId, String didMac, String errorMessage, String msgSeq) throws Exception {
		SendMsgInfo sendInfo = sendMsgInfo(didId, didMac);
		sendInfo.setSendResult("OK".equals(errorMessage) ? "OK" : "FALSE");
		sendInfo.setErrorMessage(errorMessage);
		sendInfo.setMsgSeq(msgSeq);
		return sendMsgInfoManageService.updateSendMsgInfoManage(sendInfo);
	}

	private int insertMonitorPic(String didId, String didMac, String msgSeq, String fileNm) throws Exception {
		DidMoniterPicVO picVo = new DidMoniterPicVO();
		picVo.setDidId(didId);
		picVo.setDidMac(didMac);
		picVo.setMsgSeq(msgSeq);
		picVo.setDidFileNm(fileNm);
		return picService.insertDidMoniterPicManage(picVo);
	}

	private int updatePicSendResult(String msgSeq, int picRet) throws Exception {
		SendMsgInfo sendInfo = new SendMsgInfo();
		sendInfo.setMsgSeq(msgSeq);
		sendInfo.setSendResult(picRet > 0 ? "Y" : "N");
		sendInfo.setErrorMessage(picRet > 0 ? "" : "DataBase Update Error");
		return sendMsgInfoManageService.updateSendMsgInfoManage(sendInfo);
	}

	/** HIS_SEQ "a|b|c" 각각의 페이지 전송 이력을 갱신(마지막 결과 반환 — 원본과 동일) */
	private int updatePageHistory(String hisSeqs) throws Exception {
		int resultUpdate = 0;
		for (String hisSeq : HIS_SEQ_SPLIT.split(hisSeqs)) {
			if (!hisSeq.isBlank()) {
				resultUpdate = sendHistory.updateContentSendHistoryConPageInfoManage(hisSeq.trim());
			}
		}
		return resultUpdate;
	}

	// ----- 문자열/응답 조립 -----

	private static String str(JSONObject obj, String key) {
		Object v = obj.get(key);
		return v == null ? "" : v.toString();
	}

	private static String nvl(String s) {
		return s == null ? "" : s;
	}

	/** DB에 URL 인코딩돼 저장된 한글 값을 복원(원본 URLDecoder.decode와 동일, null은 빈 문자열) */
	private static String decode(String s) {
		return s == null ? "" : URLDecoder.decode(s, StandardCharsets.UTF_8);
	}

	private static String simpleResult(String commandType, String result) {
		return "{'command_type':'" + commandType + "','result':'" + result + "'}";
	}

	private static String okOrFail(String commandType, int ret) {
		return simpleResult(commandType, ret > 0 ? "O" : "F");
	}

	private static String xmlOkOrFail(int ret) {
		return ret > 0 ? "           <result>O</result>" : "           <result>F</result>";
	}

	@SuppressWarnings("unchecked")
	private static String listResult(String commandType, String listKey, JSONArray dataArray) {
		JSONObject obj = new JSONObject();
		obj.put("command_type", commandType);
		obj.put(listKey, dataArray);
		return obj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	private static String listResultWithOk(String commandType, JSONArray dataArray) {
		JSONObject obj = new JSONObject();
		obj.put("FILEINFO", dataArray);
		obj.put("command_type", commandType);
		obj.put("result", "O");
		return obj.toJSONString();
	}

	private static Document parseXml(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// 장비 입력 XML — 외부 엔티티(XXE) 차단
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
	}

	private static String commandType(Document doc) throws Exception {
		XPath xpath = XPathFactory.newInstance().newXPath();
		return xpath.compile("//*/command_type").evaluate(doc);
	}
}
