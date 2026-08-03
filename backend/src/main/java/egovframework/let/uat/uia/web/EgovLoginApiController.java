package egovframework.let.uat.uia.web;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.jwt.EgovJwtTokenUtil;
import egovframework.com.jwt.JwtVerification;
import egovframework.let.uat.uia.models.LoginReq;
import egovframework.let.uat.uia.service.EgovLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 로그인(JWT)을 처리하는 컨트롤러 클래스.
 * basic/backend(egovframework.let.uat.uia.web.EgovLoginApiController)를 참조해서
 * did_emart(tb_managerinfo) 기준으로 포팅함. 거래처 로그인은 did_emart에 해당 도메인이
 * 없어 제외함.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@Tag(name = "EgovLoginApiController", description = "로그인 관련")
public class EgovLoginApiController {

	@Resource(name = "egovMessageSource")
	EgovMessageSource egovMessageSource;

	private final EgovJwtTokenUtil jwtTokenUtil;
	private final JwtVerification jwtVerification;
	private final EgovLoginService loginService;

	@Operation(
			summary = "JWT 로그인",
			description = "JWT 로그인 처리",
			tags = {"EgovLoginApiController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "300", description = "로그인 실패")
	})
	@PostMapping(value = "/api/loginJwt.do")
	public ResultVO actionLoginJWT(@RequestBody LoginReq loginVO,
									HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();

		try {
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			LoginVO loginResultVO = loginService.actionLogin(loginVO);
			loginResultVO.setIp(getClientIp(request));

			if (loginResultVO != null && loginResultVO.getManagerId() != null && !loginResultVO.getManagerId().equals("")) {

				String jwtToken = jwtTokenUtil.generateToken(loginResultVO);
				String refreshToken = jwtTokenUtil.generateRefreshToken(loginResultVO);

				resultMap.put(Globals.RESULTVO, loginResultVO);
				resultMap.put(Globals.TOKEN, jwtToken);
				resultMap.put(Globals.REFRESH_TOKEN, refreshToken);

				resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
				resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
				resultVO.setResultMessage(egovMessageSource.getMessage("success.common.login"));
				resultVO.setResult(resultMap);
			} else {
				resultVO.setResultCode(300);
				resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
				resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.login"));
			}
		} catch (Exception e) {
			log.error("actionLoginJWT error: {}", e.toString());
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			resultVO.setResultCode(500);
			resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.msg"));
		}

		return resultVO;
	}

	// X-Forwarded-For 헤더 등에서 클라이언트 IP 추출
	public static String getClientIp(HttpServletRequest request) {
		String[] headerNames = {
				"X-Forwarded-For",
				"X-Real-IP",
				"CF-Connecting-IP",
				"True-Client-IP",
				"Forwarded"
		};

		for (String header : headerNames) {
			String value = request.getHeader(header);
			if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
				String ip = value.split(",")[0].trim();
				return normalizeLoopbackIp(ip);
			}
		}
		return normalizeLoopbackIp(request.getRemoteAddr());
	}

	public static String normalizeLoopbackIp(String ip) {
		if (ip == null) return "";
		if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
			return "127.0.0.1";
		}
		return ip;
	}

	@Operation(
			summary = "REFRESH TOKEN",
			description = "REFRESH TOKEN으로 재로그인을 처리한다",
			tags = {"EgovLoginApiController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "300", description = "로그인 실패")
	})
	@GetMapping(value = "/uat/uia/actionRefreshToken.do")
	public ResultVO actionRefreshLogin(HttpServletRequest request) throws Exception {

		ResultVO resultVO = new ResultVO();
		if (!jwtVerification.isVerificationRefresh(request)) {
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			resultVO.setResultCode(403);
			resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.refreshlogin"));
			return resultVO;
		}

		LoginVO loginResultVO = loginService.actionLoginResfresh(
				jwtTokenUtil.getUsernameFromToken(request.getHeader("refreshToken")));

		if (loginResultVO != null && loginResultVO.getManagerId() != null && !loginResultVO.getManagerId().equals("")) {

			String jwtToken = jwtTokenUtil.generateToken(loginResultVO);
			String refreshToken = jwtTokenUtil.generateRefreshToken(loginResultVO);

			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("resultVO", loginResultVO);
			resultMap.put(Globals.TOKEN, jwtToken);
			resultMap.put(Globals.REFRESH_TOKEN, refreshToken);

			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResultCode(200);
			resultVO.setResultMessage(egovMessageSource.getMessage("success.common.login"));
			resultVO.setResult(resultMap);
		} else {
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			resultVO.setResultCode(403);
			resultVO.setResultMessage(egovMessageSource.getMessage("fail.common.login"));
		}

		return resultVO;
	}

	@Operation(
			summary = "로그아웃",
			description = "로그아웃 처리(JWT)",
			tags = {"EgovLoginApiController"}
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
	})
	@GetMapping(value = "/uat/uia/actionLogoutJWT.do")
	public ResultVO actionLogoutJSON(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResultVO resultVO = new ResultVO();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		jwtTokenUtil.deleteRefreshToken(request);
		new SecurityContextLogoutHandler().logout(request, response, null);

		resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
		resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
		resultVO.setResultMessage(egovMessageSource.getMessage("success.common.msg"));
		resultVO.setResult(resultMap);

		return resultVO;
	}
}
