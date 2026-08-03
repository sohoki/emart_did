package com.common.backoffice.bas.code.web;

import java.util.List;
import java.util.Map;

import com.common.backoffice.util.service.PaginationHelper;
import egovframework.com.cmm.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.backoffice.bas.code.modals.dto.CmmnCodeDto;
import com.common.backoffice.bas.code.modals.dto.CmmnCodeReqDto;
import com.common.backoffice.bas.code.service.EgovCcmCmmnCodeManageService;
import com.common.backoffice.bas.uni.models.UniUtilInfo;
import com.common.backoffice.bas.uni.service.UniUtilManageService;
import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.sym.log.annotation.NoLogging;
import com.common.backoffice.util.service.AuthHelper;

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/sys/cmm/cca")
@Tag(name="EgovCcmCmmnCodeManageController",description = "공통 코드")
public class EgovCcmCmmnCodeManageController {

		
	@Value("${Globals.addedOptions.pageUnit}")
	private int pageUnitSetting ;
	
	@Value("${Globals.addedOptions.pageSize}")
	private int pageSizeSetting ;
		
	

	/** JwtVerification */
	private final EgovCcmCmmnCodeManageService cmmnCodeManageService;
	protected final EgovPropertyService propertiesService;
	protected final EgovMessageSource egovMessageSource;
	/**
	 * 공통코드를 삭제한다.
	 * @param codeId
	 * @return "forward:/sym/ccm/cca/EgovCcmCmmnCodeList.do"
	 * @throws Exception
	 */
	@Operation(
			summary = "공통코드를 삭제",
			description = "성공시 공통코드를 삭제 합니다.",
			tags = {"EgovCcmCmmnCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("/{codeId}.do")
	public ResultVO deleteCmmnCodeMessage (@Parameter(description="공통코드 codeId") @PathVariable("codeId") String codeId
									, HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try {
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = cmmnCodeManageService.deleteCmmnCode(codeId);
			
			if (ret > 0 ){
                ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);
			}else {
				throw new Exception();    		
			}
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "deleteCmmnCodeMessage", e1, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteCmmnCodeMessage", e, egovMessageSource);
		}
		return resultVO;
		//상세 코드 삭제 	
	}

	@Operation(
			summary = "공통코드를 조회",
			description = "성공시 공통코드를 조회 합니다.",
			tags = {"EgovCcmCmmnCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/{codeId}.do")
	public ResultVO selectCodeCmmnCode (@Parameter(description="공통코드 CODE ID") @PathVariable("codeId") String codeId
											, HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try {
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, cmmnCodeManageService.selectCmmnCodeDetail(codeId), Globals.JSON_RETURN_RESULT);
		
		}catch(NullPointerException e1) {
			ResultHelper.setFailResult(resultVO, "selectCodeCmmnCode", e1, egovMessageSource);
			
		}catch(Exception e) {
			ResultHelper.setFailResult(resultVO, "selectCodeCmmnCode", e, egovMessageSource);
			
		}
		return resultVO;
		//상세 코드 삭제 	
	}
	@Operation(
			summary = "공통코드 리스트",
			description = "성공시 공통코드 조회 합니다.",
			tags = {"EgovCcmCmmnCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/code/codeList.do")
	public ResultVO selectCmmnCodeList (@RequestBody Map<String, Object> searchMap
										, HttpServletRequest request
										, BindingResult bindingResult
										) throws Exception {
		ResultVO resultVO = new ResultVO();
		try
			{
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            searchMap.put(Globals.USER_ROLE_ID, loginVO.getRoleId());
            searchMap.put(Globals.USER_PART_ID, loginVO.getPartId());


            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchMap,
                    propertiesService.getInt(Globals.PAGE_UNIT),
                    propertiesService.getInt(Globals.PAGE_SIZE));
            List<CmmnCodeDto> codeList = cmmnCodeManageService.selectCmmnCodeListByPagination(searchMap);
            int totCnt = codeList.isEmpty() ? 0 : Integer.parseInt(codeList.get(0).getTotalRecordCount().toString());
            PaginationHelper.setResult(resultVO, codeList, paginationInfo, searchMap, totCnt);
		}catch(NullPointerException e1) {
			ResultHelper.setFailResult(resultVO, "selectCmmnCodeList", e1, egovMessageSource);
			
		}catch (Exception e){
			ResultHelper.setFailResult(resultVO, "selectCmmnCodeList", e, egovMessageSource);
		}

		return resultVO;

	}
	
	@Operation(
			summary = "공통코드 중복체크",
			description = "성공시 공통코드 중복체크 합니다.",
			tags = {"EgovCcmCmmnCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	//ID 체크 
	@NoLogging
	@GetMapping("/codeIDCheck/{codeId}.do")
	public ResultVO selectIdCheck(@PathVariable("codeId") String codeId ,
								HttpServletRequest request)throws Exception{
		
		ResultVO resultVO = new ResultVO();
		try{
            // 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            boolean result = cmmnCodeManageService.existsByCodeId(codeId);

            String status = (result == false) ? Globals.STATUS_SUCCESS: Globals.STATUS_FAIL;
            String meesage = (result == false)  ? "common.codeOk.msg" : "common.codeFail.msg";
            int ret = (result == false) ?  ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

            ResultHelper.setCudResult(resultVO, ret, status, egovMessageSource, meesage);
        }catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectIdCheck", e1, egovMessageSource);

        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectIdCheck", e, egovMessageSource);
        }
        return resultVO;
	}
	
	
	@SuppressWarnings("finally")	
	@Operation(
			summary = "공통코드 업데이트",
			description = "성공시 공통코드 업데이트 합니다.",
			tags = {"EgovCcmCmmnCodeManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/code/codeUpdate.do")
	public ResultVO  updateCmmCode (@Valid @RequestBody CmmnCodeReqDto vo
									, HttpServletRequest request
									, BindingResult bindingResult) throws Exception {
			
		ResultVO resultVO = new ResultVO();
		try{
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            vo.setUserId(loginVO.getManagerId());

			String status = cmmnCodeManageService.updateCmmnCode(vo) > 0 ?
			 		 Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = status.equals( Globals.STATUS_SUCCESS) ?
					 	 egovMessageSource.getMessage("success.request.msg") :
						 egovMessageSource.getMessage("fail.request.msg") ;
			
			int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
			
			
			if (status.equals(Globals.STATUS_SUCCESS) ) {
				/*
				MessageDto dto =  MessageDto.builder()
						.id(vo.getCodeId())
						.processGubun(vo.getMode())
						.processName("CODEINFO")
						.urlMethod("GET")
						.url("/api/backoffice/sys/cmm/cca/"+ vo.getCodeId() + ".do?systemCode="+vo.getSystemCode())
						.build();
				
						messageService.sendMessage(dto, 
								"topic", 
								exchangeName,
								routingKey);
						log.info("=========== send message");
				*/
			}
			resultVO.setResultCode(res);
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(message);
		}catch(NullPointerException e1) {
			ResultHelper.setFailResult(resultVO, "updateCmmCode", e1, egovMessageSource);
		}catch (Exception e){
			ResultHelper.setFailResult(resultVO, "updateCmmCode", e, egovMessageSource);
		}
		finally{
			return resultVO;
		}
	}
    @Operation(
            summary = "공통코드 업데이트",
            description = "성공시 공통코드 업데이트 합니다.",
            tags = {"EgovCcmCmmnCodeManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/code/codeUseYnUpdate.do")
    public ResultVO  codeUseYnUpdate (@Valid @RequestBody CmmnCodeReqDto vo
            , HttpServletRequest request
            , BindingResult bindingResult) throws Exception {

        ResultVO resultVO = new ResultVO();
        try{
            // 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

            LoginVO  loginVO = AuthHelper.getLoginVO();
            vo.setUserId(loginVO.getManagerId());


            String status = cmmnCodeManageService.updateCmmnUseYnCode(vo) > 0 ?
                    Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = status.equals( Globals.STATUS_SUCCESS) ?
                    egovMessageSource.getMessage("success.request.msg") :
                    egovMessageSource.getMessage("fail.request.msg") ;

            int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();


            if (status.equals(Globals.STATUS_SUCCESS) ) {
				/*
				MessageDto dto =  MessageDto.builder()
						.id(vo.getCodeId())
						.processGubun(vo.getMode())
						.processName("CODEINFO")
						.urlMethod("GET")
						.url("/api/backoffice/sys/cmm/cca/"+ vo.getCodeId() + ".do?systemCode="+vo.getSystemCode())
						.build();

						messageService.sendMessage(dto,
								"topic",
								exchangeName,
								routingKey);
						log.info("=========== send message");
				*/
            }
            ResultHelper.setCudResult(resultVO, res, status, message);
        }catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "codeUseYnUpdate", e1, egovMessageSource);

        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "codeUseYnUpdate", e, egovMessageSource);
        }
        return resultVO;

    }
}
