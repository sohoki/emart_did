package com.common.backoffice.bas.menu.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.PaginationHelper;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.util.ResultHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;

import com.common.backoffice.bas.menu.modals.MenuInfo;
import com.common.backoffice.bas.menu.modals.dto.MenuInfoRequestDto;
import com.common.backoffice.bas.menu.modals.dto.MenuOrderReqDto;
import com.common.backoffice.bas.menu.service.MenuCreateManageService;
import com.common.backoffice.bas.menu.service.MenuInfoManageService;
import com.common.backoffice.bas.uni.service.UniUtilManageService;
import com.common.backoffice.bas.uni.service.UtilInfoService;
import com.common.backoffice.util.service.fileMultiService;
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
@RequestMapping("/api/backoffice/sys/menu")
@Tag(name="MenuInfoManageController",description = "메뉴 관리 정보 API")
public class MenuInfoManageController {


    @Value("${Common.filePath}")
    private String filePath;

	/** EgovPropertyService */
	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;
    @Resource(name = "egovMessageSource")
    EgovMessageSource egovMessageSource;

	private final MenuInfoManageService menuService;
	
	//파일 업로드
	private final fileMultiService uploadFile;
	private final MenuCreateManageService menuCreateService;

	/**
	 * 메뉴 목록 조회
	 * @param searchVO
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴 관리 정보 리스트",
			description = "성공시 메뉴 관리 정보 리스트를 조회 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/menuListAjax.do")
	public ResultVO selectMenuInfoListAjax(@RequestBody Map<String, Object> searchVO,
			 									HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try {
			
			// 기존 세션 체크 인증에서 토큰 방식으로 변경
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginvo = AuthHelper.getLoginVO();
            searchVO.put(Globals.PAGE_LOGIN_ROLEID, loginvo.getRoleId());
            searchVO.put(Globals.PAGE_LOGIN_PARTID , loginvo.getPartId());

			
			//searchVO.put("searchSystemCode", "DISP");
            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
                    propertiesService.getInt(Globals.PAGE_UNIT), propertiesService.getInt(Globals.PAGE_SIZE));
            List<Map<String, Object>> menuList = menuService.selectMenuManageList(searchVO);
            PaginationHelper.setResult(resultVO, menuList, paginationInfo, searchVO);
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectMenuInfoListAjax", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "selectMenuInfoListAjax", e, egovMessageSource);
		}
		return resultVO;
	}
	
	/**
	 * 메뉴 상세 조회
	 * @param menuNo
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴 정보 상세",
			description = "성공시 메뉴 정보 상세를 조회 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("/{menuNo}.do")
	public ResultVO selectMenuDetailInfo(@Parameter(description="\"menuNo CODE") @PathVariable("menuNo") String  menuNo,
											 HttpServletRequest request) throws Exception {
        ResultVO resultVO = new ResultVO();
		// 기존 세션 체크 인증에서 토큰 방식으로 변경
        try{
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put(Globals.JSON_RETURN_RESULT, menuService.selectMenuManage(menuNo));

            resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
            resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
            resultVO.setResult(resultMap);

        }catch (Exception e){

        }

		return resultVO;
	}
	@Operation(
			summary = "사용자별 메뉴 정보",
			description = "성공시 사용자별 메뉴 정보 상세를 조회 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@NoLogging
	@GetMapping("/menuNoLeft.do")
	public ResultVO selectMenuLeftInfo( HttpServletRequest request) throws Exception {
		
		// 기존 세션 체크 인증에서 토큰 방식으로 변경
		ResultVO resultVO = new ResultVO();
		try {

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginvo = AuthHelper.getLoginVO();
            Map<String, Object> resultMap = new HashMap<String, Object>();




            resultMap.put(Globals.ADMIN_INFO, loginvo.getManagerId());
            resultMap.put(Globals.ADMIN_ROLE_INFO, loginvo.getRoleId());
            resultMap.put(Globals.JSON_RETURN_RESULT,
                    menuService.selectMainMenuLeft(loginvo.getManagerId(),
                                                    loginvo.getRoleId(),
                                                    request.getScheme()+"://" + request.getServerName()+":"+ request.getServerPort()
                                                    )
            );
            resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
            resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
            resultVO.setResult(resultMap);
				

		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectMenuLeftInfo", e1, egovMessageSource);
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "selectMenuLeftInfo", e, egovMessageSource);
		}
		return resultVO;
	}
	
	/**
	 * 메뉴 저장
	 * @param menuInfo
	 * @return
	 * @throws Exception
	 */
	
