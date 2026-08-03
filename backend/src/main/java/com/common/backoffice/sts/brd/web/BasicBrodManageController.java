package com.common.backoffice.sts.brd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.bas.cnt.service.CenterInfoManageService;
import com.common.backoffice.bas.code.service.EgovCcmCmmnDetailCodeManageService;
import com.common.backoffice.sts.brd.modals.BasicBrodFileInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodFileInfoVO;
import com.common.backoffice.sts.brd.modals.BasicBrodFileIntervalInfoVO;
import com.common.backoffice.sts.brd.modals.BasicBrodInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodInfoVO;
import com.common.backoffice.sts.brd.modals.BasicBrodScheduleInfoVO;
import com.common.backoffice.sts.brd.modals.BasicFileGroupInfoVO;
import com.common.backoffice.sts.brd.modals.BasicFileGroupPlayInfoVO;
import com.common.backoffice.sts.brd.modals.BrodContentInfo;
import com.common.backoffice.sts.brd.service.BasicBrodFileInfoManageService;
import com.common.backoffice.sts.brd.service.BasicBrodFileIntervalInfoManageService;
import com.common.backoffice.sts.brd.service.BasicBrodInfoManageService;
import com.common.backoffice.sts.brd.service.BasicBrodScheduleInfoManageService;
import com.common.backoffice.sts.brd.service.BasicFileGroupInfoManageService;
import com.common.backoffice.sts.brd.service.BasicFileGroupPlayInfoManageService;
import com.common.backoffice.sts.brd.service.BrodContentInfoManageService;
import com.common.backoffice.sts.cnt.modals.ContentFileInfoVO;
import com.common.backoffice.sts.cnt.service.ContentFileInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import egovframework.let.utl.fcc.service.EgovDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기초 방송(방송 템플릿/그룹) 관리 API.
 * did_emart(REST + JWT + tb_brodbasicgroup, postgresql) 기준으로 리팩토링함.
 *
 * 원본(1098줄) 대비 변경된 부분:
 * - 원본은 앞부분(list.do~file-update, 약 440줄)만 REST(ResultVO/AuthHelper)로 이미 전환되어
 *   있었으나, `@GetMapping`/`@DeleteMapping`/`@RequestParam` import가 누락되어 있었고
 *   (있는데 안 쓴게 아니라 정말 없어서) `egovframework.let.sts.brd.service.*`(존재하지 않는
 *   패키지)를 잘못 import하고 있어 컴파일 자체가 안 되는 상태였음 — import만 바로잡음
 * - 나머지 뒷부분(brodBasicFileList.do 이후, 약 610줄)은 세션 기반 JSP 뷰 컨트롤러가 그대로
 *   남아있었음(마이그레이션이 중간에 멈춘 상태) — 이번에 REST로 전면 전환함
 * - 클래스 레벨 베이스 경로를 `/api/backoffice/sts/brd`에서 다른 방송 컨트롤러들과 통일되게
 *   `/api/backoffice/sub/brodManage/basic`로 변경함(기존 경로는 메서드별 경로가 전부
 *   `/backoffice/sub/...` 절대경로라 실제로는 이중 접두사로 깨져 있었음)
 * - `brodPlayInfoExelDown.do`/`brodPlayInfoNotCenterExelDown.do`(엑셀 다운로드)는 제외함 —
 *   `BrodPlayExcelView`/`BrodPlayNotCenterExcelView`라는 Spring View Bean을 반환하는데
 *   did_emart 어디에도 해당 View가 구성되어 있지 않아(원본에서도 이 컨트롤러 파일에서만
 *   참조되는 죽은 코드) 실제 엑셀 다운로드 재구현(POI 스트리밍 등)이 별도로 필요함(후속 작업)
 * - 신규 등록 시 원본은 DB 함수 FN_BASICCODE()로 BASIC_CODE를 채번했으나 해당 함수가 없어
 *   BasicBrodInfoManageService.generateBasicCode()에서 애플리케이션 레벨로 대체 채번함
 *   (BC+yyMMdd+2자리 순번, 기존 데이터 포맷과 일치 확인)
 * - **버그 발견/수정**: `BasicBrodManagerMapper.xml`의 `insertBasicBrodCopy`가 원본에서
 *   `WHERE BASIC_CODE = #{basicCode}`로 복사 대상을 찾았는데, 컨트롤러가 이 호출 직전에
 *   `vo.setBasicCode()`를 새 코드로 이미 덮어써서 실제로는 존재하지 않는(아직 INSERT 전인)
 *   코드로 조회하는 셈이라 항상 0건 복사되던 버그였음. `basicCodePre`(원본 코드 보관용 필드,
 *   VO에 이미 있었음)를 WHERE 조건으로 쓰도록 수정
 * - `basicbrodfile_seq`/`basicbrodsechedule_seq`/`basicgroup_seq`/`basicbrodintervalfile_seq`
 *   4개 시퀀스가 없어 db-encoding-fix/08_create_missing_sequences.sql로 신규 생성함(매퍼 SQL
 *   자체는 원본처럼 NEXTVAL을 그대로 사용 중이라 애플리케이션 코드 변경은 불필요했음)
 * - `basicDayInput.do`(팝업 날짜선택 폼, 값 변경 없이 그대로 반환)는 순수 뷰 셸이라 제외
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/sub/brodManage/basic")
public class BasicBrodManageController {

