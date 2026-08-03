package com.common.backoffice.sym.grp.web;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.sym.grp.modals.GroupDidInfo;
import com.common.backoffice.sym.grp.modals.GroupDidInfoVO;
import com.common.backoffice.sym.grp.modals.GroupInfo;
import com.common.backoffice.sym.grp.modals.GroupInfoVO;
import com.common.backoffice.sym.grp.service.GroupDidInfoManageService;
import com.common.backoffice.sym.grp.service.GroupInfoManageService;
import com.common.backoffice.sym.log.annotation.NoLogging;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.PaginationHelper;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
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
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.*;



@Tag(name="GroupInfoManageController",description = "Group 및 DID Group 매핑 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/backoffice/sub/equiManage/group")
public class GroupInfoManageController {


    @Value("${Globals.addedOptions.pageUnit}")
    private int pageUnitSetting ;

    @Value("${Globals.addedOptions.pageSize}")
    private int pageSizeSetting ;

    private final GroupInfoManageService didgroupInfoManageService;
    private final GroupDidInfoManageService groupDidInfoManageService;
	
	@Resource(name="egovMessageSource")
	protected EgovMessageSource egovMessageSource;
	
    /** EgovPropertyService */
    @Resource(name = "propertiesService")
    protected EgovPropertyService propertiesService;





