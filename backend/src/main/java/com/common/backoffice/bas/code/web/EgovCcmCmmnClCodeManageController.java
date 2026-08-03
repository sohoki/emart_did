package com.common.backoffice.bas.code.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.common.backoffice.bas.code.modals.CmmnClCode;
import com.common.backoffice.bas.code.modals.dto.CmmnClCodeReqDto;
import com.common.backoffice.bas.code.service.EgovCcmCmmnClCodeManageService;
import com.common.backoffice.sym.log.annotation.NoLogging;
import com.common.backoffice.bas.uni.models.UniUtilInfo;
import com.common.backoffice.bas.uni.service.UniUtilManageService;
import com.common.backoffice.bas.uni.service.UtilInfoService;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.jwt.JwtVerification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import egovframework.com.cmm.exception.NotFoundException;


@Slf4j
@RestController
@RequestMapping("/api/backoffice/sys/cmm/clc")
@Tag(name="EgovCcmCmmnClCodeManageController",description = "공통 분류 코드 ")
public class EgovCcmCmmnClCodeManageController {

	
	@Autowired
	private EgovCcmCmmnClCodeManageService cmmnClCodeManageService;
	
	@Value("${Globals.addedOptions.pageUnit}")
	private int pageUnitSetting ;

	@Value("${Globals.addedOptions.pageSize}")
	private int pageSizeSetting ;
	
	@Autowired
	EgovMessageSource egovMessageSource;
	
	/** JwtVerification */
	@Autowired
	private JwtVerification jwtVerification;
	
	@Autowired
	private UniUtilManageService utilService;


