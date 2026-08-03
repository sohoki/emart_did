package com.common.backoffice.util.service;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.ResultHelper;

/**
 * 공통 인증 헬퍼.
 *
 * <p>컨트롤러마다 반복되는 SecurityContext 인증 체크 코드를 통합한다.</p>
 *
 * <pre>
 * // 사용 예 1 — 인증 체크만
 * if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
 *
 * // 사용 예 2 — 인증 체크 + 로그인 사용자 취득
 * if (!AuthHelper.isAuthenticated(resultVO)) return resultVO;
 * ManagerLoginVO loginVO = AuthHelper.getLoginVO();
 * </pre>
 */
public final class AuthHelper {

	private AuthHelper() {}

	/**
	 * 인증 체크. 미인증이면 resultVO에 실패 세팅 후 false 반환.
	 *
	 * @param resultVO 응답 객체 — 미인증 시 실패 코드가 세팅됨
	 * @return 인증 성공 여부
	 */
	public static boolean isAuthenticated(ResultVO resultVO) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof LoginVO)) {
			ResultHelper.setFailAuthResult(resultVO);
			return false;
		}
		return true;
	}

	/**
	 * 현재 로그인 사용자 반환.
	 * {@link #isAuthenticated(ResultVO)} 통과 후에만 호출할 것.
	 *
	 * @return 현재 로그인한 {@link ManagerLoginVO}
	 */
	public static LoginVO getLoginVO() {
		return (LoginVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * 단건 조회/수정/삭제 대상의 소유 comCode가 현재 로그인 계정의 것인지 확인한다.
	 * 내부 관리자 로그인은 항상 true(전체 허용). 거래처(SYSTEM_VENDOR) 로그인은 자기 comCode와
	 * 일치할 때만 true — PK(예: comUserId, hotelCode)만으로 다른 거래처 레코드에 접근하는 것을 막기 위함.
	 *
	 * @param ownerComCode 대상 레코드가 실제로 속한 comCode (DB에서 조회한 값)
	 */
	public static boolean isOwnTenant(String ownerComCode) {
		LoginVO loginVO = getLoginVO();
		if (!Globals.SYSTEM_VENDOR.equals(loginVO.getSystemcodeUsecode())) {
			return true;
		}
		return loginVO.getComCode() != null && loginVO.getComCode().equals(ownerComCode);
	}
}
