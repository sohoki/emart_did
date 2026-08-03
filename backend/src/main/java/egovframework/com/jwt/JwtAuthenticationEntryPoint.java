package egovframework.com.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * fileName       : JwtAuthenticationEntryPoint
 * author         : crlee
 * date           : 2023/06/11
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023/06/11        crlee       최초 생성
 */

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {


	@Override
	public void commence(HttpServletRequest request, 
						HttpServletResponse response, 
						AuthenticationException authException) throws IOException {
	
		
		ResultVO resultVO = new ResultVO();
		System.out.println("🚨 [EntryPoint] 인증 실패 원인: " + authException.getMessage());
		Object exception = request.getAttribute("exception");
		if (exception != null) {
			System.out.println("🔍 [Filter Exception]: " + exception.toString());
		}

		resultVO.setResultCode(ResponseCode.AUTH_ERROR.getCode());
		resultVO.setResultMessage(ResponseCode.AUTH_ERROR.getMessage());
		ObjectMapper mapper = new ObjectMapper();
		
		//Convert object to JSON string
		String jsonInString = mapper.writeValueAsString(resultVO);
		
		
		
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON.toString());
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(jsonInString);
	
	}
}