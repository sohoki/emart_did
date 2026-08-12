package com.common.backoffice.sts.cnt.web;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.cnt.modals.*;
import com.common.backoffice.sts.cnt.service.ContentDetailFileInfoManageService;
import com.common.backoffice.sts.cnt.service.ContentDetailInfoService;
import com.common.backoffice.sts.cnt.service.ContentMutimanageService;
import com.common.backoffice.sym.grp.modals.GroupDidInfoVO;
import com.common.backoffice.sym.grp.service.GroupDidInfoManageService;
import com.common.backoffice.sym.sch.modals.ContentSendHistoryInfo;
import com.common.backoffice.sym.sch.modals.ScheduleInfo;
import com.common.backoffice.sym.sch.service.ContentSendHistoryInfoManagerService;
import com.common.backoffice.sym.sch.service.ScheduleInfoManageService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import net.sf.json.JSONArray;
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


    @Value("${Common.filePath}")
    private String filePath;


	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;

	private final ContentDetailFileInfoManageService conFileinfo;
	private final ContentDetailInfoService contentDetail;
    private final ScheduleInfoManageService scheduleInfoManageService;
    private final ContentMutimanageService contentMuti;
    private final ContentSendHistoryInfoManagerService SendHistory;
    private final GroupDidInfoManageService didInfo;

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

	@Operation(summary = "콘텐츠 재생시간 미입력 파일 존재여부 확인",
            description = "미리보기 전 시간이 비어있는 파일이 있는지 확인합니다."
    )
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

    @Operation(summary = "콘텐츠 재생시간 미입력 파일 존재여부 확인",
            description = "미리보기 전 시간이 비어있는 파일이 있는지 확인합니다."
    )
    @GetMapping("/preViewCheck.do")
    public ResultVO selectPreViewCheck(@RequestParam("conSeq") String conSeq,
                                       HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            int ret = conFileinfo.selectTimeIntevalNullCheck(conSeq);


            String status = (ret < 1) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = (ret < 1) ? egovMessageSource.getMessage("success.common.msg") : egovMessageSource.getMessage("fail.request.msg");
            int res = ret < 1 ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "contentTimeCheck", e, egovMessageSource);
        }
        return resultVO;
    }
    @Operation(summary = "콘텐츠 미리보기 확인",
            description = "콘텐츠 미리보기."
    )
    @GetMapping("/contentPreview.do")
    public ResultVO selectContentPreview(@RequestParam("conSeq") String conSeq,
                                       HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            String jsonTest = conPageDetail(conSeq , "S");
            String fileUrl = ContentFileCreate(jsonTest, conSeq);
            boolean fileCheck = fileUrl != null;
            if (fileCheck){
                log.debug("fileCheck:true" );
                String jsonTestLocal = conPageDetail(conSeq , "L");
                ContentFileCreateLocal(jsonTestLocal, conSeq);
            }

            String status = fileCheck ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = fileCheck ? egovMessageSource.getMessage("success.common.msg") : egovMessageSource.getMessage("fail.request.msg");
            int res = fileCheck ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);
            if (fileCheck) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("fileUrl", fileUrl);
                resultVO.setResult(resultMap);
            }
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "contentTimeCheck", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "콘텐츠 전송",
            description = "콘텐츠 전송."
    )
    @GetMapping("/contentScheduleSend.do")
    public ResultVO selectContentScheduleSend(@RequestParam("conSeq") String conSeq,
                                         HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;


            List<ScheduleInfo> schedule = scheduleInfoManageService.selectScheduleConSeqList(conSeq);

            for (int i = 0; i < schedule.size(); i++){


                int ret_result =0;
                ret_result = SendHistory.deleteContentSendHistoryInfoManage(schedule.get(i).getSchCode());
                ContentSendHistoryInfo sendHistory = new ContentSendHistoryInfo();
                List<GroupDidInfoVO> resultLst = didInfo.selectGroupInfoManageListByPagination(schedule.get(i).getGroupCode());

                //단말기 전송할 구문 넣기
                for (int a = 0 ; a < resultLst.size() ; a++    )
                {
                    sendHistory.setDidId(resultLst.get(a).getDidId().toString() );
                    sendHistory.setHisSeq("");
                    sendHistory.setSchCode(schedule.get(i).getSchCode());
                    ret_result = SendHistory.insertContentSendHistoryInfoManage(sendHistory);
                    if (  ret_result == 0 ){
                        log.error("단말단 개별 인서트 애러");
                    }
                }
            }


            String status =  Globals.STATUS_SUCCESS;
            String message =   egovMessageSource.getMessage("success.common.msg") ;
            int res = ResponseCode.SUCCESS.getCode();

            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "contentTimeCheck", e, egovMessageSource);
        }
        return resultVO;
    }



	@Operation(summary = "상세페이지에 파일 연결 등록",
                description = "동영상/음원 파일이면 실제 재생시간으로 timeInterval을 보정하고, 상세페이지 총 재생시간을 갱신합니다."
    )
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


    // 성공 시 생성된 파일의 upload 루트 기준 상대경로(EMART_DID/did/upload/{yyyyMM}/{fileName})를
    // 반환한다. DB(CON_FILE)는 character varying(30) 제약이라 상대경로 전체를 담을 수 없어서
    // 파일명만 저장하고, 실제 미리보기 URL은 이 반환값으로 프론트에 전달한다.
    public String ContentFileCreate (String htmlFile,
                                      String conSeq){
        try{
            // 일반 업로드 미디어와 동일한 관례(ContentFileInfoManageController.uploadFileManage 참고)로
            // EMART_DID/did/upload/{yyyyMM}/ 하위에 생성한다 — upload 루트에 바로 만들지 않음.
            Path rootPath = Paths.get(filePath);
            String yyyyMM = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path dirPath = rootPath.resolve("EMART_DID").resolve("did").resolve("upload").resolve(yyyyMM);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName = "";
            String existing = contentMuti.selectContentFileInfo(conSeq);

            if (!"N".equals(existing)) {
                Path targetFile = rootPath.resolve(existing);
                if (Files.isRegularFile(targetFile)) {
                    Files.delete(targetFile);
                }
                fileName = Paths.get(existing).getFileName().toString();
            } else {
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fileName = currentDate + "_" + conSeq + ".html";
            }

            Path filePathObj = dirPath.resolve(fileName);
            try (BufferedWriter fw = Files.newBufferedWriter(filePathObj,
                                                            StandardCharsets.UTF_8,
                                                            StandardOpenOption.CREATE,
                                                            StandardOpenOption.APPEND)) {
                fw.write(htmlFile);
                fw.flush();
            }

            // DB는 varchar(30)라 파일명만 저장(기존 관례 유지) — 실제 폴더 경로는 반환값으로만 전달
            ContentMutiInfo vo = new ContentMutiInfo();
            vo.setConSeq(conSeq);
            vo.setConFile(fileName);

            int ret = contentMuti.updateContentMutiFile(vo);
            if (ret <= 0) {
                return null;
            }
            return "EMART_DID/did/upload/" + yyyyMM + "/" + fileName;
        }catch(Exception e){
            log.error("CREATE HTML FILE ERROR:" + e.toString()  );
        }
        return null;
    }
    public String conPageDetail(String conSeq, String playGubun) {
        StringBuilder htmlPage = new StringBuilder();

        try {
            ContentMutiInfoVO vo_info = contentMuti.selectContentMutiInfoManageView(conSeq);
            // 기초 정보
            String width = vo_info.getConWidth();
            String height = vo_info.getConHeight();
            String mid = vo_info.getConMid();
            String conDc = vo_info.getConDc();

            int viewType = 0;

            if (conDc == null || conDc.isEmpty() || conDc.equals("0")) {
                conDc = "1";
            }

            String width0 = "";
            String width1 = "";
            String height0 = "";
            String height1 = "";

            log.info("width : {}, height : {}, conDc : {}", width, height, conDc);

            // 변수값 설정
            int widthVal = Integer.parseInt(width);
            int heightVal = Integer.parseInt(height);
            int conDcVal = Integer.parseInt(conDc);
            int midVal = Integer.parseInt(mid);

            if (widthVal > heightVal && conDcVal > 1) {
                viewType = 1;
                width0 = mid;
                width1 = Integer.toString(widthVal - midVal);
                height0 = height;
                height1 = height;
            } else if (widthVal < heightVal && conDcVal > 1) {
                viewType = 2;
                width0 = width;
                width1 = width;
                height0 = mid;
                height1 = Integer.toString(heightVal - midVal);
            } else {
                viewType = 0;
                width0 = width;
                height0 = height;
                width1 = width;
                height1 = height;
            }

            /**
             * 콘텐츠 화면 생성 부 !
             */
            htmlPage.append("<!DOCTYPE HTML>\r\n");
            htmlPage.append("<html>\r\n");
            htmlPage.append("<head>\r\n");
            htmlPage.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\r\n");
            htmlPage.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\r\n");
            htmlPage.append("<title>").append(vo_info.getConNm()).append("</title>\r\n");

            // jquery-4.0.0.min.js를 upload 루트(webapp/upload/jquery-4.0.0.min.js, "/upload/**"로
            // 정적 서빙됨 — WebMvcConfig 참고)에 배치해 둠. "L"(로컬) 재생은 단말기에 HTML과 같이
            // 내려받아 저장해 두는 방식이라 상대경로(./)로 참조하고, "S"(서버) 재생은 서버에서
            // 바로 내려주므로 절대경로(/upload/)로 참조한다.
            if ("L".equals(playGubun)) {
                htmlPage.append("<script type='text/javascript' src='./jquery-4.0.0.min.js'></script>\r\n");
            } else {
                htmlPage.append("<script type='text/javascript' src='/upload/jquery-4.0.0.min.js'></script>\r\n");
            }

            List<ContentDetailInfo> detailInfo = contentDetail.selectContentDetailLst(conSeq);

            htmlPage.append("<script type='text/javascript'>\r\n");

            for (int i = 0; i < detailInfo.size(); i++) {
                ContentDetailFileInfoVO searchVO = new ContentDetailFileInfoVO();
                searchVO.setConSeq(conSeq);
                searchVO.setDetailSeq(detailInfo.get(i).getDetailSeq());
                List<ContentDetailFileInfoVO> detailFileContent = conFileinfo.selectContentDetailFileLst(searchVO);
                JSONArray jsonA = JSONArray.fromObject(detailFileContent);

                if (i == 0 && !detailFileContent.isEmpty()) {
                    System.out.println(detailFileContent.get(0).getAtchFileId());
                    System.out.println(detailFileContent.get(0).getMediaType());
                }
                htmlPage.append("    var albumLst").append(i).append(" = '").append(jsonA).append("';\r\n");
                htmlPage.append("    var contentCount").append(i).append(" = 0;\r\n");
                htmlPage.append("    var jsonData").append(i).append(";\r\n");
                htmlPage.append("    var firstPlay").append(i).append(", errorFlag").append(i).append(";\r\n");
                htmlPage.append("    var prepareFileNm").append(i).append(", prepareFileType").append(i)
                        .append(", prepareFileTime").append(i).append(", prepareFileStreCours").append(i)
                        .append(", prepareMakeType").append(i).append(";\r\n");
            }

            // onRead
            htmlPage.append("    $(document).ready(function(){\r\n");
            htmlPage.append("        console.log('contents play start');\r\n");
            htmlPage.append("        function startContentSch(){\r\n");
            for (int i = 0; i < detailInfo.size(); i++) {
                htmlPage.append("            jsonData").append(i).append(" = JSON.parse(albumLst").append(i).append(");\r\n");
                htmlPage.append("            if(jsonData").append(i).append(".length > 0){\r\n");
                htmlPage.append("                firstPlay").append(i).append(" = true;\r\n");
                htmlPage.append("                tagMaking").append(i)
                        .append("(jsonData").append(i).append("[contentCount").append(i).append("].streFileNm, ")
                        .append("jsonData").append(i).append("[contentCount").append(i).append("].mediaType, ")
                        .append("jsonData").append(i).append("[contentCount").append(i).append("].timeInterval, ")
                        .append("jsonData").append(i).append("[contentCount").append(i).append("].fileStreCours, 'A');\r\n");
                htmlPage.append("            }\r\n");
            }
            htmlPage.append("        }\r\n");
            htmlPage.append("        startContentSch();\r\n");
            htmlPage.append("    });\r\n");

            for (int i = 0; i < detailInfo.size(); i++) {
                htmlPage.append("    function tagMaking").append(i).append("(fileNm, fileType, fileInterval, fileStreCours, makeType){\r\n");
                htmlPage.append("        var result, mediaTag;\r\n");

                if ("L".equals(playGubun)) {
                    htmlPage.append("        if (makeType == 'A'){\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("a').html('');\r\n");
                    htmlPage.append("            switch(fileType){\r\n");
                    if (i == 0) {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("a\" src=\"./'+fileNm+'\" width=\"").append(width0).append("\" height=\"").append(height0).append("\" />'; break;\r\n");
                    } else {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("a\" src=\"./'+fileNm+'\" width=\"").append(width1).append("\" height=\"").append(height1).append("\" />'; break;\r\n");
                    }
                    htmlPage.append("                case 'MEDIA' : result = '<video id=\"content").append(i).append("a\" src=\"./'+fileNm+'\"><source type=\"video/mp4\"/></video>'; break;\r\n");
                    htmlPage.append("                default      : result = '<audio id=\"content").append(i).append("a\" controls src=\"./'+fileNm+'\"><source type=\"audio/mpeg\"/></audio>'; break;\r\n");
                    htmlPage.append("            }\r\n");
                    htmlPage.append("            mediaTag = $('#content").append(i).append("a');\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("a').html(result);\r\n");
                    htmlPage.append("        } else {\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("b').html('');\r\n");
                    htmlPage.append("            switch(fileType){\r\n");
                    if (i == 0) {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("b\" src=\"./'+fileNm+'\" width=\"").append(width0).append("\" height=\"").append(height0).append("\" />'; break;\r\n");
                    } else {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("b\" src=\"./'+fileNm+'\" width=\"").append(width1).append("\" height=\"").append(height1).append("\" />'; break;\r\n");
                    }
                    htmlPage.append("                case 'MEDIA' : result = '<video id=\"content").append(i).append("b\" src=\"./'+fileNm+'\"><source type=\"video/mp4\"/></video>'; break;\r\n");
                    htmlPage.append("                default      : result = '<audio id=\"content").append(i).append("b\" controls src=\"./'+fileNm+'\"><source type=\"audio/mpeg\"/></audio>'; break;\r\n");
                    htmlPage.append("            }\r\n");
                    htmlPage.append("            mediaTag = $('#content").append(i).append("b');\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("b').html(result);\r\n");
                    htmlPage.append("        }\r\n");
                } else {
                    htmlPage.append("        if (makeType == 'A'){\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("a').html('');\r\n");
                    htmlPage.append("            switch(fileType){\r\n");
                    if (i == 0) {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("a\" src=\"'+fileStreCours+fileNm+'\" width=\"").append(width0).append("\" height=\"").append(height0).append("\" />'; break;\r\n");
                    } else {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("a\" src=\"'+fileStreCours+fileNm+'\" width=\"").append(width1).append("\" height=\"").append(height1).append("\" />'; break;\r\n");
                    }
                    htmlPage.append("                case 'MEDIA' : result = '<video id=\"content").append(i).append("a\" src=\"'+fileStreCours+fileNm+'\"><source type=\"video/mp4\"/></video>'; break;\r\n");
                    htmlPage.append("                default      : result = '<audio id=\"content").append(i).append("a\" controls src=\"'+fileStreCours+fileNm+'\"><source type=\"audio/mpeg\"/></audio>'; break;\r\n");
                    htmlPage.append("            }\r\n");
                    htmlPage.append("            mediaTag = $('#content").append(i).append("a');\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("a').html(result);\r\n");
                    htmlPage.append("        } else {\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("b').html('');\r\n");
                    htmlPage.append("            switch(fileType){\r\n");
                    if (i == 0) {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("b\" src=\"'+fileStreCours+fileNm+'\" width=\"").append(width0).append("\" height=\"").append(height0).append("\" />'; break;\r\n");
                    } else {
                        htmlPage.append("                case 'IMAGE' : result = '<img id=\"content").append(i).append("b\" src=\"'+fileStreCours+fileNm+'\" width=\"").append(width1).append("\" height=\"").append(height1).append("\" />'; break;\r\n");
                    }
                    htmlPage.append("                case 'MEDIA' : result = '<video id=\"content").append(i).append("b\" src=\"'+fileStreCours+fileNm+'\"><source type=\"video/mp4\"/></video>'; break;\r\n");
                    htmlPage.append("                default      : result = '<audio id=\"content").append(i).append("b\" controls src=\"'+fileStreCours+fileNm+'\"><source type=\"audio/mpeg\"/></audio>'; break;\r\n");
                    htmlPage.append("            }\r\n");
                    htmlPage.append("            mediaTag = $('#content").append(i).append("b');\r\n");
                    htmlPage.append("            $('#content_show").append(i).append("b').html(result);\r\n");
                    htmlPage.append("        }\r\n");
                }
                htmlPage.append("        if(firstPlay").append(i).append("){\r\n");
                htmlPage.append("            firstPlay").append(i).append(" = false;\r\n");
                htmlPage.append("            mediaPlaySetting").append(i).append("(fileNm, fileType, fileInterval, fileStreCours, makeType);\r\n");
                htmlPage.append("        } else if (errorFlag").append(i).append("){\r\n");
                htmlPage.append("            errorFlag").append(i).append(" = false;\r\n");
                htmlPage.append("            mediaPlaySetting").append(i).append("(fileNm, fileType, fileInterval, fileStreCours, makeType);\r\n");
                htmlPage.append("        } else {\r\n");
                htmlPage.append("            prepareFileNm").append(i).append(" = fileNm;\r\n");
                htmlPage.append("            prepareFileType").append(i).append(" = fileType;\r\n");
                htmlPage.append("            prepareFileTime").append(i).append(" = fileInterval;\r\n");
                htmlPage.append("            prepareFileStreCours").append(i).append(" = fileStreCours;\r\n");
                htmlPage.append("            prepareMakeType").append(i).append(" = makeType;\r\n");
                htmlPage.append("        }\r\n");
                htmlPage.append("    }\r\n");
            }

            for (int i = 0; i < detailInfo.size(); i++) {
                htmlPage.append("    function mediaPlaySetting").append(i).append("(fileNm, fileType, fileInterval, fileStreCours, makeType){\r\n");
                htmlPage.append("        var mediaTag;\r\n");
                htmlPage.append("        var contentLoadCheck = setTimeout(function(){\r\n");
                htmlPage.append("            console.log('CONTENTS LOADING ISSUE, NEXT !');\r\n");
                htmlPage.append("            nextSeqConChk").append(i).append("('1', makeTypeRevers(makeType));\r\n");
                htmlPage.append("        }, 3000);\r\n");
                htmlPage.append("        var contentPauseChk = setTimeout(function(){\r\n");
                htmlPage.append("            console.log('content Pause ? Play Time : ' + ((fileInterval*1000)+3000));\r\n");
                htmlPage.append("            nextSeqConChk").append(i).append("('2', prepareMakeType").append(i).append(");\r\n");
                htmlPage.append("        }, ((fileInterval*1000)+3000));\r\n");
                htmlPage.append("        switch(makeType){\r\n");
                htmlPage.append("            case 'A' : mediaTag = $('#content").append(i).append("a'); $('#content_show").append(i).append("b').css('z-index', '150'); break;\r\n");
                htmlPage.append("            case 'B' : mediaTag = $('#content").append(i).append("b'); $('#content_show").append(i).append("b').css('z-index', '300'); break;\r\n");
                htmlPage.append("        }\r\n");
                htmlPage.append("        if(fileType == 'IMAGE'){\r\n");
                htmlPage.append("            mediaTag.each(function(){\r\n");
                htmlPage.append("                clearTimeout(contentLoadCheck);\r\n");
                htmlPage.append("                setTimeout(function(){\r\n");
                htmlPage.append("                    clearTimeout(contentPauseChk);\r\n");
                htmlPage.append("                    nextSeqConChk").append(i).append("('2', prepareMakeType").append(i).append(");\r\n");
                htmlPage.append("                }, fileInterval*1000);\r\n");
                htmlPage.append("                nextSeqConChk").append(i).append("('1', makeTypeRevers(makeType));\r\n");
                htmlPage.append("            });\r\n");
                htmlPage.append("        } else {\r\n");
                htmlPage.append("            mediaTag[0].autoplay = true;\r\n");
                if ("L".equals(playGubun)) {
                    htmlPage.append("            mediaTag[0].src = './' + fileNm + '?autoplay=1';\r\n");
                } else {
                    htmlPage.append("            mediaTag[0].src = fileStreCours+fileNm + '?autoplay=1';\r\n");
                }
                htmlPage.append("            mediaTag.on('loadeddata', function(){\r\n");
                htmlPage.append("                clearTimeout(contentLoadCheck);\r\n");
                htmlPage.append("                nextSeqConChk").append(i).append("('1', makeTypeRevers(makeType));\r\n");
                htmlPage.append("            });\r\n");
                htmlPage.append("            mediaTag.on('ended', function(){\r\n");
                htmlPage.append("                clearTimeout(contentPauseChk);\r\n");
                htmlPage.append("                nextSeqConChk").append(i).append("('2', prepareMakeType").append(i).append(");\r\n");
                htmlPage.append("            });\r\n");
                htmlPage.append("        }\r\n");
                htmlPage.append("        mediaTag.on('error', function(err){\r\n");
                htmlPage.append("            clearTimeout(contentLoadCheck);\r\n");
                htmlPage.append("            clearTimeout(contentPauseChk);\r\n");
                htmlPage.append("            console.log('CONTENT NAME : ' + fileNm + ', ERROR !');\r\n");
                htmlPage.append("            errorFlag").append(i).append(" = true;\r\n");
                htmlPage.append("            nextSeqConChk").append(i).append("('1', makeType);\r\n");
                htmlPage.append("        });\r\n");
                htmlPage.append("    }\r\n");

                htmlPage.append("    function nextSeqConChk").append(i).append("(checkType, makeType){\r\n");
                htmlPage.append("        if(contentCount").append(i).append(" == jsonData").append(i).append(".length){\r\n");
                htmlPage.append("            location.href = document.location.href;\r\n");
                htmlPage.append("        } else {\r\n");
                htmlPage.append("            if(checkType == '1'){\r\n");
                htmlPage.append("                contentCount").append(i).append("++;\r\n");
                htmlPage.append("                if(contentCount").append(i).append(" != jsonData").append(i).append(".length){\r\n");
                htmlPage.append("                    tagMaking").append(i)
                        .append("(jsonData").append(i).append("[contentCount").append(i).append("].streFileNm, ")
                        .append("jsonData").append(i).append("[contentCount").append(i).append("].mediaType, ")
                        .append("jsonData").append(i).append("[contentCount").append(i).append("].timeInterval, ")
                        .append("jsonData").append(i).append("[contentCount").append(i).append("].fileStreCours, makeType);\r\n");
                htmlPage.append("                }\r\n");
                htmlPage.append("            } else {\r\n");
                htmlPage.append("                mediaPlaySetting").append(i)
                        .append("(prepareFileNm").append(i).append(", prepareFileType").append(i)
                        .append(", prepareFileTime").append(i).append(", prepareFileStreCours").append(i)
                        .append(", prepareMakeType").append(i).append(");\r\n");
                htmlPage.append("            }\r\n");
                htmlPage.append("        }\r\n");
                htmlPage.append("    }\r\n");
            }

            htmlPage.append("    function makeTypeRevers(makeType){\r\n");
            htmlPage.append("        if(makeType == 'A'){\r\n");
            htmlPage.append("            return 'B';\r\n");
            htmlPage.append("        } else {\r\n");
            htmlPage.append("            return 'A';\r\n");
            htmlPage.append("        }\r\n");
            htmlPage.append("    }\r\n");
            htmlPage.append("</script>\r\n");

            if (vo_info.getConNextSeq() != null && !vo_info.getConNextSeq().isEmpty() && !"0".equals(vo_info.getConNextSeq())) {
                htmlPage.append("<script type='text/javascript'> \r\n");
                String pageUrl = "";
                if ("L".equals(playGubun)) {
                    pageUrl = contentMuti.selectContentFileInfoLocal(vo_info.getConNextSeq());
                } else {
                    pageUrl = contentMuti.selectContentFileInfo(vo_info.getConNextSeq());
                }
                String pageTime = contentMuti.selectMaxTimeInterval(vo_info.getConNextSeq());
                int timeoutSec = (pageTime != null && !pageTime.isEmpty()) ? Integer.parseInt(pageTime) * 1000 : 0;

                htmlPage.append("setTimeout('nextPage()', ").append(timeoutSec).append(");\r\n");
                htmlPage.append("function nextPage(){\r\n");
                htmlPage.append("    location.href='").append(pageUrl).append("';\r\n");
                htmlPage.append("}\r\n");
                htmlPage.append("</script>\r\n");
            }

            htmlPage.append("<style>\r\n");
            htmlPage.append("    body{\r\n");
            htmlPage.append("        margin : 0;\r\n");
            htmlPage.append("        background-color : #000;\r\n");
            htmlPage.append("    }\r\n");

            for (int i = 0; i < detailInfo.size(); i++) {
                htmlPage.append("    .contentBox").append(i).append("{\r\n");
                htmlPage.append("        position : fixed;\r\n");
                htmlPage.append("        top : 0;\r\n");
                if (i == 0) {
                    htmlPage.append("        width: ").append(width0).append("px; height: ").append(height0).append("px;\r\n");
                } else {
                    htmlPage.append("        width: ").append(width1).append("px; height: ").append(height1).append("px;\r\n");
                    if (viewType == 1) {
                        htmlPage.append("        left: ").append(mid).append("px;\r\n");
                    } else if (viewType == 2) {
                        htmlPage.append("        top: ").append(mid).append("px;\r\n");
                    }
                }
                htmlPage.append("        float : left;\r\n");
                htmlPage.append("        background-color : #000;\r\n");
                htmlPage.append("    }\r\n");

                htmlPage.append("    .contentBox").append(i).append(" video{\r\n");
                if (viewType == 0) {
                    htmlPage.append("        width: ").append(width0).append("px; height: ").append(height0).append("px;\r\n");
                    htmlPage.append("        min-width: ").append(width0).append("px; min-height: ").append(height0).append("px;\r\n");
                } else {
                    if (i == 0) {
                        htmlPage.append("        width: ").append(width0).append("px; height: ").append(height0).append("px;\r\n");
                        htmlPage.append("        min-width: ").append(width0).append("px; min-height: ").append(height0).append("px;\r\n");
                    } else {
                        htmlPage.append("        width: ").append(width1).append("px; height: ").append(height1).append("px;\r\n");
                        htmlPage.append("        min-width: ").append(width1).append("px; min-height: ").append(height1).append("px;\r\n");
                    }
                    htmlPage.append("        object-fit: fill;\r\n");
                }
                htmlPage.append("    }\r\n");
            }

            htmlPage.append("</style>\r\n");
            htmlPage.append("</head>\r\n");
            htmlPage.append("<body>\r\n");

            if ("L".equals(playGubun)) {
                htmlPage.append("    <iframe src='./silence.mp3' allow='autoplay' id='setAudio' style='display:none'></iframe>\r\n");
            } else {
                htmlPage.append("    <iframe src='/upload/silence.mp3' allow='autoplay' id='setAudio' style='display:none'></iframe>\r\n");
            }

            for (int i = 0; i < detailInfo.size(); i++) {
                htmlPage.append("    <div id='content_show").append(i).append("a' class='contentBox").append(i).append("' style='z-index:200;'></div>\r\n");
                htmlPage.append("    <div id='content_show").append(i).append("b' class='contentBox").append(i).append("' style='z-index:150;'></div>\r\n");
            }
            htmlPage.append("</body>\r\n");
            htmlPage.append("</html>\r\n");

        } catch (Exception e) {
            htmlPage.append(" 페이지 애러:").append(e.toString());
            e.printStackTrace();
        }

        return htmlPage.toString();
    }
    // 성공 시 생성된 파일의 upload 루트 기준 상대경로를 반환(ContentFileCreate와 동일한 이유로
    // DB에는 파일명만 저장).
    public String ContentFileCreateLocal(String htmlFile, String conSeq) {
        try {

            String existing = contentMuti.selectContentFileInfoLocal(conSeq);

            // 일반 업로드 미디어와 동일한 관례로 EMART_DID/did/upload/{yyyyMM}/ 하위에 생성한다.
            Path rootPath = Paths.get(filePath);
            String yyyyMM = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path dirPath = rootPath.resolve("EMART_DID").resolve("did").resolve("upload").resolve(yyyyMM);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileName;
            if (!"N".equals(existing)) {
                Path targetFile = rootPath.resolve(existing);
                if (Files.isRegularFile(targetFile)) {
                    Files.delete(targetFile);
                }
                fileName = Paths.get(existing).getFileName().toString();
            } else {
                log.info("conSeq pre: {}", conSeq);
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fileName = currentDate + "_" + conSeq + "_local.html";
                log.info("fileName Pre: {}", fileName);
            }

            Path filePathObj = dirPath.resolve(fileName);

            // Try-with-resources 및 UTF-8 인코딩 명시
            try (BufferedWriter fw = Files.newBufferedWriter(filePathObj, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                fw.write(htmlFile);
                fw.flush();
            }

            // DB는 varchar(30)라 파일명만 저장 — 실제 폴더 경로는 반환값으로만 전달
            ContentMutiInfo vo = new ContentMutiInfo();
            vo.setConSeq(conSeq);
            log.debug("fileName: {}", fileName);
            vo.setConLocalfile(fileName);

            int ret = contentMuti.updateContentMutiFileLocal(vo);
            if (ret <= 0) {
                return null;
            }
            return "EMART_DID/did/upload/" + yyyyMM + "/" + fileName;

        } catch (Exception e) {
            log.error("CREATE HTML FILE ERROR: {}", e.toString());
            return null;
        }
    }
}
