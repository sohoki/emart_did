package com.common.backoffice.use.web;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.backoffice.use.modals.Group;
import com.common.backoffice.use.modals.GroupVo;
import com.common.backoffice.use.service.GroupManagerService;
import com.common.backoffice.util.service.AuthHelper;
import egovframework.com.cmm.LoginVO;


import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springmodules.validation.commons.DefaultBeanValidator;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/backoffice/sub/basicManage")
@Tag(name="GroupManagerController",description = "Group 관련 API")
public class GroupManagerController {

    private final GroupManagerService groupManagerService;
	
	@Resource(name="egovMessageSource")
	protected EgovMessageSource egovMessageSource;
	
	
    /** EgovPropertyService */
    @Resource(name = "propertiesService")
    protected EgovPropertyService propertiesService;

	
	
	//@RequestMapping( value = "/backoffice/sub/basicManage/selectGroupLst.do")
    @Operation(
            summary = "Group List 정보 리스트",
            description = "성공시 Group List 정보 리스트를 조회 합니다.",
            tags = {"HolidayInfoController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/selectGroupLst.do")
	public ResultVO selectUserGroupManageListByPagination(@ModelAttribute("loginVO") LoginVO loginVO,
                                                          @ModelAttribute("searchVO") GroupVo searchVO,
                                                          HttpServletRequest request
                                                       )throws Exception {
        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("resultList", groupManagerService.selectUserGroupManageListByPagination(searchVO));
            map.put("totalCnt", groupManagerService.selectGroupManageListTotCnt_S(searchVO));

            ResultHelper.setSuccess(resultVO, map);
            /*
            model.addAttribute("resultList", groupManagerService.selectUserGroupManageListByPagination(searchVO));
            int totalCnt = groupManagerService.selectGroupManageListTotCnt_S(searchVO);
            model.addAttribute("totalCnt", totalCnt);
            */
        }catch(Exception e){
            ResultHelper.setFailResult(resultVO, "selectHolyInfoListAjax", e, egovMessageSource);
        }
        return resultVO;
		//return "/backoffice/sub/basicManage/group_list";
	}
	
	
	
	@SuppressWarnings("finally")	
	//@RequestMapping(value="/backoffice/sub/basicManage/deleteGroup.do")
    @Operation(
            summary = "Group 정보 삭제 리스트",
            description = "성공시 Group 정보 삭제 합니다.",
            tags = {"HolidayInfoController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @DeleteMapping("/deleteGroup/{groupId}.do")
	public ResultVO deleteGroupManage(@Parameter(description="ROLE 코드") @PathVariable("groupId") String groupId,
                                    HttpServletRequest request
                                     )throws Exception{

        ResultVO resultVO = new ResultVO();


		try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			
            int ret = 	groupManagerService.deleteGroupManage(groupId);
            String status = (ret > 0) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = (ret > 0) ? egovMessageSource.getMessage("success.common.delete") : egovMessageSource.getMessage("fail.request.msg");
            int res = ret > 0 ? ResponseCode.SUCCESS.getCode(): ResponseCode.SERVER_ERROR.getCode();

            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "deleteGroupManage", e, egovMessageSource);
		}
        return resultVO;
        /*
		finally{
			return "forward:/backoffice/sub/basicManage/selectGroupLst.do";				
		}
		*/
	}		

	//@RequestMapping(value = "/backoffice/sub/basicManage/updateGroup.do")
    @Operation(
            summary = "Group 정보 저장 리스트",
            description = "성공시 Group 정보 저장 합니다.",
            tags = {"HolidayInfoController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/updateGroup.do")
	public ResultVO insertGroupManage(@RequestBody Group vo,
                                    HttpServletRequest request
			                                             )throws Exception{

        ResultVO resultVO = new ResultVO();
		try{
			int ret  = vo.getMode().equals(Globals.SAVE_MODE_INSERT) ? groupManagerService.insertGroupManage(vo) :
                    groupManagerService.updateGroupManage(vo) ;
            String eGovmessage = vo.getMode().equals(Globals.SAVE_MODE_INSERT) ? "sucess.common.insert" : "sucess.common.update";
            String status = (ret > 0) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = (ret > 0) ? egovMessageSource.getMessage(eGovmessage) : egovMessageSource.getMessage("fail.request.msg");
            int res = ret < 1 ? ResponseCode.SERVER_ERROR.getCode(): ResponseCode.SUCCESS.getCode();


            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);

		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "insertGroupManage", e, egovMessageSource);
		}
        return  resultVO;
        /*
        finally {
			return "/backoffice/sub/basicManage/groupDetail";
		}
		*/
	}

    //@RequestMapping (value="/backoffice/sub/basicManage/groupDetail.do")
    @Operation(
            summary = "Group 정보 저장 리스트",
            description = "성공시 Group 정보 저장 합니다.",
            tags = {"HolidayInfoController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @GetMapping("/groupDetail/{groupId}.do")
	public ResultVO getGroupManage(HttpServletRequest request,
                                   @Parameter(description="groupId 코드") @PathVariable("groupId") String groupId
			                                             )throws Exception{
        ResultVO resultVO = new ResultVO();
		try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO =  AuthHelper.getLoginVO();
            GroupVo groupVo = new GroupVo();
            groupVo.setParentGroupId(loginVO.getPartId());
            groupVo.setGroupId(groupId);

            Map<String,Object> map = new HashMap<String,Object>();
            map.put("selectGroup", groupManagerService.selectGroupManageCombo(groupVo));
            map.put("regist", groupManagerService.selectGroupManageDetail(groupId));
            ResultHelper.setSuccess(resultVO, map);

        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "getGroupManage", e, egovMessageSource);
        }
        return resultVO;

		/*
		LoginVO user = (LoginVO) request.getSession().getAttribute("LoginVO");			    
		if (user != null ){
			groupVo.setParentGroupId(user.getParentGroupId());	
			groupVo.setGroupId(user.getGroupId());
		} else {
			groupVo.setParentGroupId("EMART_00000000000001");
			groupVo.setGroupId("EMART_00000000000002");
		}
		
		model.addAttribute("selectGroup", groupManagerService.selectGroupManageCombo(groupVo));
		model.addAttribute("regist", vo);
		if (!vo.getMode().equals("Ins")){			
		    model.addAttribute("regist",  groupManagerService.selectGroupManageDetail(vo.getGroupId())  );
		}				
		return "/backoffice/sub/basicManage/groupDetail";
		*/
	}
	
	
}