    private final EgovPropertyService propertiesService;
    private final EgovCcmCmmnDetailCodeManageService cmmnDetailCodeManageService;
    private final EgovMessageSource egovMessageSource;
    private final BasicBrodInfoManageService basicService;
    private final ContentFileInfoManageService conFileService;
    private final BasicBrodFileInfoManageService basicFileService;
    private final BasicBrodScheduleInfoManageService schService;
    private final BrodContentInfoManageService brodService;
    private final CenterInfoManageService centerInfoManageService;
    private final BasicFileGroupInfoManageService groupService;
    private final BasicBrodFileIntervalInfoManageService fileInterval;
    private final BasicFileGroupPlayInfoManageService groupPlayInfo;

    @Operation(summary = "기초 방송 목록 조회", description = "기초 방송 목록을 조회합니다.")
    @PostMapping("/list.do")
    public ResultVO selectBrodBasicList(@RequestBody BasicBrodInfoVO searchVO, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        Map<String, Object> resultMap = new HashMap<>();

        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            searchVO.setPageUnit(propertiesService.getInt("pageUnit"));
            searchVO.setPageSize(propertiesService.getInt("pageSize"));

            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
            paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
            paginationInfo.setPageSize(searchVO.getPageSize());

            searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
            searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
            searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

            List<BasicBrodInfoVO> resultList = basicService.selectBasicBrodLst(searchVO);
            int totCnt = basicService.selectBasicBrodPageCnt(searchVO);
            paginationInfo.setTotalRecordCount(totCnt);

            resultMap.put("resultList", resultList);
            resultMap.put("paginationInfo", paginationInfo);
            resultMap.put("totalCnt", totCnt);
            resultMap.put("regist", searchVO);

            resultVO.setResult(resultMap);
            resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
            resultVO.setResultMessage(ResponseCode.SUCCESS.getMessage());

        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectBrodBasicList", e, egovMessageSource);
        }

