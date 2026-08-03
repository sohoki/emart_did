package egovframework.com.jwt;

import egovframework.com.cmm.LoginVO;
import egovframework.com.exception.dto.ErrorCode;
import egovframework.com.util.RedisUtil;
import egovframework.let.utl.fcc.service.EgovStringUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
@Component
public class EgovJwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -5180902194184255251L;
	
	private final RedisTemplate<String, String> redisTemplate;
	private final RedisUtil redis;
	
	@Value("${token.secret}")
	private String secretKey;
	
	@Value("${token.expiration_time}")
	private long JWT_TOKEN_VALIDITY;
	
	@Value("${token.refresh_time}")
	private long TOKEN_REFRESH_TIME;
	
	@Value("${token.crypto.algoritm}")
	private String secret;

	// ── 공통 키 생성 헬퍼 ──────────────────────────────────────────────────────
	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
	
	private SecretKey getSigningSecretKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}
	
	// ── 토큰 파싱 ──────────────────────────────────────────────────────────────
	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser()
				.verifyWith(getSecretKey())          // 0.12.x API
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(getAllClaimsFromToken(token));
	}
	
	public Claims getClaimFromToken(String token) {
		return getAllClaimsFromToken(token);
	}
	
	// ── 사용자 정보 추출 ────────────────────────────────────────────────────────
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token.replace("Bearer", "").trim(), Claims::getSubject);
	}
	
	public String getUserIdFromToken(String token) {
		Object val = getAllClaimsFromToken(token).get("managerId");
		return val != null ? val.toString() : null;
	}

	public String getRoleIdFromToken(String token) {
		Object val = getAllClaimsFromToken(token).get("roleId");
		return val != null ? val.toString() : null;
	}

	// generateToken()/generateManagerToken()이 claims에 담아둔 사용자 정보를 전부 복원한다 —
	// JwtAuthenticationFilter가 managerId/roleId만 꺼내 쓰고 나머지(partId 등)는 항상 null로
	// 남겨두던 문제를 근본적으로 해결하기 위해 claims 전체를 LoginVO로 복원하는 헬퍼로 통일한다.
	public LoginVO getLoginVOFromToken(String token) {
		Claims claims = getAllClaimsFromToken(token);
		LoginVO vo = new LoginVO();
		vo.setManagerId(asString(claims.get("managerId")));
		vo.setManagerName(asString(claims.get("managerName")));
		vo.setPartId(asString(claims.get("partId")));
		vo.setPartName(asString(claims.get("partName")));
		vo.setEmpNo(asString(claims.get("empNo")));
		vo.setRoleId(asString(claims.get("roleId")));
		vo.setRoleGubun(asString(claims.get("roleGubun")));
		vo.setManagerStatus(asString(claims.get("managerStatus")));
		vo.setManagerPic(asString(claims.get("managerPic")));
		vo.setComName(asString(claims.get("comName")));
		vo.setComGubun(asString(claims.get("comGubun")));
		vo.setComLogo(asString(claims.get("comLogo")));
		vo.setComCode(asString(claims.get("comCode")));
		vo.setSystemcodeUsecode(asString(claims.get("systemcodeUsecode")));
		return vo;
	}

	private String asString(Object val) {
		return val != null ? val.toString() : null;
	}
	
	public String getUserSeFromToken(String token) {
		return getAllClaimsFromToken(token).get("userSe").toString();
	}
	
	public String getInfoFromToken(String type, String token) {
		return getAllClaimsFromToken(token).get(type).toString();
	}
	
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token.replace("Bearer", "").trim(), Claims::getExpiration);
	}
	
	private Boolean isTokenExpired(String token) {
		return getExpirationDateFromToken(token).before(new Date());
	}
	
	// ── 토큰 요청 헤더에서 추출 ─────────────────────────────────────────────────
	public String resolveToken(HttpServletRequest req) {
		String bearerToken = req.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
	
	// ── 토큰 검증 ──────────────────────────────────────────────────────────────
	public Boolean validateToken(String token, LoginVO loginVO) {
		final String username = getUsernameFromToken(token);
		return (username != null && !isTokenExpired(token));
	}
	
	public Boolean validateToken(String token) throws BaseException {
		try {
			Jwts.parser()
				.verifyWith(getSigningSecretKey())   // 0.12.x API
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.error(ErrorCode.EXPIRED_JWT.getMessage());
			throw new BaseException(ErrorCode.EXPIRED_JWT.getMessage());
		} catch (JwtException e) {
			log.error(ErrorCode.INVALID_JWT.getMessage());
			throw new BaseException(ErrorCode.INVALID_JWT.getMessage());
		}
	}
	
	// ── Authentication 생성 ────────────────────────────────────────────────────
	// 로그인 서비스 재조회 없이, 발급 시 claims에 담아둔 사용자 정보를 그대로 복원한다.
	// (관리자 로그인 도메인/서비스가 구현되면 DB 재조회가 필요한 시나리오에 맞게 확장할 것)
	public Authentication getAuthentication(String token) {
		LoginVO resultVO = getLoginVOFromToken(token);

		return new UsernamePasswordAuthenticationToken(
			resultVO.getManagerId(),
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))  // authorities 필수
		);
	}
	
	// ── 토큰 생성 ──────────────────────────────────────────────────────────────
	public String generateToken(LoginVO loginVO) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("managerId",      loginVO.getManagerId());
		claims.put("managerName",    loginVO.getManagerName());
		claims.put("partId",         loginVO.getPartId());
		claims.put("partName",       loginVO.getPartName());
		claims.put("empNo",          loginVO.getEmpNo());
		claims.put("roleId",         loginVO.getRoleId());
		claims.put("roleGubun",      loginVO.getRoleGubun());
		claims.put("managerStatus",  loginVO.getManagerStatus());
		claims.put("managerPic",     loginVO.getManagerPic());
		claims.put("comName",        loginVO.getComName());
		claims.put("comGubun",       loginVO.getComGubun());
		claims.put("comLogo",        loginVO.getComLogo());
		claims.put("comCode",        loginVO.getComCode());
		claims.put("systemcodeUsecode", loginVO.getSystemcodeUsecode());

		return doGenerateToken(claims,
			 loginVO.getManagerName() + "|" + loginVO.getManagerId()
			 + "|" + loginVO.getRoleId() + "|" + loginVO.getPartId());
	}
	
	public String generateManagerToken(LoginVO loginVO) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("managerId",   loginVO.getManagerId());
		claims.put("managerName", loginVO.getManagerName());
		claims.put("roleId",      loginVO.getRoleId());
		claims.put("partId",      loginVO.getPartId());
		
		return doGenerateToken(claims,
			loginVO.getManagerName() + "|" + loginVO.getManagerId()
			+ "|" + loginVO.getRoleId() + "|" + loginVO.getPartId());
	}
	
	private String doGenerateToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
			.claims(claims)                             // 0.12.x: setClaims → claims()
			.subject(subject)                           // 0.12.x: setSubject → subject()
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
			.signWith(getSecretKey())                   // 0.12.x: 알고리즘 자동 선택
			.compact();
	}
	
	private String doGenerateToken(LoginVO loginVO, String subject) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("managerId",     loginVO.getManagerId());
		claims.put("managerName",   loginVO.getManagerName());
		claims.put("partId",        loginVO.getPartId());
		claims.put("partName",      loginVO.getPartName());
		claims.put("empNo",         loginVO.getEmpNo());
		claims.put("roleId",        loginVO.getRoleId());
		claims.put("roleGubun",     loginVO.getRoleGubun());
		claims.put("managerStatus", loginVO.getManagerStatus());
		claims.put("type",          subject);
		claims.put("managerPic",    loginVO.getManagerPic());
		
		return Jwts.builder()
			.claims(claims)
			.subject(subject)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
			.signWith(getSecretKey())
			.compact();
	}
	
	// ── Refresh Token ──────────────────────────────────────────────────────────
	public String generateRefreshToken(LoginVO loginVO) {
		redis.keyDelete(loginVO.getManagerId());
		
		Date now = new Date();
		Date expireDate = new Date(now.getTime() + TOKEN_REFRESH_TIME);
		
		String refreshToken = Jwts.builder()
			.expiration(expireDate)                     // 0.12.x: setExpiration → expiration()
			.subject(loginVO.getManagerId())
			.signWith(getSecretKey())
			.compact();
		
		try {
			redisTemplate.opsForValue().set(
				loginVO.getManagerId(),
				refreshToken,
				TOKEN_REFRESH_TIME,
				TimeUnit.MILLISECONDS
			);
		} catch (Exception e) {
			// Redis 서버가 없어도 로그인 자체는 계속 진행되게 함(로컬 개발 환경 등). Redis가
			// 없으면 리프레시 토큰이 저장 안 되므로 /actionRefreshToken.do 재발급만 못 쓰게 됨
			log.warn("Redis unavailable, refresh token not cached: {}", e.toString());
		}
		return refreshToken;
	}
	
	public boolean isRefreshToken(String refreshKey) {
		return redis.isKeyNullCheck(refreshKey);
	}
	
	public Boolean isBlackListCheck(String token) {
		return redis.isKeyNullCheck(token);
	}
	
	// ── 로그아웃 (블랙리스트 등록) ──────────────────────────────────────────────
	public Boolean deleteRefreshToken(HttpServletRequest request) {
		String authHeader = EgovStringUtil.isNullToString(request.getHeader("authorization"));
		String accessToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : "";
		String refreshToken = EgovStringUtil.isNullToString(request.getHeader("refreshToken"));
		
		boolean isDeleted = true;
		
		if (!refreshToken.isEmpty()) {
			try {
				String id = getUsernameFromToken(refreshToken);
				redis.keyDelete(id);
			} catch (Exception e) {
				log.error("delete refresh token error: {}", e.toString());
				isDeleted = false;
			}
		}
		
		if (!accessToken.isEmpty()) {
			try {
				Date expiration = getClaimFromToken(accessToken, Claims::getExpiration);
				long diff = expiration.getTime() - System.currentTimeMillis();
				if (diff > 0) {
				    redisTemplate.opsForValue().set(accessToken, "logout", diff, TimeUnit.MILLISECONDS);
				}
			} catch (IllegalArgumentException e) {
				log.debug("delete Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				log.debug("delete JWT Token has expired");
			} catch (MalformedJwtException e) {
				log.debug("delete JWT strings must contain exactly 2 period characters");
			} catch (UnsupportedJwtException e) {
				log.debug("delete not support JWT token.");
			} catch (Exception e) {
				// Redis 서버가 없어도 로그아웃 자체는 계속 진행되게 함(로컬 개발 환경 등)
				log.warn("Redis unavailable, access token blacklist skipped: {}", e.toString());
			}
		}
		return isDeleted;
	}
}