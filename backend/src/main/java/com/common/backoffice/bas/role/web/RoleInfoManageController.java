package com.common.backoffice.bas.role.web;

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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.backoffice.bas.role.modals.RoleInfo;
import com.common.backoffice.bas.role.modals.dto.RoleInfoRequestDto;
import com.common.backoffice.bas.role.service.RoleInfoManageService;
import com.common.backoffice.bas.uni.models.UniUtilInfo;
import com.common.backoffice.bas.uni.service.UniUtilManageService;
import com.common.backoffice.bas.uni.service.UtilInfoService;
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
@RequestMapping("/api/backoffice/uat/role/")
@Tag(name="RoleInfoManageController",description = "권한 관리 코드")
public class RoleInfoManageController {

	/** EgovPropertyService */
	@Resource(name = "propertiesService")
	protected EgovPropertyService propertyService;

    /** EgovMessageSource */
    @Resource(name = "egovMessageSource")
    EgovMessageSource egovMessageSource;

	private final RoleInfoManageService roleMangeServiec;

	
	@Operation(
			summary = "권한 정보 상세 조회",
			description = "성공시 권한 정보 상세 조회 합니다.",
			tags = {"RoleInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/{roleId}.do")
	public ResultVO selectServerDetailInfo(@Parameter(description="ROLE 코드") @PathVariable("roleId") String roleId,
											   HttpServletRequest request)throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			Optional<RoleInfo> info = roleMangeServiec.selectRoleInfoDetail(roleId);
			
			info.orElseThrow(() -> new IllegalArgumentException("해당하는 서버 정보가가 없습니다. 잘못된 입력"));
            ResultHelper.setSuccess(resultVO, info, Globals.JSON_RETURN_RESULT);

			
		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "selectServerDetailInfo", e, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "selectServerDetailInfo", e, egovMessageSource);
		}
		return resultVO;
	}
	
	@Operation(
			summary = "권한 정보 COMBOBOX",
			description = "성공시 권한 정보 COMBOBOX 조회 합니다.",
			tags = {"RoleInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/roleCombo.do")
	public ResultVO selectRoleComboInfo(@RequestParam Map<String, Object> model,
										HttpServletRequest request)throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, roleMangeServiec.selectRoleInfoComboList(model), Globals.JSON_RETURN_RESULT);

		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "selectRoleComboInfo", e, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "selectRoleComboInfo", e, egovMessageSource);
		}
		return resultVO;
	}
	
	
	@Operation(
			summary = "권한 정보 중복 체크",
			description = "성공시 권한 정보 중복 체크 합니다.",
			tags = {"RoleInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/idCheck/{roleId}.do")
	public ResultVO selectServerIdCkeckInfo(@Parameter(description="ROLE 코드") @PathVariable("roleId") String roleId,
			  									HttpServletRequest request)throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			boolean ret = roleMangeServiec.existsByRoleId(roleId);

            String status = ret == false ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = ret == false ? "common.codeOk.msg" : "common.codeFail.msg";
            int res = ret == false ? ResponseCode.SUCCESS.getCode(): ResponseCode.SERVER_ERROR.getCode();

            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(this.egovMessageSource.getMessage(message));
			
		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "selectServerIdCkeckInfo", e, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "selectServerIdCkeckInfo", e, egovMessageSource);

		}
		return resultVO;
	}
	@Operation(
			summary = "권한 정보  삭제",
			description = "성공시 권한 정보 삭제 합니다.",
			tags = {"RoleInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("/{roleId}.do")
	public ResultVO deleteServerDetailInfo(@Parameter(description="ROLE 코드") @PathVariable("roleId") String roleId,
			   								HttpServletRequest request)throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = roleMangeServiec.deleteRoleInfo(roleId);
			
			
			if (ret > 0) {
				/*
				MessageDto dto =  MessageDto.builder()
									.id(roleId)
									.processGubun("DELETE")
									.processName("ROLEINFO")
									.urlMethod("DELETE")
									.url("")
									.build();
							
				messageService.sendMessage(dto, 
						"Topic", 
						exchangeName,
						routingKey);
				log.info("=========== send message");
				*/
			}

			String status = (ret > 0) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = (ret > 0) ? egovMessageSource.getMessage("success.common.delete") : egovMessageSource.getMessage("fail.request.msg");
			int res = ret > 0 ? ResponseCode.SUCCESS.getCode(): ResponseCode.SERVER_ERROR.getCode();
			
			resultVO.setResultCode(res);
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(message);
			
			
		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "deleteServerDetailInfo", e, egovMessageSource);

		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteServerDetailInfo", e, egovMessageSource);
		}
		return resultVO;
	}
	@Operation(
			summary = "권한 정보 업데이트 조회",
			description = "성공시 권한 정보 업데이트 합니다.",
			tags = {"RoleInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/roleUpdate.do")
	public ResultVO updateRoleInfo(@Valid @RequestBody RoleInfoRequestDto info,
									HttpServletRequest request)throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			
			
	    	// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();

            info.setUserId(loginVO.getManagerId());

			if (info.getMode().equals(Globals.SAVE_MODE_INSERT)) {
                boolean ret = roleMangeServiec.existsByRoleId(info.getRoleId());
				
				if (ret == true) {
					resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
					resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
					resultVO.setResultMessage(egovMessageSource.getMessage("common.isExist.msg"));
					return resultVO;
				}
			}
			
			int ret = roleMangeServiec.updateRoleInfo(info);
			if (ret > 0) {
				/*
				MessageDto dto =  MessageDto.builder()
									.id(info.getRoleId())
									.processGubun(info.getMode())
									.processName("ROLEINFO")
									.urlMethod("GET")
									.url("/api/backoffice/uat/role/"+info.getRoleId()+".do")
									.build();
							
				messageService.sendMessage(dto, 
						"Topic", 
						exchangeName,
						routingKey);
				log.info("=========== send message");
				*/
			}
			String eGovmessage = info.getMode().equals(Globals.SAVE_MODE_INSERT) ? "sucess.common.insert" : "sucess.common.update";
			String status = (ret > 0) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = (ret > 0) ? egovMessageSource.getMessage(eGovmessage) : egovMessageSource.getMessage("fail.request.msg");
			int res = ret < 1 ? ResponseCode.SERVER_ERROR.getCode(): ResponseCode.SUCCESS.getCode();
			
			
			resultVO.setResultCode(res);
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(message);
			
		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "updateRoleInfo", e, egovMessageSource);

		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "updateRoleInfo", e, egovMessageSource);
		}
		return resultVO;
	}
    @Operation(
            summary = "권한 정보 업데이트 사용 변경 ",
            description = "성공시 권한 정보 사용 유무 업데이트 합니다.",
            tags = {"RoleInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/roleUseynUpdate.do")
    public ResultVO roleUseynUpdate(@Valid @RequestBody RoleInfoRequestDto info,
                                    HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
        try {

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            info.setUserId(loginVO.getManagerId());

            int ret = roleMangeServiec.updateRoleUseynInfo(info);

            String message = info.getMode().equals(Globals.SAVE_MODE_INSERT) ? "sucess.common.insert" : "sucess.common.update";
            ResultHelper.setCudMsgResult(resultVO, ret, message, egovMessageSource);
        } catch(Exception e) {
            // [리팩토링] Exception 한 줄 처리
            ResultHelper.setFailResult(resultVO, "updateRoleInfo", e, egovMessageSource);
        }
        return resultVO;
    }

	@Operation(
			summary = "권한 정보 리스트",
			description = "성공시 권한 관리 정보 리스트를 조회 합니다.",
			tags = {"RoleInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/roleList.do")
	public ResultVO  selectRoleInfoPageList(@RequestBody Map<String, Object> searchMap
												, HttpServletRequest request
												, BindingResult bindingResult) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();


            searchMap.put(Globals.USER_ROLE_ID, loginVO.getRoleId());
            searchMap.put(Globals.USER_PART_ID, loginVO.getPartId());

            int pageSize = UtilInfoService.NVLObj(searchMap.get(Globals.PAGE_SIZE), propertyService.getInt(Globals.PAGE_SIZE));
            int pageUnit = UtilInfoService.NVLObj(searchMap.get(Globals.PAGE_UNIT), propertyService.getInt(Globals.PAGE_UNIT));


            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchMap,pageUnit, pageSize);
            List<Map<String, Object>> roleList = roleMangeServiec.selectRoleInfoPageList(searchMap);
            PaginationHelper.setResult(resultVO, roleList, paginationInfo, searchMap);
			
		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "selectRoleInfoPageList", e, egovMessageSource);

			
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "selectRoleInfoPageList", e, egovMessageSource);
		}
		return resultVO;
	}
}