        return resultVO;
    }

    @Operation(summary = "기초 방송 삭제", description = "기초 방송 정보를 삭제합니다.")
    @DeleteMapping("/delete.do")
    public ResultVO basicBrodDel(@RequestBody Map<String, String> params, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            String delBasicSeq = params.getOrDefault("delBasicSeq", "");
            BasicBrodScheduleInfoVO vo = new BasicBrodScheduleInfoVO();
            int ret = 0;

            if (delBasicSeq.contains(",")) {
                String[] delBasicSeqs = delBasicSeq.split(",");
                for (String seq : delBasicSeqs) {
                    vo.setCreateCheck("E");
                    vo.setPreCreateCheck("Y");
                    vo.setBasicCode(seq);
                    schService.updateBasicBrodScheduleCenterStateChange(vo);
                    schService.updateBasicCodeCenterReset(seq);
                    basicFileService.deleteBasicBrodBasicCode(seq);
                    ret += basicService.deleteBasicBrod(seq);
                }
            } else if (!delBasicSeq.isEmpty()) {
                vo.setCreateCheck("E");
                vo.setPreCreateCheck("Y");
                vo.setBasicCode(delBasicSeq);
                schService.updateBasicBrodScheduleCenterStateChange(vo);
                schService.updateBasicCodeCenterReset(delBasicSeq);
                basicFileService.deleteBasicBrodBasicCode(delBasicSeq);
                ret = basicService.deleteBasicBrod(delBasicSeq);
            }

            ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);

        } catch (Exception e) {
            log.error("basicBrodDel error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicBrodDel", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 등록/수정/복사", description = "기초 방송 정보를 등록, 수정, 또는 복사합니다.")
    @PostMapping("/update.do")
    public ResultVO basicBrodUpdate(@RequestBody BasicBrodInfo vo, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }
            LoginVO loginVO = AuthHelper.getLoginVO();
            vo.setFrstRegisterId(loginVO.getManagerId());
            vo.setLastUpdusrId(loginVO.getManagerId());

            int ret = 0;
            String mode = vo.getMode();

            if ("Ins".equals(mode)) {
                vo.setBasicCode(basicService.selectBasicCode());
                ret = basicService.insertBasicBrod(vo);
            } else if ("Cpy".equals(mode)) {
                String cpbasicCode = vo.getBasicCode();
                vo.setBasicCode(basicService.selectBasicCode());
                vo.setBasicCodePre(cpbasicCode);
                ret = basicService.insertBasicBrodCopy(vo);

                BasicBrodFileInfoVO fileVo = new BasicBrodFileInfoVO();
                fileVo.setBasicCodeCp(cpbasicCode);
                fileVo.setBasicCode(vo.getBasicCode());
                basicFileService.insertBasicBrodFileCopy(fileVo);
            } else {
                ret = basicService.updateBasicBrod(vo);
            }

            if (ret > 0) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("basicCode", vo.getBasicCode());
                ResultHelper.setSuccess(resultVO, resultMap);
            } else {
                throw new Exception("Update failed");
            }

        } catch (Exception e) {
            log.error("basicBrodUpdate error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicBrodUpdate", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 콤보 목록", description = "기초 방송 콤보 목록을 조회합니다.")
    @GetMapping("/combo.do")
    public ResultVO basicCombo(HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("brodInfoLst", basicService.selectBasicBrodCombo());
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            log.error("basicCombo error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicCombo", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 센터 목록", description = "기초 방송 센터 목록을 조회합니다.")
    @GetMapping("/center-list")
    public ResultVO basicCenterlist(@RequestParam(required = false, defaultValue = "") String centerGubun, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("centerInfo", schService.selectBasicBrodScheduleCheckList(centerGubun));
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            log.error("basicCenterlist error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicCenterlist", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 센터 상태 S 업데이트", description = "기초 방송 센터 상태를 'S'로 업데이트합니다.")
    @PostMapping("/center-update-s")
    public ResultVO basicCenterUpdateS(@RequestBody Map<String, String> params, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }
            String basicCode = params.get("basicCode");
            BasicBrodScheduleInfoVO vo = new BasicBrodScheduleInfoVO();
            vo.setCreateCheck("Y");
            vo.setBasicCode(basicCode);
            schService.updateBasicBrodScheduleCenterStateChange(vo);
            ResultHelper.setSuccess(resultVO);
        } catch (Exception e) {
            log.error("basicCenterUpdateS error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicCenterUpdateS", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 센터 업데이트", description = "기초 방송의 센터 정보를 업데이트합니다.")
    @PostMapping("/center-update")
    public ResultVO basicCenterUpdate(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            String centerSeq = params.getOrDefault("CenterSeq", "").toString();
            String basicCode = params.getOrDefault("basicCode", "").toString();
            String checkValue = params.getOrDefault("checkValue", "").toString();

            BasicBrodScheduleInfoVO vo = new BasicBrodScheduleInfoVO();
            BrodContentInfo brodInfo = new BrodContentInfo();

            if (centerSeq.contains(",")) {
                String[] centerSeqs = centerSeq.split(",");
                for (String seq : centerSeqs) {
                    if (seq.trim().isEmpty()) continue;
                    updateCenterInfo(seq.trim(), basicCode, checkValue, vo, brodInfo);
                }
            } else if (!centerSeq.trim().isEmpty()) {
                updateCenterInfo(centerSeq.trim(), basicCode, checkValue, vo, brodInfo);
            }

            basicService.updateBasicBrodCnt(basicCode);
            ResultHelper.setSuccess(resultVO);

        } catch (Exception e) {
            log.error("basicCenterUpdate error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicCenterUpdate", e, egovMessageSource);
        }
        return resultVO;
    }

    private void updateCenterInfo(String centerId, String basicCode, String checkValue, BasicBrodScheduleInfoVO vo, BrodContentInfo brodInfo) throws Exception {
        vo.setCenterId(centerId);
        vo.setBasicCode(basicCode);

        schService.deleteBasicBrodScheduleOther(vo);

        brodInfo.setBrodCode(centerInfoManageService.selectCenterInfoBrod(vo.getCenterId()));
        brodInfo.setCenterId(vo.getCenterId());

        if ("Y".equals(checkValue)) {
            vo.setCreateCheck("E");
            schService.updateBasicBrodScheduleCenterE(vo);
            vo.setCreateCheck("Y");
            vo.setBasicCode(basicCode);
            schService.insertBasicBrodSchedule(vo);
            brodInfo.setBasicBrodCode(basicCode);
        } else {
            schService.deleteBasicBrodScheduleCenter(vo);
        }

        if (brodInfo.getBrodCode() != null) {
            brodService.updateBrodContentBasicFileInfo(brodInfo);
        }
    }

    @Operation(summary = "기초 방송 스케줄 리셋", description = "기초 방송 스케줄을 리셋합니다.")
    @PostMapping("/schedule-reset")
    public ResultVO basicScheduleReset(@RequestBody BasicBrodScheduleInfoVO vo, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }
            schService.updateBasicBrodScheduleCenterStateChange(vo);
            schService.insertBasicBrodScheduleDistribute(vo.getBasicCode());
            schService.deleteBasicBrodSchedule(vo);
            ResultHelper.setSuccess(resultVO);
        } catch (Exception e) {
            log.error("basicScheduleReset error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicScheduleReset", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 스케줄 목록", description = "기초 방송 스케줄 목록을 페이징하여 조회합니다.")
    @PostMapping("/schedule-list")
    public ResultVO selectBrodScheduleList(@RequestBody BasicBrodScheduleInfoVO searchVO, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
            paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
            paginationInfo.setPageSize(searchVO.getPageSize());

            searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
            searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
            searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

            int totCnt = schService.selectBasicBrodScheduleLstCnt(searchVO);
            paginationInfo.setTotalRecordCount(totCnt);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("schTotCnt", totCnt);
            resultMap.put("schList", schService.selectBasicBrodScheduleLst(searchVO));
            resultMap.put("paging", paginationInfo);

            ResultHelper.setSuccess(resultVO, resultMap);

        } catch (Exception e) {
            log.error("selectBrodScheduleList error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "selectBrodScheduleList", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 파일 체크 업데이트", description = "기초 방송 파일 체크 상태를 업데이트합니다.")
    @PostMapping("/file-check-update")
    public ResultVO basicAllLeftUpdate(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            String delBasicSeq = params.getOrDefault("delBasicSeq", "").toString();
            String basicCode = params.getOrDefault("basicCode", "").toString();
            String basicStartDay = params.getOrDefault("basicStartDay", "").toString();
            String basicEndDay = params.getOrDefault("basicEndDay", "").toString();
            String basicStartTime = params.getOrDefault("basicStartTime", "").toString();
            String basicEndTime = params.getOrDefault("basicEndTime", "").toString();
            String basicTimeDiv = params.getOrDefault("basicTimeDiv", "").toString();
            int pageIndex = Integer.parseInt(params.getOrDefault("pageIndex", "1").toString());
            int pageSize = Integer.parseInt(params.getOrDefault("pageSize", propertiesService.getInt("pageSize")).toString());
            String searchCondition = params.getOrDefault("searchCondition", "").toString();
            String searchKeyword = params.getOrDefault("searchKeyword", "").toString();

            BasicBrodFileInfo vo = new BasicBrodFileInfo();
            vo.setBasicCode(basicCode);

            if (!delBasicSeq.isEmpty()) {
                String[] delSeq = delBasicSeq.split(",");
                for (String seq : delSeq) {
                    vo.setAtchFileId(seq);
                    vo.setBasicOrder("10");
                    vo.setBasicStartDay(basicStartDay);
                    vo.setBasicEndDay(basicEndDay);
                    vo.setBasicStartTime(basicStartTime);
                    vo.setBasicEndTime(basicEndTime);
                    vo.setBasicTimeDiv(basicTimeDiv);
                    basicFileService.insertBasicBrodFile(vo);
                }
            }

            schService.updateBasicBrodScheduleState(basicCode);

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(basicCode);
            fileVO.setPageUnit(pageSize);
            fileVO.setPageSize(pageSize);
            fileVO.setPageIndex(pageIndex);
            fileVO.setSearchCondition(searchCondition);
            fileVO.setSearchKeyword(searchKeyword);

            Map<String, Object> resultMap = basicPagingInfo(fileVO);
            ResultHelper.setSuccess(resultVO, resultMap);

        } catch (Exception e) {
            log.error("basicAllLeftUpdate error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicAllLeftUpdate", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 파일 전체 삭제", description = "기초 방송 파일을 전체 삭제합니다.")
    @DeleteMapping("/file-all-del")
    public ResultVO basicAllDelRight(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            String delBasicSeq = params.getOrDefault("delBasicSeq", "").toString();
            String basicCode = params.getOrDefault("basicCode", "").toString();
            int pageIndex = Integer.parseInt(params.getOrDefault("pageIndex", "1").toString());
            int pageSize = Integer.parseInt(params.getOrDefault("pageSize", propertiesService.getInt("pageSize")).toString());

            if (!delBasicSeq.isEmpty()) {
                String[] delSeq = delBasicSeq.split(",");
                for (String seq : delSeq) {
                    if (!seq.trim().isEmpty()) {
                        basicFileService.deleteBasicBrodFile(seq.trim());
                    }
                }
            }

            schService.updateBasicBrodScheduleState(basicCode);

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(basicCode);
            fileVO.setPageUnit(pageSize);
            fileVO.setPageSize(pageSize);
            fileVO.setPageIndex(pageIndex);

            Map<String, Object> resultMap = basicPagingInfo(fileVO);
            ResultHelper.setSuccess(resultVO, resultMap);

        } catch (Exception e) {
            log.error("basicAllDelRight error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicAllDelRight", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 파일 우측 업데이트", description = "기초 방송 파일의 우측 영역을 업데이트합니다.")
    @PostMapping("/file-update")
    public ResultVO basicUpdateRight(@RequestBody BasicBrodFileInfoVO vo, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) {
                return resultVO;
            }

            if ("R".equals(vo.getFileGubun())) {
                basicFileService.insertBasicBrodFile(vo);
            } else {
                basicFileService.deleteBasicBrodFile(vo.getBasicSeq());
            }

            schService.updateBasicBrodScheduleState(vo.getBasicCode());

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(vo.getBasicCode());
            fileVO.setPageUnit(vo.getPageUnit());
            fileVO.setPageSize(vo.getPageUnit());
            fileVO.setPageIndex(vo.getPageIndex());

            Map<String, Object> resultMap = basicPagingInfo(fileVO);
            ResultHelper.setSuccess(resultVO, resultMap);

        } catch (Exception e) {
            log.error("basicUpdateRight Error: {}", e.getMessage());
            ResultHelper.setFailResult(resultVO, "basicUpdateRight", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 파일 목록 조회")
    @GetMapping("/file-list.do")
    public ResultVO basicFileInfo(@RequestParam("basicCode") String basicCode, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, basicFileService.selectBasicBrodFileLst(basicCode), Globals.JSON_RETURN_RESULT_LIST);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "basicFileInfo", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "일자별 기초 방송 파일 조회")
    @GetMapping("/file-list-by-day.do")
    public ResultVO brodBasicSearchDay(@RequestParam("basicCode") String basicCode,
                                        @RequestParam(value = "searchDay", required = false) String searchDay,
                                        HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            BasicBrodFileInfoVO vo = new BasicBrodFileInfoVO();
            vo.setBasicCode(basicCode);
            vo.setSearchDay(searchDay != null && !searchDay.isBlank() ? searchDay : EgovDateUtil.getCurrentDate(""));
            ResultHelper.setSuccess(resultVO, basicFileService.selectBasicBrodSchFileLst(vo), Globals.JSON_RETURN_RESULT_LIST);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "brodBasicSearchDay", e, egovMessageSource);
        }
        return resultVO;
    }

    private Map<String, Object> basicPagingInfo(ContentFileInfoVO fileVO) throws Exception {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(fileVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(fileVO.getPageUnit());
        paginationInfo.setPageSize(fileVO.getPageSize());
        fileVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        fileVO.setLastIndex(paginationInfo.getLastRecordIndex());
        fileVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        Map<String, Object> resultMap = new HashMap<>();

        int totCnt = conFileService.selectBasicFilePageListByPaginationTotCnt_S(fileVO);
        paginationInfo.setTotalRecordCount(totCnt);

        resultMap.put("paging", paginationInfo);
        resultMap.put("schList", conFileService.selectBasicFilePageListByPagination(fileVO));
        resultMap.put("atchFileLst", basicFileService.selectBasicBrodFileLst(fileVO.getBasicCode()));

        return resultMap;
    }

    @Operation(summary = "기초 방송 상세 조회", description = "기초 방송 정보 + 파일 목록을 함께 반환합니다.")
    @GetMapping("/detail.do")
    public ResultVO selectBasicDetail(@RequestParam("basicCode") String basicCode,
                                       @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                       @RequestParam(value = "pageUnit", required = false, defaultValue = "10") int pageUnit,
                                       HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(basicCode);
            fileVO.setPageUnit(pageUnit);
            fileVO.setPageSize(propertiesService.getInt("pageSize"));

            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setCurrentPageNo(pageIndex);
            paginationInfo.setRecordCountPerPage(fileVO.getPageUnit());
            paginationInfo.setPageSize(fileVO.getPageSize());
            fileVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
            fileVO.setLastIndex(paginationInfo.getLastRecordIndex());
            fileVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

            int totCnt = conFileService.selectBasicFilePageListByPaginationTotCnt_S(fileVO);
            paginationInfo.setTotalRecordCount(totCnt);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("regist", basicService.selectBasicBrod(basicCode));
            resultMap.put("paginationInfo", paginationInfo);
            resultMap.put("totalCnt", totCnt);
            resultMap.put(Globals.JSON_RETURN_RESULT_LIST, conFileService.selectBasicFilePageListByPagination(fileVO));
            resultMap.put("resultListBasic", basicFileService.selectBasicBrodFileLst(basicCode));
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectBasicDetail", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "기초 방송 시간대 그룹 목록 조회")
    @GetMapping("/timeDetail.do")
    public ResultVO selectBasicTimeDetail(@RequestParam("basicCode") String basicCode,
                                           @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                           @RequestParam(value = "pageUnit", required = false, defaultValue = "10") int pageUnit,
                                           HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            BasicFileGroupInfoVO groupInfo = new BasicFileGroupInfoVO();
            groupInfo.setBasicCode(basicCode.trim());
            groupInfo.setPageUnit(pageUnit);
            groupInfo.setPageSize(propertiesService.getInt("pageSize"));
            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setCurrentPageNo(pageIndex);
            paginationInfo.setRecordCountPerPage(groupInfo.getPageUnit());
            paginationInfo.setPageSize(groupInfo.getPageSize());
            groupInfo.setFirstIndex(paginationInfo.getFirstRecordIndex());
            groupInfo.setLastIndex(paginationInfo.getLastRecordIndex());
            groupInfo.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

            List<BasicFileGroupInfoVO> fileGroupInfo = groupService.selectBasicGroupInfoLst(groupInfo);
            int totCnt = fileGroupInfo.size() > 0 ? fileGroupInfo.get(0).getRnum() : 0;
            paginationInfo.setTotalRecordCount(totCnt);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("regist", basicService.selectBasicBrod(basicCode));
            resultMap.put("paginationInfo", paginationInfo);
            resultMap.put("totalCnt", totCnt);
            resultMap.put(Globals.JSON_RETURN_RESULT_LIST, fileGroupInfo);
            resultMap.put("groupTimegubun", cmmnDetailCodeManageService.selectCmmnDetailCombo("EMT023"));
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectBasicTimeDetail", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹 상세 조회")
    @GetMapping("/timeInfo.do")
    public ResultVO selectBasicFileInfo(@RequestParam("groupSeq") String groupSeq, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, groupService.selectBasicGroupInfoDetail(groupSeq), Globals.JSON_RETURN_RESULT);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectBasicFileInfo", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹 콤보 조회")
    @GetMapping("/groupCombo.do")
    public ResultVO brodGroupSelect(HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, groupService.selectBasicGroupInfoCombo(), Globals.JSON_RETURN_RESULT_LIST);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "brodGroupSelect", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹 일괄 삭제")
    @PostMapping("/timeDelete.do")
    public ResultVO deleteBasicTimeGroup(@RequestBody Map<String, String> params, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            String delBasicSeq = params.getOrDefault("delBasicSeq", "");
            int ret = 0;
            if (!delBasicSeq.isBlank()) {
                for (String seq : delBasicSeq.split(",")) {
                    if (!seq.isBlank()) {
                        ret = groupService.deleteBasicGroup(seq);
                    }
                }
            }
            ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteBasicTimeGroup", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹 등록/수정/복사", description = "동일 시간대 중복 및 일반 시간대(TIME_INPUT_1) 1회 제약을 검사한 뒤 등록/수정합니다.")
    @PostMapping("/timeUpdate.do")
    public ResultVO updateBasicTimeGroup(@RequestBody BasicFileGroupInfoVO vo, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            vo.setUserId(loginVO.getManagerId());

            BasicFileGroupInfoVO groupInfo = groupService.selectBasicGroupPreCheck(vo);
            if (!"0".equals(groupInfo.getTimeCnt())) {
                resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
                resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
                resultVO.setResultMessage("중복되는 시간이 있습니다.");
                return resultVO;
            }
            if (Integer.parseInt(groupInfo.getInutCnt()) > 1 && "TIME_INPUT_1".equals(vo.getGroupTimegubun())) {
                resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
                resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
                resultVO.setResultMessage("일반 시간대는 1번만 편성 가능 합니다.");
                return resultVO;
            }

            int ret = "Cpy".equals(vo.getMode()) ? groupService.updateBasicGroupInfoCopy(vo) : groupService.updateBasicGroupInfo(vo);
            if (ret > 0) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put(Globals.JSON_RETURN_RESULT_LIST, groupService.selectBasicGroupInfoLst(vo));
                ResultHelper.setSuccess(resultVO, resultMap);
            } else {
                throw new Exception("update failed");
            }
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "updateBasicTimeGroup", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹별 배치 파일 목록 조회(페이징)")
    @GetMapping("/timeFileList.do")
    public ResultVO detailSelectFileInfo(@RequestParam("groupSeq") String groupSeq,
                                          @RequestParam("basicCode") String basicCode,
                                          @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                          HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(basicCode);
            fileVO.setGroupSeq(groupSeq);
            fileVO.setPageSize(propertiesService.getInt("pageSize"));

            PaginationInfo paginationInfo = new PaginationInfo();
            paginationInfo.setCurrentPageNo(pageIndex);
            paginationInfo.setRecordCountPerPage(fileVO.getPageUnit());
            paginationInfo.setPageSize(fileVO.getPageSize());
            fileVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
            fileVO.setLastIndex(paginationInfo.getLastRecordIndex());
            fileVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

            List<ContentFileInfoVO> fileList = conFileService.selectBasicFileDetailPageListByPagination(fileVO);
            int totCnt = fileList.size() > 0 ? Integer.parseInt(fileList.get(0).getTotalRecodCount()) : 0;
            paginationInfo.setTotalRecordCount(totCnt);

            BasicBrodFileIntervalInfoVO fileIntervalInfo = new BasicBrodFileIntervalInfoVO();
            fileIntervalInfo.setBasicCode(basicCode);
            fileIntervalInfo.setGroupSeq(groupSeq);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("paginationInfo", paginationInfo);
            resultMap.put("totalCnt", totCnt);
            resultMap.put(Globals.JSON_RETURN_RESULT_LIST, fileList);
            resultMap.put("resultListBasic", fileInterval.selectBasicBrodIntervalFileLst(fileIntervalInfo));
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "detailSelectFileInfo", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹 배치 파일 등록/삭제")
    @PostMapping("/timeFileUpdate.do")
    public ResultVO basicUpdateRightItv(@RequestBody BasicBrodFileIntervalInfoVO vo, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            if ("R".equals(vo.getFileGubun())) {
                fileInterval.insertBasicBrodIntervalFile(vo);
            } else {
                fileInterval.deleteBasicBrodIntervalFile(vo);
            }

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(vo.getBasicCode());
            fileVO.setPageUnit(vo.getPageUnit());
            fileVO.setPageSize(vo.getPageUnit());
            fileVO.setPageIndex(vo.getPageIndex());
            fileVO.setGroupSeq(vo.getGroupSeq());

            ResultHelper.setSuccess(resultVO, basicPagingGroupInfo(fileVO));
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "basicUpdateRightItv", e, egovMessageSource);
        }
        return resultVO;
    }

    private Map<String, Object> basicPagingGroupInfo(ContentFileInfoVO fileVO) throws Exception {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(fileVO.getPageIndex());
        paginationInfo.setRecordCountPerPage(fileVO.getPageUnit());
        paginationInfo.setPageSize(fileVO.getPageSize());
        fileVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
        fileVO.setLastIndex(paginationInfo.getLastRecordIndex());
        fileVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

        List<ContentFileInfoVO> fileList = conFileService.selectBasicFileDetailPageListByPagination(fileVO);
        int totCnt = fileList.size() > 0 ? Integer.parseInt(fileList.get(0).getTotalRecodCount()) : 0;
        paginationInfo.setTotalRecordCount(totCnt);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("paging", paginationInfo);
        resultMap.put("schList", fileList);

        BasicBrodFileIntervalInfoVO fileIntervalInfo = new BasicBrodFileIntervalInfoVO();
        fileIntervalInfo.setBasicCode(fileVO.getBasicCode());
        fileIntervalInfo.setGroupSeq(fileVO.getGroupSeq());
        resultMap.put("atchFileLst", fileInterval.selectBasicBrodIntervalFileLst(fileIntervalInfo));

        return resultMap;
    }

    @Operation(summary = "기초 방송 배포 스케줄 재시작")
    @PostMapping("/scheduleRestart.do")
    public ResultVO basicScheduleRestart(@RequestBody BasicBrodScheduleInfoVO vo, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            int ret = schService.updateBasicBrodScheduleRestart(vo);
            ResultHelper.setCudResult(resultVO, ret, "success.common.update", egovMessageSource);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "basicScheduleRestart", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "시간대 그룹 배치 파일 이동/삭제(다건)", description = "moveGubun=L이면 파일을 그룹에 추가(이동), 그 외에는 제거합니다.")
    @PostMapping("/groupFileCheckUpdate.do")
    public ResultVO groupAllLeftUpdate(@RequestBody Map<String, String> params, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            String delBasicSeq = params.getOrDefault("delBasicSeq", "");
            String basicCode = params.getOrDefault("basicCode", "");
            String groupSeq = params.getOrDefault("groupSeq", "");
            String brodStartday = params.getOrDefault("brodStartday", "");
            String brodEndday = params.getOrDefault("brodEndday", "");
            String moveGubun = params.getOrDefault("moveGubun", "");
            String searchCondition = params.getOrDefault("searchCondition", "");
            String searchKeyword = params.getOrDefault("searchKeyword", "");

            BasicBrodFileIntervalInfoVO vo = new BasicBrodFileIntervalInfoVO();
            vo.setBasicCode(basicCode);
            vo.setGroupSeq(groupSeq);
            vo.setBrodStartday(brodStartday);
            vo.setBrodEndday(brodEndday);

            if (!delBasicSeq.isBlank()) {
                for (String seq : delBasicSeq.split(",")) {
                    if ("L".equals(moveGubun)) {
                        vo.setAtchFileId(seq);
                        vo.setBasicOrder("10");
                        fileInterval.insertBasicBrodIntervalFile(vo);
                    } else {
                        vo.setBrodFileseq(seq);
                        fileInterval.deleteBasicBrodIntervalFile(vo);
                    }
                }
            }

            ContentFileInfoVO fileVO = new ContentFileInfoVO();
            fileVO.setBasicCode(vo.getBasicCode());
            fileVO.setPageUnit(vo.getPageUnit());
            fileVO.setPageSize(vo.getPageUnit());
            fileVO.setPageIndex(vo.getPageIndex());
            fileVO.setGroupSeq(vo.getGroupSeq());
            fileVO.setSearchCondition(searchCondition);
            fileVO.setSearchKeyword(searchKeyword);

            ResultHelper.setSuccess(resultVO, basicPagingGroupInfo(fileVO));
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "groupAllLeftUpdate", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "방송 송출 현황(점포 무관) 리스트 조회")
    @PostMapping("/playInfo.do")
    public ResultVO selectBrodBasicPlayList(@RequestBody BasicFileGroupPlayInfoVO searchVO, HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            String searchStartDay = !searchVO.getSearchStartDay().isEmpty() ? searchVO.getSearchStartDay() : EgovDateUtil.getCurrentDate("");
            String searchEndDay = !searchVO.getSearchEndDay().isEmpty() ? searchVO.getSearchEndDay() : EgovDateUtil.getCurrentDate("");
            searchVO.setSearchStartDay(searchStartDay);
            searchVO.setSearchEndDay(searchEndDay);
            if (searchVO.getSearchCenterId() == null || searchVO.getSearchCenterId().isEmpty()) {
                searchVO.setSearchCenterId(null);
            }

            List<BasicFileGroupPlayInfoVO> playList = groupPlayInfo.selectPlayListInfoNotCenter(searchVO);
            for (BasicFileGroupPlayInfoVO vo : playList) {
                vo.setFileAlbumRegdate(vo.getFileAlbumRegdate() != null ? EgovDateUtil.formatDate(vo.getFileAlbumRegdate(), "-") : "정보없음");
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("centerList", centerInfoManageService.selectCenterBrodCombo(""));
            resultMap.put(Globals.JSON_RETURN_RESULT_LIST, playList);
            resultMap.put("totalCnt", playList.size());
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectBrodBasicPlayList", e, egovMessageSource);
        }
        return resultVO;
    }
}
