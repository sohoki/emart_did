package com.common.backoffice.sts.brd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.sts.brd.modals.BrodAnniversary;
import com.common.backoffice.sts.brd.modals.BrodAnniversaryVO;
import com.common.backoffice.sts.brd.service.BrodAnniversaryManagerService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 방송 기념일 관리 API.
 * did_emart(REST + JWT + tb_brodanniversary, postgresql) 기준으로 리팩토링함.
 *
 * 원본 대비 변경된 부분:
 * - 기존 컨트롤러 파일은 이미 REST 형태(ResultVO/AuthHelper 등)로 뼈대가 작성되어 있었으나,
 *   실제 서비스(BrodAnniversaryManagerService)/매퍼와 메서드명·파라미터 타입이 전혀 맞지
 *   않는 상태였음(예: 존재하지 않는 selectBrodAnniversaryList/selectBrodAnniversaryListCnt를
 *   호출, Optional 미지원 selectBrodAnniversary를 Optional로 취급 등) — 실제 서비스 계약에
 *   맞춰 다시 연결함
 * - `anniverBrodList.do`/`anniverBrodUpdate.do`(방송에 기념일을 체크박스로 연결/해제하는 화면)는
 *   제외함 — TB_BRODANNIVERSARY는 BROD_CODE가 각 기념일 행에 직접 달려있는 구조(N:1)라
 *   "연결/해제" 개념 자체가 매퍼에 없고, 가장 가까운 기능은 다른 방송으로 복사하는
 *   insertBrodAnniverCopy(다건 복사, 단일 ID 채번 방식과 안 맞음)뿐이라 잘못 짐작해서 만들면
 *   실제 UX와 다르게 동작할 위험이 있어 이번 범위에서 제외함(후속 작업 필요)
 * - 신규 등록 시 BROD_ANNSEQ는 DB 시퀀스 conanniversary_seq로 채번함(매퍼 &lt;selectKey&gt;가
 *   INSERT 전에 NEXTVAL 값을 미리 가져와 vo에 채워 넣음). 이 시퀀스는 db-encoding-fix/
 *   08_create_missing_sequences.sql로 신규 생성함(마이그레이션 과정에서 누락됨)
 * - 별도 Request DTO 대신 세션의 다른 컨트롤러들과 동일하게 BrodAnniversary VO를 직접 바인딩함
 * - `BrodAnniversaryVO.java`의 `egovframework.let.sts.brd.service.BrodAnniversary`(존재하지
 *   않는 잘못된 자기 참조 import)를 제거함(같은 패키지라 import 자체가 불필요했음)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "BrodAnniversaryManagerController", description = "방송 기념일 관리")
@RequestMapping("/api/backoffice/sub/brodManage")
public class BrodAnniversaryManagerController {

    @Resource(name = "egovMessageSource")
    protected EgovMessageSource egovMessageSource;

    @Resource(name = "propertiesService")
    protected EgovPropertyService propertiesService;

    private final BrodAnniversaryManagerService anniverInfo;

    @Operation(summary = "방송 기념일 리스트 조회", description = "brodCode(방송코드) 기준으로 기념일 목록을 조회합니다. brodDay(YYYYMMDD) 지정 시 해당 일자에 걸치는 기념일만 반환합니다.")
    @PostMapping("/anniverList.do")
    public ResultVO selectAnniverList(@RequestBody BrodAnniversaryVO searchVO, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            List<BrodAnniversaryVO> resultList = anniverInfo.selectBrodAnniverLst(searchVO);
            int totCnt = anniverInfo.selectBrodAnniverPageCnt(searchVO.getBrodCode());

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(Globals.JSON_RETURN_RESULT_LIST, resultList);
            resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
            ResultHelper.setSuccess(resultVO, resultMap);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectAnniverList", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "방송 기념일 상세 조회")
    @PostMapping("/anniverDetail.do")
    public ResultVO selectAnniverDetail(@RequestBody Map<String, String> body, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            BrodAnniversaryVO detail = anniverInfo.selectBrodAnniver(body.get("brodAnnSeq"));
            if (detail != null) {
                ResultHelper.setSuccess(resultVO, detail, Globals.JSON_RETURN_RESULT);
            } else {
                resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
                resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
                resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.select"));
            }
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectAnniverDetail", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "방송 기념일 등록/수정", description = "mode=Ins면 신규 등록, 그 외에는 수정합니다.")
    @PostMapping("/anniverUpdate.do")
    public ResultVO updateAnniver(@RequestBody BrodAnniversary vo, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();

            boolean isInsert = Globals.SAVE_MODE_INSERT.equals(vo.getMode());
            int ret;
            if (isInsert) {
                vo.setFrstRegisterId(loginVO.getManagerId());
                ret = anniverInfo.insertBrodAnniver(vo);
            } else {
                vo.setLastUpdusrId(loginVO.getManagerId());
                ret = anniverInfo.updateBrodAnniver(vo);
            }

            String successMsgKey = isInsert ? "success.common.insert" : "success.common.update";
            ResultHelper.setCudResult(resultVO, ret, successMsgKey, egovMessageSource);
            if (ret > 0) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put(Globals.STATUS_REGINFO, vo);
                resultVO.setResult(resultMap);
            }
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "updateAnniver", e, egovMessageSource);
        }
        return resultVO;
    }

    @Operation(summary = "방송 기념일 삭제")
    @DeleteMapping("/anniver/{brodAnnSeq}.do")
    public ResultVO deleteAnniver(@Parameter(description = "기념일 순번") @PathVariable String brodAnnSeq, HttpServletRequest request) {
        ResultVO resultVO = new ResultVO();
        try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            int ret = anniverInfo.deleteBrodAnniver(brodAnnSeq);
            ResultHelper.setCudResult(resultVO, ret, "success.common.delete", egovMessageSource);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteAnniver", e, egovMessageSource);
        }
        return resultVO;
    }
}