	@Operation(
			summary = "삭제",
			description = "성공시 대 분류 코드를 삭제 합니다.",
			tags = {"EgovCcmCmmnClCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("/{clCode}.do")
	public ResultVO deleteCmmnClCode (@Parameter(description="공통코드 CL_CODE") @PathVariable String clCode, 
										  HttpServletRequest request) throws Exception {
	 
		ResultVO resultVO = new ResultVO();
		try {
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
			if (!jwtVerification.isVerification(request)) {
				return jwtVerification.handleAuthError(resultVO); // 토큰 확인
			}
		
			int ret = cmmnClCodeManageService.deleteCmmnClCode(clCode);

			if (ret > 0) {
				/*
				MessageDto dto =  MessageDto.builder()
						.id(clCode)
						.processGubun("DELETE")
						.processName("LCODEINFO")
						.urlMethod("DELETE")
						.url("")
						.build();

						messageService.sendMessage(dto,
								"Topic",
								exchangeName,
								routingKey);
						log.info("=========== send message");
				*/
				String message = egovMessageSource.getMessage("success.common.delete");
				
				
				resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
				resultVO.setResultMessage(message);
				
			}else {
				
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage("삭제할 내용이 없습니다.");
				
			}
		}catch(NullPointerException e1) {
			
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg") + e1.toString());
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
		}catch(Exception e) {
			log.debug("deleteCmmnClCode error:" + e.toString());
			
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.delete"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
		}
		return resultVO;
	}
	
	//ID 체크 
	@Operation(
			summary = "대분류 중복체크",
			description = "성공시 대분류 중복체크 합니다.",
			tags = {"EgovCcmCmmnClCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@NoLogging
	@GetMapping("/codeIDCheck/{codeId}.do")
	public ResultVO selectIdCheck(@Parameter(description="중복코드") String codeId ,
									 HttpServletRequest request)throws Exception{
		
		ResultVO resultVO = new ResultVO();
		
		// 기존 세션 체크 인증에서 토큰 방식으로 변경
		if (!jwtVerification.isVerification(request)) {
			return jwtVerification.handleAuthError(resultVO); // 토큰 확인
		}
		
		
		
		UniUtilInfo util = new UniUtilInfo();
		util.setInCondition("CODE_ID = [" + codeId + "[");
		util.setInTable("COMTCCMMNCODE");
		util.setInCheckName("CODE_ID");
	
		int ret = utilService.selectIdDoubleCheck(util);
		if (ret == 0) {
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResultMessage( egovMessageSource.getMessage("common.codeOk.msg"));
			
		}
		else {
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("common.codeFail.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
		}
		return resultVO;
	}
	/**
	 * 공통분류코드를 등록한다.
	 * @param loginVO
	 * @param cmmnClCode
	 * @param bindingResult
	 * @return "/cmm/sym/ccm/EgovCcmCmmnClCodeRegist"
	 * @throws Exception
	 */
	@Operation(
			summary = "대분류 업데이트",
			description = "성공시 공통분류코드 코드를 업데이트 합니다.",
			tags = {"EgovCcmCmmnClCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/update.do")
	public ResultVO updateCmmnClCode(@Valid  @RequestBody CmmnClCodeReqDto clCode, 
			  							 HttpServletRequest request) throws Exception {
		
		
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
			if (!jwtVerification.isVerification(request)) {
				return jwtVerification.handleAuthError(resultVO); // 토큰 확인
			}else {
				clCode.setUserId(jwtVerification.getTokenUserName(request));
			}
			String status = cmmnClCodeManageService.updateCmmnClCode(clCode) > 0 ?
			 		 Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = status.equals( Globals.STATUS_SUCCESS) ?
					 	 egovMessageSource.getMessage("success.request.msg") :
						 egovMessageSource.getMessage("fail.request.msg") ;
			
			int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
			if (status.equals(Globals.STATUS_SUCCESS)) {
				/*
				MessageDto dto =  MessageDto.builder()
									.id(clCode.getClCode())
									.processGubun(clCode.getMode())
									.processName("LCODEINFO")
									.urlMethod("GET")
									.url("/api/backoffice/sys/cmm/clc/"+clCode.getClCode()+".do")
									.build();
							
				messageService.sendMessage(dto, 
						"Topic", 
						exchangeName,
						routingKey);
				log.info("=========== send message");
				*/
			}
			
			resultVO.setResultCode(res);
			resultVO.setResultMessage(message);
			resultVO.setResultCodeInfo(status);
			
			
		}catch(NullPointerException e1) {
			log.error("updateCmmnClCode error:" + e1.toString());
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
		}catch(Exception e) {
			log.error("updateCmmnClCode error:" + e.toString());
			
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
		}
		return resultVO;
	}

	/**
	 * 공통분류코드 상세항목을 조회한다.
	 * @param loginVO
	 * @param cmmnClCode
	 * @param model
	 * @return "cmm/sym/ccm/EgovCcmCmmnClCodeDetail"
	 * @throws Exception
	 */
	@Operation(
			summary = "상세 조회",
			description = "공통분류코드 상세항목을 조회한다.",
			tags = {"EgovCcmCmmnClCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/{clCode}.do")
	public ResultVO selectCmmnClCodeDetail(@PathVariable String clCode, 
			  									HttpServletRequest request) throws Exception {
		
		//공용 확인 하기 
		ResultVO resultVO = new ResultVO();
		try {
			if (!jwtVerification.isVerification(request) ) {
				return jwtVerification.handleAuthError(resultVO); // 토큰 확인
			}
			CmmnClCode detail = cmmnClCodeManageService.selectCmmnClCodeDetail(clCode).orElseThrow(() 
					-> new NotFoundException(egovMessageSource.getMessage("fial.common.info")));
				
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(Globals.JSON_RETURN_RESULT, detail);
			resultVO.setResult(resultMap);
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			
			
		}catch(NullPointerException e1) {
			log.error("selectCmmnClCodeDetail error:" + e1.toString());
			
			
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
			
		}catch (Exception e){
			
			log.error("selectCmmnClCodeDetail line:" + e.getStackTrace()[0].getLineNumber());
						
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
		}
		return resultVO;
	// 기존 세션 체크 인증에서 토큰 방식으로 변경
	}

	/**
	 * 공통분류코드 목록을 조회한다.
     * @param loginVO
     * @param searchVO
     * @param model
     * @return "/cmm/sym/ccm/EgovCcmCmmnClCodeList"
     * @throws Exception
     */
	@Operation(
			summary = "대분류 목록 조회",
			description = "대분류 분류코드 목록을 조회한다.",
			tags = {"EgovCcmCmmnClCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/list.do")
	public ResultVO selectCmmnClCodeList(@RequestBody Map<String, Object> searchMap, 
											HttpServletRequest request) throws Exception {
	
		ResultVO resultVO = new ResultVO();
		try
		{
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
			if (!jwtVerification.isVerification(request)) {
				
				return jwtVerification.handleAuthError(resultVO); // 토큰 확인
			}else {
				String[] userinfo = jwtVerification.getTokenUserInfo(request);
				searchMap.put(Globals.USER_ROLE_ID, userinfo[2]);
				searchMap.put(Globals.USER_PART_ID, userinfo[3]);
			}
			int pageUnit = 0;
			if (!searchMap.get(Globals.USER_PART_ID).equals("SYSTEM"))
				pageUnit = UtilInfoService.NVLObj(searchMap.get(Globals.PAGE_UNIT), pageUnitSetting);
			else 
				pageUnit =  1000;
			
			int pageSize = UtilInfoService.NVLObj(searchMap.get(Globals.PAGE_SIZE), pageSizeSetting);
			/** pageing */
			PaginationInfo paginationInfo = new PaginationInfo();
			paginationInfo.setCurrentPageNo( UtilInfoService.NVLObj(searchMap.get(Globals.PAGE_INDEX), 1));
			paginationInfo.setRecordCountPerPage(pageUnit);
			paginationInfo.setPageSize(pageSize);
	
			searchMap.put(Globals.PAGE_FIRST_INDEX, paginationInfo.getFirstRecordIndex());
			searchMap.put(Globals.PAGE_LAST_INDEX, paginationInfo.getLastRecordIndex());
			searchMap.put(Globals.PAGE_RECORD_PER_PAGE, paginationInfo.getRecordCountPerPage());
			List<Map<String, Object>> codeList = (List<Map<String, Object>>) cmmnClCodeManageService.selectCmmnClCodeListByPagination(searchMap);
			int totCnt = codeList.size() > 0 ?  Integer.valueOf( codeList.get(0).get(Globals.PAGE_TOTAL_RECORD_COUNT).toString().replace("-", "") ) :0;
			
			paginationInfo.setTotalRecordCount(totCnt);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, codeList);
			resultMap.put(Globals.JSON_PAGEINFO, paginationInfo);
			
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResult(resultMap);
			
		}catch(NullPointerException e1) {
			
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			
		}catch (Exception e){
			log.debug("selectCmmnClCodeList number:" + e.getStackTrace()[0].getLineNumber());
			log.debug("selectCmmnClCodeList error:" + e.toString());
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultMessage(this.egovMessageSource.getMessage("fail.common.msg"));
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
		}

		return resultVO;
	}

}