    //@RequestMapping ("/backoffice/sub/equiManage/did_groupList.do")
    @Operation(
            summary = "did group list",
            description = "성공시 did group list 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/didGroupList.do")
	public ResultVO selectGroupDidInfoManageListByPagination(@RequestBody Map<String, Object> searchVO,
                                                            HttpServletRequest request
															) throws Exception {
        ResultVO resultVO = new ResultVO();
		try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            searchVO.put("roldId", loginVO.getRoleId());
            searchVO.put("mberId", loginVO.getManagerId());

            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
                    propertiesService.getInt(Globals.PAGE_UNIT),
                    propertiesService.getInt(Globals.PAGE_SIZE));
            List<GroupInfoVO> groupList = didgroupInfoManageService.selectGroupInfoManageListByPagination(searchVO);
            int totCnt = groupList.isEmpty() ? 0 : groupList.get(0).getTotalRecordCount();
            PaginationHelper.setResult(resultVO, groupList, paginationInfo, searchVO, totCnt);


        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectGroupDidInfoManageListByPagination", e, egovMessageSource);
        }
        return resultVO;
		/*
		
	      if(  searchVO.getPageUnit() > 0  ){    	   
    	     searchVO.setPageUnit(searchVO.getPageUnit());
		  }else {
			   searchVO.setPageUnit(propertiesService.getInt("pageUnit"));   
		  }	      
	      searchVO.setPageSize(propertiesService.getInt("pageSize"));
	       

	   	  PaginationInfo paginationInfo = new PaginationInfo();
		  paginationInfo.setCurrentPageNo(searchVO.getPageIndex());
		  paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
		  paginationInfo.setPageSize(searchVO.getPageSize());

		  searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		  searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
		  searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

	       model.addAttribute("resultList",   didgroupInfoManageService.selectGroupInfoManageListByPagination(searchVO) );

	       int totCnt = didgroupInfoManageService.selectGroupInfoManageListTotCnt_S(searchVO) ;       
		   paginationInfo.setTotalRecordCount(totCnt);
	       model.addAttribute("paginationInfo", paginationInfo);
	       model.addAttribute("totalCnt", totCnt);
	       
	       model.addAttribute("regist", searchVO);
	       
	       
	      return "/backoffice/sub/equiManage/did_groupList";
        */
	}
	// groupCode 체크

    //@RequestMapping("/backoffice/sub/equiManage/IdCheck.do")
    @Operation(
            summary = "Group 코드 중복체크",
            description = "성공시 Group 코드 중복체크 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    //ID 체크
    @NoLogging
    @GetMapping("/IdCheck/{groupCode}.do")
	public ResultVO selectIdCheck(@PathVariable("groupCode") String groupCode ,
                                HttpServletRequest request){

        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            int idCheck = didgroupInfoManageService.selectGroupIDInfoManageListTotCnt_S(groupCode);
            String status = (idCheck < 1) ? Globals.STATUS_SUCCESS: Globals.STATUS_FAIL;
            String meesage = (idCheck < 1)  ? "common.codeOk.msg" : "common.codeFail.msg";
            int ret = (idCheck < 1) ?  ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
            ResultHelper.setCudResult(resultVO, ret, status, egovMessageSource, meesage);
        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectIdCheck", e, egovMessageSource);
        }
        return resultVO;

	}
	//ajax 로 값 보내기 
	//@RequestMapping ("/backoffice/sub/equiManage/groupDetail.do")
    @Operation(
            summary = "공통코드 중복체크",
            description = "성공시 공통코드 중복체크 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @GetMapping("/detail/{groupCode}.do")
	public ResultVO selectGroupDetail(@PathVariable("groupCode") String groupCode ,
                                    HttpServletRequest request) throws  Exception{
        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            GroupInfo groupInfo  =	didgroupInfoManageService.selectGroupInfoManageDetail(groupCode);
            String grInfo =   groupInfo.getGroupNm()+"/"+groupInfo.getGroupUseYn();
            ResultHelper.setSuccess(resultVO, grInfo, Globals.JSON_RETURN_RESULT);
        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectIdCheck", e, egovMessageSource);
        }
        return resultVO;

	}
	
	//@RequestMapping("/backoffice/sub/equiManage/DidgroupLst.do")
    @Operation(
            summary = "DID GROUP 리스트",
            description = "성공시 DID GROUP 리스트 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @GetMapping("/didgroupLst/{groupCode}.do")
	public ResultVO selectDidLst(@PathVariable("groupCode") String groupCode ,
                                    HttpServletRequest request) throws Exception{

        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            List<GroupDidInfoVO> groupDidTb = groupDidInfoManageService.selectGroupInfoManageListByPagination(groupCode);
            ResultHelper.setSuccess(resultVO, groupDidTb, "didLst");
            /*
            ModelAndView model = new 	ModelAndView("jsonView");
            String groupCode = request.getParameter("groupCode") != null ? request.getParameter("groupCode") : "";
            List<GroupDidInfoVO> groupDidTb = groupDidInfoManageService.selectGroupInfoManageListByPagination(groupCode);
            model.addObject("didLst", groupDidTb);


            List<GroupDidInfo> groupCmbLst = groupDidInfoManageService.selectComboLst();
            model.addObject("didCmbLst", groupCmbLst);

            return model;
             */
        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectDidLst", e, egovMessageSource);
        }
        return resultVO;

	}	
	
	
	//@RequestMapping("/backoffice/sub/equiManage/DidgroupDel.do")
    @Operation(
            summary = "DID GROUP 리스트 삭제",
            description = "성공시 DID GROUP 리스트  삭제 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @DeleteMapping("/didgroupDel/{groupCode}.do")
	public ResultVO selectDidDelLst(@Parameter(description="Group Code") @PathVariable("groupCode") String groupCode ,
                                    @Parameter(description="DID ID") @RequestParam("didId") String didId ,
                                        HttpServletRequest request) throws Exception{

        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            int ret = groupDidInfoManageService.deleteGroupInfoManage(didId);

            if (ret > 0 ){
                List<GroupDidInfoVO> groupDidTb = groupDidInfoManageService.selectGroupInfoManageListByPagination(groupCode);
                ResultHelper.setSuccess(resultVO, groupDidTb, "didLst");

            }else {
                throw new Exception();
            }
            /*
            ModelAndView model = new 	ModelAndView("jsonView");
            String groupCode = request.getParameter("groupCode") != null ? request.getParameter("groupCode") : "";
            String didId = request.getParameter("didId") != null ? request.getParameter("didId") : "";

            int rnt = groupDidInfoManageService.deleteGroupInfoManage(didId);

            List<GroupDidInfoVO> groupDidTb = groupDidInfoManageService.selectGroupInfoManageListByPagination(groupCode);
            model.addObject("didLst", groupDidTb);
            return model;
             */
        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectDidDelLst", e, egovMessageSource);
        }
        return resultVO;

	}
	
	//select Combo 리스트 보여주기 
	//@RequestMapping("/backoffice/sub/equiManage/DidcomboLst.do")
    @Operation(
            summary = "DID GROUP 리스트 삭제",
            description = "성공시 DID GROUP 리스트  삭제 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @GetMapping("/didcomboLst.do")
	public ResultVO selectDidComboLst(HttpServletRequest request) throws Exception{
        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            ResultHelper.setSuccess(resultVO, groupDidInfoManageService.selectComboLst(), "didCmbLst");
            /*
            ModelAndView model = new 	ModelAndView("jsonView");
            List<GroupDidInfo> groupCmbLst = groupDidInfoManageService.selectComboLst();
            model.addObject("didCmbLst", groupCmbLst);
            return model;
             */
        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectDidDelLst", e, egovMessageSource);
        }
        return resultVO;


	}	
	//@RequestMapping ("/backoffice/sub/equiManage/DidgroupInsret.do")
    @Operation(
            summary = "DID GROUP 업데이트",
            description = "성공시 DID GROUP 리스트  업데이트 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/didgroupInsret.do")
	public ResultVO insertGroupDid(@RequestBody GroupDidInfo vo,
                                 HttpServletRequest request)throws Exception{

        ResultVO resultVO = new ResultVO();
		try{
            String status = groupDidInfoManageService.insertGroupInfoManage(vo) > 0 ?
                    Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = status.equals( Globals.STATUS_SUCCESS) ?
                    egovMessageSource.getMessage("success.request.msg") :
                    egovMessageSource.getMessage("fail.request.msg") ;

            int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);

		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "insertGroupDid", e, egovMessageSource);
		}
        return resultVO;
		//return "forward:/backoffice/sub/equiManage/did_groupList.do";
		
	}
	
	//@RequestMapping("/backoffice/sub/equiManage/didgroupDelete.do")
    @Operation(
            summary = "Group Code를 삭제",
            description = "성공시 Group Code 를 삭제 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @DeleteMapping("/del/{groupCode}.do")
	public ResultVO deleteGroupInfoManage(@Parameter(description="groupCode") @PathVariable("groupCode") String groupCode
                                    ,HttpServletRequest request
                                        )throws Exception{
		

		/*
		if (StringUtils.equals(vo.getGroupCode(), Globals.REGITER_SYSTEM)){
			model.addAttribute("status", Globals.STATUS_FAIL);
			//model.addAttribute("message", egovMessageSource.getMessage("fail.common.delete.system"));
			return "forward:/backoffice/sub/equiManage/didList.do";
		}
		*/
        ResultVO resultVO = new ResultVO();
		try{
			
		    int ret = didgroupInfoManageService.deleteGroupInfoManage(groupCode);

            if (ret > 0 ){
                ResultHelper.setCudResult(resultVO, ret, "success.common.delete", "fail.common.delete", egovMessageSource);
            }else {
                throw new Exception();
            }
            /*
		      if (ret > 0 ) {		    	  
		    	  model.addAttribute("status", Globals.STATUS_SUCCESS);
		    	  model.addAttribute("message", egovMessageSource.getMessage("success.common.delete") );		    	  
		      }else {		    	  
		    	  throw new Exception();		    	  
		      }
		    */
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "deleteGroupInfoManage", e, egovMessageSource);
		}				
		return resultVO; //"forward:/backoffice/sub/equiManage/did_groupList.do";
	}
	
	//@RequestMapping(value="/backoffice/sub/equiManage/didgroupUpdate.do")
    @Operation(
            summary = "Group Code를 삭제",
            description = "성공시 Group Code 를 삭제 합니다.",
            tags = {"GroupInfoManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/didgroupUpdate.do")
	public ResultVO updateGroupManager(HttpServletRequest request,
									@RequestBody  GroupInfoVO vo) throws Exception {

        /*
		model.addAttribute("regist", vo);
		String meesage = "";
		String url = "redirect:/backoffice/sub/equiManage/did_groupList.do";
		
		loginVO = (LoginVO)request.getSession().getAttribute("LoginVO");
		if(loginVO != null){
			vo.setAuthorCode(loginVO.getAuthorCode());
			vo.setGroupId(loginVO.getGroupId());
			vo.setCenterId(loginVO.getCenterId());
		} else {
        	model.addAttribute("message", egovMessageSource.getMessage("fail.common.login"));
        	return "backoffice/login";
		}
		*/
        ResultVO resultVO = new ResultVO();
		try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();

            vo.setAuthorCode(loginVO.getRoleId());
            vo.setGroupId(loginVO.getPartId());
            vo.setCenterId(loginVO.getCenterId());

			int ret  = vo.getMode().equals(Globals.SAVE_MODE_INSERT) ?
                    didgroupInfoManageService.insertGroupInfoManage(vo) :
                    didgroupInfoManageService.updateGroupInfoManage(vo);

            String status = ret > 0 ?
                    Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
            String message = status.equals( Globals.STATUS_SUCCESS) ?
                    egovMessageSource.getMessage("success.request.msg") :
                    egovMessageSource.getMessage("fail.request.msg") ;

            int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
            resultVO.setResultCode(res);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(message);
			
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "updateGroupManager", e, egovMessageSource);
		}
		return resultVO;
	}
	
	
	
	//@RequestMapping(value="/backoffice/sub/equiManage/groupSearch.do")
    @Operation(
            summary = "공통코드 리스트",
            description = "성공시 공통코드 조회 합니다.",
            tags = {"EgovCcmCmmnCodeManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/groupSearch.do")
	public ResultVO selectDidGroupSearchList(@RequestBody Map<String, Object> searchMap,
                                             HttpServletRequest request) throws Exception{

        ResultVO resultVO = new ResultVO();
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            GroupInfoVO groupInfoVO = new GroupInfoVO();
            groupInfoVO.setAuthorCode(loginVO.getRoleId());
            groupInfoVO.setMberId(loginVO.getManagerId());
            String grpSearchKeyword = UtilInfoService.NVLObj(searchMap.get("grpSearchKeyword"),"");
            groupInfoVO.setSearchKeyword(grpSearchKeyword);
            ResultHelper.setSuccess(resultVO, didgroupInfoManageService.selectGroupInfoManageCombo(groupInfoVO), "resultMap");
            /*
            ModelAndView model = new ModelAndView("jsonView");

            String grpSearchKeyword = request.getParameter("grpSearchKeyword") == null ? "" : request.getParameter("grpSearchKeyword");

            LoginVO loginVO = (LoginVO)request.getSession().getAttribute("LoginVO");
            if(loginVO != null){
                groupInfoVO.setAuthorCode(loginVO.getAuthorCode());
                groupInfoVO.setMberId(loginVO.getMberId());
            } else {

            }
            groupInfoVO.setSearchKeyword(grpSearchKeyword);
            return model.addObject("resultMap", didgroupInfoManageService.selectGroupInfoManageCombo(groupInfoVO));
            */

        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectDidGroupSearchList", e, egovMessageSource);
        }
        return resultVO;
		
	}
	
	
	
	
	// 2019-02-11 create
	//@RequestMapping ("/backoffice/sub/equiManage/groupListInfo.do")
    @Operation(
            summary = "공통코드 리스트",
            description = "성공시 공통코드 조회 합니다.",
            tags = {"EgovCcmCmmnCodeManageController"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "실패")
    })
    @PostMapping("/groupListInfo.do")
	public ResultVO selectGroupListInfo(@RequestBody Map<String, Object> searchVO,
                                            HttpServletRequest request
                                            ) throws Exception {
        ResultVO resultVO = new ResultVO();
		try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            searchVO.put("roldId", loginVO.getRoleId());
            searchVO.put("mberId", loginVO.getManagerId());

            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
                    propertiesService.getInt(Globals.PAGE_UNIT),
                    propertiesService.getInt(Globals.PAGE_SIZE));


            List<GroupInfoVO> codeList = didgroupInfoManageService.selectGroupInfoManageListByPagination(searchVO);
            int totCnt = codeList.isEmpty() ? 0 : codeList.get(0).getTotalRecordCount();
            PaginationHelper.setResult(resultVO, codeList, paginationInfo, searchVO, totCnt);
        } catch (Exception e) {
            ResultHelper.setFailResult(resultVO, "selectGroupListInfo", e, egovMessageSource);
        }
        return resultVO;

		
		/** 권환관리 필요 데이터 set
		LoginVO loginVO = (LoginVO)request.getSession().getAttribute("LoginVO");
		if(loginVO != null){
			searchVO.setAuthorCode(loginVO.getAuthorCode());
			searchVO.setMberId(loginVO.getMberId());
		} else {
/*        	model.addAttribute("message", egovMessageSource.getMessage("fail.common.login"));
        	return "backoffice/login";
		}  

		String firstIndex = request.getParameter("firstIndex") == null ? "0" : request.getParameter("firstIndex");
		String lastIndex = request.getParameter("lastIndex") == null ? "0" : request.getParameter("lastIndex");
		String recordCountPerPage = request.getParameter("recordCountPerPage") == null ? "10" : request.getParameter("recordCountPerPage");
		
		String searchCondition = request.getParameter("searchCondition") == null ? "" : request.getParameter("searchCondition");
		String searchKeyword = request.getParameter("searchKeyword") == null ? "" : request.getParameter("searchKeyword");
		
		
		searchVO.setFirstIndex(Integer.parseInt(firstIndex));
		searchVO.setLastIndex(Integer.parseInt(lastIndex));
		searchVO.setRecordCountPerPage(Integer.parseInt(recordCountPerPage));
		
		searchVO.setSearchCondition(searchCondition);
		searchVO.setSearchKeyword(searchKeyword);
		
		model.addObject("resultList", didgroupInfoManageService.selectGroupInfoManageListByPagination(searchVO));
		
		return model;
         */
	}
	
	
	
	@RequestMapping(value="/backoffice/sub/equiManage/modifyGroupInfo.do")
	@ResponseBody
	public String modifyGroupInfo( HttpServletRequest request) throws Exception{
		
		String result = "";
		
		GroupInfo groupInfo = new GroupInfo();
        
		
		String work = request.getParameter("work") == null ? "" : request.getParameter("work");
		String groupNm = request.getParameter("new_group_nm") == null ? "" : request.getParameter("new_group_nm");
        String groupCode = request.getParameter("new_group_code") == null ? "" : request.getParameter("new_group_code");
        try{
        	if(work != null && !work.equals("")){
        		int ret  = 0;
        		if(groupNm != null && !groupNm.equals("")){
        			groupInfo.setGroupNm(groupNm);
        			groupInfo.setGroupCode(groupCode);
        			if (work.equals("INSERT")){
        				groupCode = didgroupInfoManageService.selectLastInsertGroup();
        				groupInfo.setGroupCode(groupCode);
        				ret = didgroupInfoManageService.insertGroupInfoManage(groupInfo)  ;		
        			}else if(work.equals("UPDATE")) {
        				// 1. UPDATE의 경우에는  groupCode와  groupNm 둘 다 필수
        				 // ret = didgroupInfoManageService.updateGroupInfoManage(groupInfo);
        			}			
        			if (ret > 0){
        				// 성공
        				result = work+"|SUCCESS|"+groupCode;
        			}else {
        				// 실패
        				result = work+"|FAIL"+groupCode;
        			}	
        		} else {
        			// 그룹명 입력되지 않음
        			result = work+"|GROUPNM|EMPTY";
        		}
        	} else {
        		// 요청작업 명시되지 않음
        		result = "WORK|EMPTY";
        	}
		}catch (Exception e){
			result = "ERROR";
			e.printStackTrace();
		}
        
        return result;
		
	}
	
	@RequestMapping ("/backoffice/sub/equiManage/insertGroupInDid.do")
	@ResponseBody
	public String insertGroupInDid(HttpServletRequest request)throws Exception{
		String result = "";
		try{
			
			GroupDidInfoVO vo = new GroupDidInfoVO();
			String groupCode = request.getParameter("groupCode") == null ? "" : request.getParameter("groupCode");
			String addDeviceId = request.getParameter("addDeviceId") == null ? "" : request.getParameter("addDeviceId");
	        
			String[] addDevice = addDeviceId.split(",");
			vo.setGroupCode(groupCode);
			for(int i = 0; i < addDevice.length; i++){
				System.out.println("추가하고자 하는 DEVICE ID : " + addDevice[i]);
				vo.setDidId(addDevice[i]);
				if(i != 0){
					result += ",";
				}
				if(groupDidInfoManageService.insertGroupInfoManage(vo) > 0){
					result += addDevice[i]+"|SUCCESS";
				} else {
					result += addDevice[i]+"|FAIL";
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}				
		return result;
	}
	
	
}