	@Operation(
			summary = "메뉴 저장",
			description = "성공시 메뉴 저장 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("menuUpdate.do")
	public ResultVO updateMenuInfoManage(@Valid @RequestBody MenuInfoRequestDto info,
											MultipartRequest mRequest,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		
		try {

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginvo = AuthHelper.getLoginVO();
            info.setUserId(loginvo.getManagerId());

			
			int ret = menuService.updateMenuManage(info);
			
			
			String status = ret > 0 ?
					Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = status.equals( Globals.STATUS_SUCCESS) ?
					StringUtils.equals(info.getMode(), Globals.SAVE_MODE_INSERT) ?
							"sucess.common.insert" : "sucess.common.update"
					: StringUtils.equals(info.getMode(), Globals.SAVE_MODE_INSERT) ?
							 "fail.common.insert" : "fail.common.update";
			int res = status.equals( Globals.STATUS_SUCCESS)  ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
	
	
			resultVO.setResultCode(res);
			resultVO.setResultMessage(egovMessageSource.getMessage(message));
			resultVO.setResultCodeInfo(status);
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "updateMenuInfoManage", e1, egovMessageSource);
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "updateMenuInfoManage", e, egovMessageSource);
		}
		return resultVO;
		
	}

	/**
	 * 메뉴 삭제
	 * @param menuInfo
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴 삭제",
			description = "성공시 메뉴 삭제 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("/{menuNo}.do")
	public ResultVO deleteMenInfoManage(@Parameter(description="메뉴 코드") @PathVariable("menuNo") String  menuNo,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = menuService.deleteMenuManage(menuNo);
			if (ret > 0) {
				/*
				MessageDto dto =  MessageDto.builder()
						.id(menuNo)
						.processGubun("DEL")
						.processName("MENUINFO")
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
				
			}else {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.delete"));
			}
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "deleteMenInfoManage", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "deleteMenInfoManage", e, egovMessageSource);
		}
		return resultVO;

	}

	/**
	 * 메뉴 아이디 중복 체크
	 * @param menuNo
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴 아이디 중복 체크",
			description = "성공시 메뉴 아이디 중복 체크 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@NoLogging
	@GetMapping("/menuCheck/{menuNo}.do")
	public ResultVO menuNoCheck(@Parameter(description="메뉴 코드") @PathVariable String menuNo,
									@RequestParam Map<String, Object> commandMap,
									HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			
			boolean ret = menuService.existsByMenuNm(menuNo);

            String stuts = (ret == false) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			String message = (ret == false)? "common.codeOk.msg" : "common.codeFail.msg";
			
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(stuts);
			resultVO.setResultMessage(egovMessageSource.getMessage(message));
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "menuNoCheck", e1, egovMessageSource);
		}catch(Exception e){
            ResultHelper.setFailResult(resultVO, "menuNoCheck", e, egovMessageSource);
		}
		return resultVO;
		
	}

	/**
	 * 메뉴목록 멀티 삭제한다.
	 * @param checkedMenuNoForDel  String
	 * @return 출력페이지정보 "forward:/sym/mnu/mpm/EgovMenuManageSelect.do"
	 * @exception Exception
	 */
	@Operation(
			summary = "메뉴목록 멀티 삭제한다.",
			description = "성공시 메뉴목록 멀티 삭제합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("menuManageListDelete.do")
	public ResultVO deleteMenuManageList(@RequestParam("checkedMenuNoForDel") String checkedMenuNoForDel, 
			                                 HttpServletRequest request)throws Exception {
		
		ResultVO resultVO = new ResultVO();
		// 0. Spring Security 사용자권한 처리
		
		
		
		try {

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			int ret = menuService.deleteMenuManageList(checkedMenuNoForDel);
			int res = 0;
			String status = "";
			String message = "";
			if (ret == -1) {
				res = ResponseCode.SERVER_ERROR.getCode();
				status = Globals.STATUS_FAIL;
				message = "menu.exist.fail";
			} else if (ret == 0 ) {
				res = ResponseCode.SERVER_ERROR.getCode();
				status = Globals.STATUS_FAIL;
				message = "fail.common.delete";
		
			} else {
				menuService.deleteMenuManageList(checkedMenuNoForDel);
				res = ResponseCode.SUCCESS.getCode();
				status = Globals.STATUS_SUCCESS;
				message = "success.common.delete";
			}
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(egovMessageSource.getMessage(message));
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "deleteMenuManageList", e1, egovMessageSource);

		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "deleteMenuManageList", e, egovMessageSource);
		}
		return resultVO;
	}

	/**
	 * 메뉴정보를 등록화면으로 이동 및 등록 한다.
	 * @param commandMap
	 * @param menuIno
	 * @param bindingResult
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴 업데이트.",
			description = "성공시 메뉴 업데이트합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("menuRegistUpdate.do")
	public ResultVO insertMenuManage(MultipartRequest mRequest,
										MenuInfoRequestDto menuIno, 
										HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            menuIno.setUserId(UtilInfoService.NVLObj(loginVO.getManagerId(), ""));
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("menuNo", menuIno.getMenuNo());
			
				
			if (menuIno.getMode().equals(Globals.SAVE_MODE_INSERT) && menuService.selectMenuNoByPk(params) != 0 ) {
				resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
				resultVO.setResultMessage(this.egovMessageSource.getMessage("common.isExist.msg"));
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				return resultVO;
			}

            if ("ICON_TYPE_1".equals(menuIno.getMenuIconType())) {
                List<MultipartFile> files = mRequest.getFiles("relateImage");
                if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
                    String fileNm = uploadFile.uploadFileNm(files, filePath);
                    menuIno.setRelateImageNm(fileNm);
                    menuIno.setRelateImagePath(filePath);
                }
            }
		
			int ret = menuService.updateMenuManage(menuIno);
            String message = (ret > 0) ? "success.common.insert" : "fail.common.insert";
            String status = (ret > 0) ? Globals.STATUS_SUCCESS: Globals.STATUS_FAIL;
            int resCode = ret > 0 ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

            resultVO.setResultCode(resCode);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(egovMessageSource.getMessage(message));
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "insertMenuManage", e1, egovMessageSource);
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "insertMenuManage", e, egovMessageSource);
		}
		return resultVO;
		
	}

	/**
	 * 메뉴 트리 드래그&드롭 이동/순서변경
	 * @param list
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴 트리 드래그&드롭 이동/순서변경.",
			description = "성공시 메뉴의 순서(MENU_ORDR)/상위메뉴(UPPER_MENU_NO)를 일괄 변경합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("updateMenuOrder.do")
	public ResultVO updateMenuOrder(@RequestBody List<MenuOrderReqDto> list,
									 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = menuService.updateMenuOrder(list);
			String message = (ret > 0) ? "success.common.insert" : "fail.common.insert";
			String status = (ret > 0) ? Globals.STATUS_SUCCESS : Globals.STATUS_FAIL;
			int resCode = ret > 0 ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

			resultVO.setResultCode(resCode);
			resultVO.setResultCodeInfo(status);
			resultVO.setResultMessage(egovMessageSource.getMessage(message));
		} catch (NullPointerException e1) {
			ResultHelper.setFailResult(resultVO, "updateMenuOrder", e1, egovMessageSource);
		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateMenuOrder", e, egovMessageSource);
		}
		return resultVO;
	}

	/*### 일괄처리 프로세스 ###*/

	/**
	 * 메뉴생성 일괄삭제프로세스
	 * @param menuInfo
	 * @param mmp
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴생성 일괄삭제프로세스.",
			description = "성공시 메뉴생성 일괄삭제프로세스합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@DeleteMapping("menuBndeAllDelete.do")
	public ResultVO menuBndeAllDelete(HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		// 0. Spring Security 사용자권한 처리
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			
			
			menuService.menuBndeAllDelete();
			
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResultMessage( egovMessageSource.getMessage("success.common.delete"));
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "menuBndeAllDelete", e1, egovMessageSource);

		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "menuBndeAllDelete", e, egovMessageSource);
		}
		return resultVO;
	}

	/**
	 * 메뉴일괄등록화면 호출 및  메뉴일괄등록처리 프로세스
	 * @param commandMap
	 * @param request
	 * @param menuInfo
	 * @param mmp
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "메뉴일괄등록처리 프로세스.",
			description = "성공시 메뉴일괄등록처리 프로세스 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping(value = "menuBndeRegist.do")
	public ResultVO menuBndeRegist(@RequestParam Map<String, Object> commandMap, 
										final HttpServletRequest request, 
										@ModelAttribute("MenuInfo") MenuInfo menuInfo,
										ModelMap mmp) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			
			String sMessage = "";
			String status = "";
			
			// 0. Spring Security 사용자권한 처리
			
			final MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			final Map<String, MultipartFile> files = multiRequest.getFileMap();
			Iterator<Entry<String, MultipartFile>> itr = files.entrySet().iterator();
			MultipartFile file = null;
			InputStream fis = null;
			while (itr.hasNext()) {
				Entry<String, MultipartFile> entry = itr.next();
				try {
					file = entry.getValue();
					fis = file.getInputStream();
					if (!"".equals(file.getOriginalFilename())) {
						// 2011.10.07 업로드 파일에 대한 확장자를 체크
						if (file.getOriginalFilename().toLowerCase().endsWith(".xls") || file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
							if (menuService.menuBndeAllDelete()) {
								sMessage = menuService.menuBndeRegist(menuInfo, fis);
//								resultMsg = sMessage;
							} else {
								
								sMessage = "EgovMenuBndeRegist Error!!";
								status = Globals.STATUS_FAIL;
								
							}
						} else {
							
							sMessage = "xls, xlsx 파일 타입만 등록이 가능합니다.";
							status = Globals.STATUS_FAIL;
							
						}
						// *********** 끝 ***********
					} else {
						sMessage = egovMessageSource.getMessage("fail.common.msg");
					}

				} finally {
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException ee) {
						log.debug(egovMessageSource.getMessage("fail.common.msg"));
					}
				}

			}
			
			int res = status.equals(Globals.STATUS_SUCCESS) ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();
			resultVO.setResultCode(res);
			resultVO.setResultMessage(sMessage);
			resultVO.setResultCodeInfo(status);
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "menuBndeRegist", e1, egovMessageSource);

		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "menuBndeRegist", e, egovMessageSource);
		}
		return resultVO;
		
	}
	
	@Operation(
			summary = "메뉴 조회 프로세스.",
			description = "성공시 메뉴 리스트 조회 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("menuCreateListAjax.do")
	public ResultVO selectAuthInfoListAjax(	@RequestBody Map<String, Object> searchVO, 
												HttpServletRequest request) throws Exception {
		
		ResultVO resultVO = new ResultVO();
		
		try {

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			searchVO.put(Globals.PAGE_SIZE, propertiesService.getInt(Globals.PAGE_SIZE));

            PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
                    propertiesService.getInt(Globals.PAGE_UNIT), propertiesService.getInt(Globals.PAGE_SIZE));
            List<Map<String, Object>> list = menuCreateService.selectMenuCreatManagList(searchVO);
            PaginationHelper.setResult(resultVO, list, paginationInfo, searchVO);
			
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "selectAuthInfoListAjax", e1, egovMessageSource);
			
		}catch(Exception e) {
            ResultHelper.setFailResult(resultVO, "selectAuthInfoListAjax", e, egovMessageSource);
			
		}
		return resultVO;
	}

	/**
	 * 권한 코드에 따른 매핑 메뉴 정보 저장
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "권한 코드에 따른 매핑 메뉴 정보 저장",
			description = "성공시 권한 코드에 따른 매핑 메뉴 정보 저장 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/menuCreateUpdateAjax.do")
	public ResultVO updateMenuCreateAjax(@RequestBody Map<String, Object> params,
											HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		
		try {
            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
            String userId = UtilInfoService.NVLObj(loginVO.getManagerId(),"");

				
				
				
            String roleId = String.valueOf(params.get("roleId"));
            String checkedMenuNo = String.valueOf(params.get("checkedMenuNo"));
            String hid_menuGubun = UtilInfoService.NVLObj(params.get("menuGubun"), "MENU_GUBUN_1");
            List<Map<String, Object>> menuList =  (params.get("checkedMenuBasic") != null) ?
                                                 (List<Map<String, Object>>) params.get("checkedMenuBasic"): null;
            int res = 0;
            int ret =  menuCreateService.insertMenuCreatList(roleId, userId, checkedMenuNo, hid_menuGubun, menuList);

            String message = (ret > 0) ? "success.common.update" : "fail.common.update";
            String status = (ret > 0) ? Globals.STATUS_SUCCESS: Globals.STATUS_FAIL;
            int resCode = ret > 0 ? ResponseCode.SUCCESS.getCode() : ResponseCode.SERVER_ERROR.getCode();

            resultVO.setResultCode(resCode);
            resultVO.setResultCodeInfo(status);
            resultVO.setResultMessage(egovMessageSource.getMessage(message));
		}catch(NullPointerException e1) {
            ResultHelper.setFailResult(resultVO, "updateMenuCreateAjax", e1, egovMessageSource);
		}catch (Exception e){
            ResultHelper.setFailResult(resultVO, "updateMenuCreateAjax", e, egovMessageSource);
		}
		return resultVO;
		
	}
	
	/**
	 * 권한 코드에 매핑된 메뉴 목록 조회 
	 * @param authorCode
	 * @return
	 * @throws Exception
	 */
	@Operation(
			summary = "권한 코드에 매핑된 메뉴 목록 조회",
			description = "성공시 거래처별 권한 코드에 매핑된 메뉴 목록 조회 합니다.",
			tags = {"MenuInfoManageController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@GetMapping("roleMenu/{roleId}.do")
	public ResultVO selectMenuCreateMenuListAjax(@RequestParam Map<String, Object> commandMap,
												HttpServletRequest request,
												@PathVariable("roleId") String roleId)throws Exception {
		ResultVO resultVO = new ResultVO();
		try {

            if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
            LoginVO loginVO = AuthHelper.getLoginVO();
			
			String hidMenuGubun = UtilInfoService.NVL(commandMap.get("hidMenuGubun"),"MENU_GUBUN_1");
			
			List<Map<String, Object>> list = menuCreateService.selectMenuCreatList_Author(roleId, hidMenuGubun);
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(Globals.JSON_RETURN_RESULT_LIST, list);
			
			
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResult(resultMap);
			
		}catch (NullPointerException e) {
            ResultHelper.setFailResult(resultVO, "selectMenuCreateMenuListAjax", e, egovMessageSource);
        }catch (Exception e){
            ResultHelper.setFailResult(resultVO, "selectMenuCreateMenuListAjax", e, egovMessageSource);
        }
		return resultVO;
	}
	/**
	 * 권한 코드에 매핑된 메뉴 목록 조회
	 * 추후 밴더사별 메뉴 목록으로 변경 필요
	 * @param authorCode
	 * @return
	 * @throws Exception
	 */
}
