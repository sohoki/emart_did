package com.common.backoffice.bas.code.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.util.ResultHelper;
import jakarta.servlet.http.HttpServletRequest;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.backoffice.bas.code.modals.CmmnDetailCode;
import com.common.backoffice.bas.code.modals.dto.CmmnDetailCodeDto;
import com.common.backoffice.bas.code.service.EgovCcmCmmnDetailCodeManageService;
import com.common.backoffice.bas.uni.service.UniUtilManageService;

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Tag(name="EgovCcmCmmnDetailCodeManageController",description = "공통상세코드 정보 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/sys/cmm/cde")
public class EgovCcmCmmnDetailCodeManageController {

	
	@Value("${Globals.addedOptions.pageUnit}")
	private int pageUnitSetting ;
	
	@Value("${Globals.addedOptions.pageSize}")
	private int pageSizeSetting ;
	
	/** JwtVerification */

	private final EgovCcmCmmnDetailCodeManageService cmmnDetailCodeManageService;
	protected final EgovMessageSource egovMessageSource;
	protected final EgovPropertyService propertiesService;
	

	@Operation(
			summary = "공통 상세 코드 삭제",
			description = "성공시 공통 상세 코드를 삭제 합니다.",
			tags = {"EgovCcmCmmnDetailCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("/{code}.do")
	public ResultVO deleteCmmnDetailCode (@Parameter(description="공통상세코드 CODE") @PathVariable String code, 
											ModelMap modelMe,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try{
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = cmmnDetailCodeManageService.deleteCmmnDetailCode(code);
			if (ret > 0 ) {
				/*
				MessageDto dto =  MessageDto.builder()
						.id(code)
						.processGubun("DEL")
						.processName("DETAILCODEINFO")
						.urlMethod("DELETE")
						.url("")
						.build();
				
						messageService.sendMessage(dto, 
								"Topic", 
								exchangeName,
								routingKey);
						log.info("=========== send message");
				*/
				resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
				resultVO.setResultMessage(egovMessageSource.getMessage("success.common.delete"));
			}
			else {
				
				
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.delete"));
			}
			
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "deleteCmmnDetailCode", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "deleteCmmnDetailCode", e, egovMessageSource);

		}
		return resultVO;
	}

	/**
	* 공통상세코드 상세항목을 조회한다.
	* @param loginVO
	 * @param cmmnDetailCode
	 * @param model
	 * @return "cmm/sym/ccm/EgovCcmCmmnDetailCodeDetail"
	 * @throws Exception
	 */
	@Operation(
			summary = "공통 상세 코드 조회",
			description = "성공시 공통 상세 코드를 조회 합니다.",
			tags = {"EgovCcmCmmnDetailCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/{code}.do")
 	public ResultVO selectCmmnDetailCodeDetail (@Parameter(description="공통상세코드 CODE") @PathVariable("code") String code,
 												HttpServletRequest request)	throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, cmmnDetailCodeManageService.selectCmmnDetailCodeDetail(code), Globals.JSON_RETURN_RESULT);
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectCmmnDetailCodeDetail", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "selectCmmnDetailCodeDetail", e, egovMessageSource);
		}
		return resultVO;
	}
	
	@Operation(
			summary = "공통 상세 코드 combo list",
			description = "성공시 공통 상세 코드를 combo list 합니다.",
			tags = {"EgovCcmCmmnDetailCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/combo/{codeId}.do")
 	public ResultVO selectCmmnDetailComboList (@Parameter(description="공통상세코드 CODE") @PathVariable("codeId") String codeId,
 												HttpServletRequest request)	throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, cmmnDetailCodeManageService.selectCmmnDetailComboLamp(codeId), Globals.JSON_RETURN_RESULT);
        }catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectCmmnDetailComboList", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "selectCmmnDetailComboList", e, egovMessageSource);
		}
		return resultVO;
	}
	
	
	@Operation(
			summary = "공통 상세 코드 combo list",
			description = "성공시 공통 상세 코드를 combo list 합니다.",
			tags = {"EgovCcmCmmnDetailCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/comboEtc/{codeId}.do")
 	public ResultVO selectCmmnComboEtcList (@Parameter(description="공통상세코드 CODE") @PathVariable("codeId") String codeId,
 												HttpServletRequest request)	throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, cmmnDetailCodeManageService.selectCmmnDetailComboEtc(codeId), Globals.JSON_RETURN_RESULT);
        }catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectCmmnComboEtcList", e1, egovMessageSource);
			
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "selectCmmnComboEtcList", e, egovMessageSource);

		}
		return resultVO;
	}
	/**
	 * 공통상세코드 목록을 조회한다.
	 * @param loginVO
	 * @param searchVO
	 * @param model 
	 * @throws Exception
	 */
	@Operation(
			summary = "공통 상세 코드 조회",
			description = "성공시 공통 상세 코드 조회 합니다.",
			tags = {"EgovCcmCmmnDetailCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping(value="detailList.do")
	public ResultVO selectCmmnDetailCodeList ( @RequestBody Map<String, Object> searchMap, 
												HttpServletRequest request)  {
		//나중에 권한 설정 값 넣기 
		
		
		ResultVO resultVO = new ResultVO();
		try{
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			List<CmmnDetailCodeDto> codeDetailList = cmmnDetailCodeManageService.selectCmmnDetailCodeList(searchMap.get("codeId").toString());
			int totCnt = codeDetailList.size() > 0 ? codeDetailList.size()  : 0;
		
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(Globals.STATUS_REGINFO, searchMap.get("codeId"));
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, codeDetailList);
			resultMap.put(Globals.PAGE_TOTAL_COUNT, totCnt);
			
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResult(resultMap);
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectCmmnDetailCodeList", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "selectCmmnDetailCodeList", e, egovMessageSource);
		}
		return resultVO;
	}
	@Operation(
			summary = "공통 상세 코드 업데이트",
			description = "성공시 공통 상세 코드 업데이트 합니다.",
			tags = {"EgovCcmCmmnDetailCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping(value="/CodeDetailUpdate.do")
	public ResultVO updateCmmnDetailCode ( @RequestBody CmmnDetailCode vo
											, HttpServletRequest request
											, BindingResult bindingResult ) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try{

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            vo.setUserId(loginVO.getManagerId());
            int ret = cmmnDetailCodeManageService.updateCmmnDetailCode(vo);
            log.info("============== ret:" + ret);

            String status = ret > 0 ?
                    Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = status.equals( Globals.STATUS_SUCCESS) ?
                    egovMessageSource.getMessage("success.request.msg") :
                    egovMessageSource.getMessage("fail.request.msg") ;
            int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

            if (status.equals(Globals.STATUS_SUCCESS)) {
				log.info(message);
				/*
				MessageDto dto =  MessageDto.builder()
						.id(vo.getCode())
						.processGubun(vo.getMode())
						.processName("DETAILCODEINFO")
						.urlMethod("GET")
						.url("/api/backoffice/sys/cmm/cde/"+ vo.getCode() + ".do")
						.build();
				
						messageService.sendMessage(dto, 
								"Topic", 
								exchangeName,
								routingKey);
						log.info("=========== send message");
				*/
			}
            ResultHelper.setCudResult(resultVO, res, status, message);
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "updateCmmnDetailCode", e1, egovMessageSource);
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "updateCmmnDetailCode", e, egovMessageSource);

		}
		return resultVO;
	}
    @Operation(
            summary = "공통 상세 코드 사용유무 업데이트",
            description = "성공시 공통 상세 코드 사용유무 업데이트 합니다.",
            tags = {"EgovCcmCmmnDetailCodeManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping(value="/CodeDetailUpdateYn.do")
    public ResultVO CodeDetailUpdateYn ( @RequestBody CmmnDetailCode vo
            , HttpServletRequest request) throws Exception {

        ResultVO resultVO = new ResultVO();
        try{

            // 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            LoginVO  loginVO = AuthHelper.getLoginVO();
            vo.setUserId(loginVO.getManagerId());

            String status = cmmnDetailCodeManageService.updateUseAtOnly(vo)> 0 ?
                    Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = status.equals( Globals.STATUS_SUCCESS) ?
                    egovMessageSource.getMessage("success.request.msg") :
                    egovMessageSource.getMessage("fail.request.msg") ;
            int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
            if (status.equals(Globals.STATUS_SUCCESS)) {
                log.info(message);
				/*
				MessageDto dto =  MessageDto.builder()
						.id(vo.getCode())
						.processGubun(vo.getMode())
						.processName("DETAILCODEINFO")
						.urlMethod("GET")
						.url("/api/backoffice/sys/cmm/cde/"+ vo.getCode() + ".do")
						.build();

						messageService.sendMessage(dto,
								"Topic",
								exchangeName,
								routingKey);
						log.info("=========== send message");
				*/
            }

            ResultHelper.setCudResult(resultVO, res, status, message);
        }catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "CodeDetailUpdateYn", e1, egovMessageSource);

        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "CodeDetailUpdateYn", e, egovMessageSource);
        }
        return resultVO;
    }
}
