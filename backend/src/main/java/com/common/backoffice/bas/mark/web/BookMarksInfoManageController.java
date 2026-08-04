package com.common.backoffice.bas.mark.web;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.backoffice.bas.mark.models.dto.BookMarksInfoReqDto;
import com.common.backoffice.bas.mark.service.BookMarksInfoManageService;
import com.common.backoffice.util.service.AuthHelper;
import com.common.backoffice.util.service.PaginationHelper;

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 즐겨찾기(TB_BOOKMARKS) 관리 API.
 * basic/backend(com.common.backoffice.bas.mark.web.BookMarksInfoManageController)를
 * 참조해서 이식함 — AppLayout의 UserBookmarks.jsx가 호출하지만 그동안 컨트롤러 자체가
 * 없어서 401(Spring Security의 /error 내부 forward 인증 실패)이 나던 것을 해결.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/bas/books")
@Tag(name = "BookMarksInfoManageController", description = "즐겨찾기 정보 API")
public class BookMarksInfoManageController {

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertiesService;

	@Resource(name = "egovMessageSource")
	EgovMessageSource egovMessageSource;

	private final BookMarksInfoManageService bookMarksInfoManageService;

	@Operation(summary = "즐겨찾기 정보 리스트", description = "성공시 즐겨찾기 정보 리스트를 조회합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/bookmarksListAjax.do")
	public ResultVO selectBookMarksInfoListAjax(@RequestBody Map<String, Object> searchVO,
												 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			PaginationInfo paginationInfo = PaginationHelper.buildInfo(searchVO,
					propertiesService.getInt(Globals.PAGE_UNIT), propertiesService.getInt(Globals.PAGE_SIZE));
			List<Map<String, Object>> list = bookMarksInfoManageService.selectBookMarksInfoList(searchVO);
			PaginationHelper.setResult(resultVO, list, paginationInfo, searchVO);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "selectBookMarksInfoListAjax", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "즐겨찾기 정보 저장", description = "성공시 즐겨찾기 정보를 저장합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/updateBookMarksInfo.do")
	public ResultVO updateBookMarksInfo(@RequestBody List<BookMarksInfoReqDto> info,
										 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = bookMarksInfoManageService.insertBookMarksInfo(info);
			String messageKey = ret > 0 ? "sucess.common.insert" : "fail.common.insert";
			ResultHelper.setCudMsgResult(resultVO, ret, messageKey, egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateBookMarksInfo", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "즐겨찾기 순서 변경", description = "성공시 즐겨찾기 순서를 일괄 변경합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/updateBookMarksOrder.do")
	public ResultVO updateBookMarksOrder(@RequestBody List<BookMarksInfoReqDto> info,
										  HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
			LoginVO loginVO = AuthHelper.getLoginVO();

			int ret = bookMarksInfoManageService.updateBookMarksOrder(loginVO.getManagerId(), info);
			String messageKey = ret > 0 ? "sucess.common.insert" : "fail.common.insert";
			ResultHelper.setCudMsgResult(resultVO, ret, messageKey, egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "updateBookMarksOrder", e, egovMessageSource);
		}
		return resultVO;
	}

	@Operation(summary = "즐겨찾기 정보 삭제", description = "성공시 즐겨찾기 정보를 삭제합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "성공"),
			@ApiResponse(responseCode = "500", description = "실패")
	})
	@PostMapping("/deleteBookMarksInfo.do")
	public ResultVO deleteBookMarksInfo(@RequestBody List<BookMarksInfoReqDto> info,
										 HttpServletRequest request) throws Exception {
		ResultVO resultVO = new ResultVO();
		try {
			if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;

			int ret = bookMarksInfoManageService.deleteBookMarksInfo(info);
			String messageKey = ret > 0 ? "sucess.common.insert" : "fail.common.insert";
			ResultHelper.setCudMsgResult(resultVO, ret, messageKey, egovMessageSource);

		} catch (Exception e) {
			ResultHelper.setFailResult(resultVO, "deleteBookMarksInfo", e, egovMessageSource);
		}
		return resultVO;
	}
}
