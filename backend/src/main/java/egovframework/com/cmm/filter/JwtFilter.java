package egovframework.com.cmm.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.com.exception.dto.IErrorCode;
import egovframework.com.jwt.EgovJwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtFilter extends OncePerRequestFilter{

	
	private EgovJwtTokenUtil jwtTokenProvider;

	public JwtFilter(EgovJwtTokenUtil jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		String token = jwtTokenProvider.resolveToken(request);
	
		try {
			// StringUtils.hasText를 사용하면 null과 공백을 동시에 체크합니다.
			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
				Authentication auth = jwtTokenProvider.getAuthentication(token);
				
				if (auth != null) {
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}
		} catch (Exception e) {
			// 상세한 에러 로그를 남겨야 원인 파악이 가능합니다.
			log.error("Could not set user authentication in security context", e);
		}
	
		filterChain.doFilter(request, response);
	}
	private void setErrorResponse(HttpServletResponse response,
								  IErrorCode errorCode){
		ObjectMapper objectMapper = new ObjectMapper();
		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ErrorResponse errorResponse = new ErrorResponse(500, errorCode.getMessage());
		try{
			response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
		}catch (IOException e){
			log.error(e.toString());
		}
	}
	@Data
	public static class ErrorResponse{
		private final Integer code;
		private final String message;
	}

}
