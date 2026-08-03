package egovframework.com.jwt;

import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.let.utl.fcc.service.EgovStringUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtVerification {

	private final EgovJwtTokenUtil jwtTokenUtil;


	public boolean isVerification(HttpServletRequest request) {
		
		boolean verificationFlag = true;
		
		
		
		
		// step 1. request header에서 토큰을 가져온다.
		String jwtToken = EgovStringUtil.isNullToString(request.getHeader("authorization").replace("Bearer", ""));
		log.info("jwtToken:" + jwtToken);

		// step 2. 토큰에 내용이 있는지 확인 & 토큰 기간이 자났는지를 확인해서 username값을 가져옴
		// Exception 핸들링 추가처리
		String username = null;

		try {
			username = jwtTokenUtil.getUsernameFromToken(jwtToken);
		} catch (IllegalArgumentException e) {
			log.debug("Unable to get JWT Token");
		} catch (ExpiredJwtException e) {
			log.debug("JWT Token has expired");
		} catch (MalformedJwtException e) {
			log.debug("JWT strings must contain exactly 2 period characters");
		} catch (UnsupportedJwtException e) {
			log.debug("not support JWT token.");
		}


		// step 3. 토큰 자체의 만료 여부를 체크한다(세션이 아닌 토큰만으로 검증 — validateToken의 두 번째 인자는 미사용).

		log.debug("username:" + username);

		if (username == null || !(jwtTokenUtil.validateToken(jwtToken, null))) {
			log.debug("jwtToken not validate       jwtToken not validate");
			verificationFlag =  false;
			return verificationFlag;
		}

		log.debug("jwtToken validated");

		return verificationFlag;
	}


	public boolean isVerificationManager(HttpServletRequest request) {

		boolean verificationFlag = true;

		// step 1. request header에서 토큰을 가져온다.
		if (request.getHeader("authorization") == null  ) {
			log.error("authorization error");
			return false;
		}

		String jwtToken = EgovStringUtil.isNullToString(request.getHeader("authorization").replace("Bearer", ""));

		if (jwtTokenUtil.isBlackListCheck(jwtToken) == true) {
			log.error("blacklist check user");
			return false;
		}

		// step 2. 토큰에 내용이 있는지 확인 & 토큰 기간이 자났는지를 확인해서 username값을 가져옴
		// Exception 핸들링 추가처리
		String username = null;

		try {
			username = jwtTokenUtil.getUsernameFromToken(jwtToken);
		} catch (IllegalArgumentException e) {
			log.debug("Unable to get JWT Token");
		} catch (ExpiredJwtException e) {
			log.debug("JWT Token has expired");
		} catch (MalformedJwtException e) {
		    	log.debug("JWT strings must contain exactly 2 period characters");
		} catch (UnsupportedJwtException e) {
		    	log.debug("not support JWT token.");
		}

		//log.debug("===>>> username = " + username);

		// step 3. 토큰 자체의 만료 여부를 체크한다(세션이 아닌 토큰만으로 검증 — validateToken의 두 번째 인자는 미사용).
		if (username == null || !(jwtTokenUtil.validateToken(jwtToken, null))) {
			//log.debug("jwtToken not validate");
			verificationFlag =  false;
			return verificationFlag;
		}
		
		//log.debug("jwtToken validated");
		
		return verificationFlag;
	}
	
	// isVerificationClient/isVerificationCustomer(거래처·고객 토큰 검증)는 did_emart에 아직
	// 거래처/고객 도메인이 없어 제거함 — 해당 도메인 모델과 서비스가 만들어지면 다시 추가할 것.

	public boolean isVerificationRefresh(HttpServletRequest request) {
		
		boolean verificationFlag = true;
		
		// step 1. request header에서 토큰을 가져온다.
		if (request.getHeader("refreshToken") == null  ) {
			log.error("refreshToken error");
			return false;
		}
		
		String refreshToken = EgovStringUtil.isNullToString(request.getHeader("refreshToken"));
		
		String id = jwtTokenUtil.getUsernameFromToken(refreshToken);
		
		log.debug("==========================id:" + id);
		
		// step 3. 토큰에 내용이 있는지 확인 & 토큰 기간이 자났는지를 확인해서 username값을 가져옴
		// Exception 핸들링 추가처리
		try {
			if (jwtTokenUtil.isRefreshToken(id) == false) {
				log.debug("redis check");
				verificationFlag =  false;
				return verificationFlag;
			}
			
		} catch (IllegalArgumentException e) {
			log.debug("Unable to get refresh Token");
		} catch (ExpiredJwtException e) {
			log.debug("refresh Token has expired");
		} catch (MalformedJwtException e) {
			log.debug("refresh strings must contain exactly 2 period characters");
		} catch (UnsupportedJwtException e) {
			log.debug("not support refresh token.");
		}
		log.debug("verificationFlag====== " + verificationFlag);
		return verificationFlag;
	}
	
	//사용자 아이디 가지고 오기 
	public String getTokenUserName(HttpServletRequest request) {
		// step 1. request header에서 토큰을 가져온다.
		String jwtToken = EgovStringUtil.isNullToString(request.getHeader("authorization").replace("Bearer", ""));
		String[] tokenS = jwtTokenUtil.getUsernameFromToken(jwtToken).split("\\|");
		return tokenS[1];
	}
	//사용자 정보 전체 가지고 오기
	public String[] getTokenUserInfo(HttpServletRequest request) {
		// step 1. request header에서 토큰을 가져온다.
		String jwtToken = EgovStringUtil.isNullToString(request.getHeader("authorization").replace("Bearer", ""));	
		return jwtTokenUtil.getUsernameFromToken(jwtToken).split("\\|");
	}
	public ResultVO handleAuthError(ResultVO resultVO) {
		System.out.println("handleAuthError ============================= handleAuthError");
		resultVO.setResultCode(ResponseCode.AUTH_ERROR.getCode());
		resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
		resultVO.setResultMessage(ResponseCode.AUTH_ERROR.getMessage());
		return resultVO;
	}
}
