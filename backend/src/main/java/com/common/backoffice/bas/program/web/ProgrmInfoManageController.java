package com.common.backoffice.bas.program.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.PaginationHelper;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.util.ResultHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.common.backoffice.bas.program.modals.dto.ProgrmInfoDto;
import com.common.backoffice.bas.program.service.ProgrameInfoManageService;
import com.common.backoffice.bas.uni.service.UniUtilManageService;
import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.sym.log.annotation.NoLogging;

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/sys/prog")
@Tag(name="ProgrmInfoManageController",description = "권프로그램 API")
public class ProgrmInfoManageController {

    @Resource(name = "egovMessageSource")
    EgovMessageSource egovMessageSource;

    protected final EgovPropertyService propertiesService;
	private final ProgrameInfoManageService progrmService;
	private final UniUtilManageService uniMangeServiec;


	/**
	 * 프로그램 목록 조회
	 * @param searchVO
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "프로그램 목록 조회",
			description = "프로그램 목록 조회 상세 조회 합니다.",
			tags = {"ProgrmInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/programList.do")
	public ResultVO selectProgrmInfoListAjax(@RequestBody Map<String, Object> searchVO,
			HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginvo = AuthHelper.getLoginVO();
            searchVO.put(Globals.USER_ROLE_ID, UtilInfoService.NVLObj(loginvo.getRoleId(),""));
            searchVO.put(Globals.USER_PART_ID, UtilInfoService.NVLObj(loginvo.getPartId(), ""));



            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
                    propertiesService.getInt(Globals.PAGE_UNIT),
                    propertiesService.getInt(Globals.PAGE_SIZE));
            List<ProgrmInfoDto> list = progrmService .selectProgrmInfoList(searchVO);
            int totCnt = list.isEmpty() ? 0 : Integer.parseInt(list.get(0).getTotalRecordCount().toString());
            PaginationHelper.setResult(resultVO, list, paginationInfo, searchVO, totCnt);

		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "selectProgrmInfoListAjax", e, egovMessageSource);
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectProgrmInfoListAjax", e, egovMessageSource);
		}
		return resultVO;
		
		
	}
	
	/**
	 * 프로그램 저장
	 * @param progrmInfo
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "프로그램 정보 저장",
			description = "성공시 프로그램 정보 저장 합니다.",
			tags = {"ProgrmInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping ("updateProgrmInfo.do")
	public ResultVO updateProgrmInfo(@Valid @RequestBody ProgrmInfoDto progrmInfoDto,
										 HttpServletRequest request) throws Exception{
		ResultVO resultVO = new ResultVO();
		
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginvo = AuthHelper.getLoginVO();
            progrmInfoDto.setUserId(loginvo.getManagerId());

			int ret = progrmService.updateProgrmInfo(progrmInfoDto);
            String messageKey = (ret > 0)  ?  Globals.SAVE_MODE_INSERT.equals(progrmInfoDto.getMode())
                    ? "sucess.common.insert" : "sucess.common.update" :  Globals.SAVE_MODE_INSERT.equals(progrmInfoDto.getMode())
                    ? "fail.common.insert" : "fail.common.update";
            ResultHelper.setCudMsgResult(resultVO, ret, messageKey, egovMessageSource);

		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "updateProgrmInfo", e1, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "updateProgrmInfo", e, egovMessageSource);

			
		}
		return resultVO;
		
		
	}
	
	/**
	 * 프로그램 삭제
	 * @param progrmInfo
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "프로그램 정보 삭제",
			description = "성공시 프로그램 정보 삭제 합니다.",
			tags = {"ProgrmInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping ("/{progrmFileNm}.do")
	public ResultVO deleteProgrmInfoManage(@Parameter(description="프로그램 생성시 발급되는 progrmFileNm") @PathVariable("progrmFileNm") String progrmFileNm,
												HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			String status =  progrmService.deleteProgrmInfo(progrmFileNm) > 0 ?
			 		 Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = status.equals( Globals.STATUS_SUCCESS) ?
					 	 egovMessageSource.getMessage("success.request.msg") :
						 egovMessageSource.getMessage("fail.request.msg") ;
			
			int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
			
			resultVO.setResultCode(res);
			resultVO.setResultMessage(message);
			resultVO.setResultCodeInfo(status);
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "deleteProgrmInfoManage", e1, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteProgrmInfoManage", e, egovMessageSource);
		}
		return resultVO;
		
	}
	
	/**
	 * 프로그램 중복 체크
	 * @param progrmFileNm
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "프로그램 코드 중복 체크",
			description = "성공시 프로그램 코드 중복 체크 합니다.",
			tags = {"ProgrmInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@NoLogging
	@GetMapping ("/programIDCheck/{progrmFileNm}.do")
	public ResultVO programIDCheck(@Parameter(description="프로그램 생성시 발급되는 progrmFileNm") @PathVariable("progrmFileNm") String progrmFileNm,
									  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            int ret = progrmService.isPresentPrograme(progrmFileNm);
            ResultHelper.setCudResult(resultVO, ret, "common.codeOk.msg", "common.codeFail.msg", egovMessageSource);
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "programIDCheck", e1, egovMessageSource);

		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "programIDCheck", e, egovMessageSource);
		}
		return resultVO;
	
	}
	/**
	 * 프로그램 삭제
	 * @param progrmInfo
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "프로그램 상세 정보",
			description = "성공시 프로그램 상세 정보 표시.",
			tags = {"ProgrmInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping ("/{progrmFileNm}.do")
	public ResultVO selectProgrmInfoManage(@Parameter(description="프로그램 생성시 발급되는 progrmFileNm") @PathVariable("progrmFileNm") String progrmFileNm,
												HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			Optional<ProgrmInfoDto> info = progrmService.selectProgrmInfoDetail(progrmFileNm) ;
			String status = info.isPresent() ?
			 		 Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = status.equals( Globals.STATUS_SUCCESS) ?
					 	 egovMessageSource.getMessage("success.request.msg") :
						 egovMessageSource.getMessage("fail.request.msg") ;
			
			int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(Globals.JSON_RETURN_RESULT, info);
			
			resultVO.setResultCode(res);
			resultVO.setResultMessage(message);
			resultVO.setResultCodeInfo(status);
			resultVO.setResult(resultMap);
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "deleteProgrmInfoManage", e1, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteProgrmInfoManage", e, egovMessageSource);
		}
		return resultVO;
		
	}
}
