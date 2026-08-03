package egovframework.com.security;

import egovframework.com.cmm.filter.HTMLTagFilter;
import egovframework.com.jwt.JwtAuthenticationEntryPoint;
import egovframework.com.jwt.JwtAuthenticationFilter;
import jakarta.servlet.MultipartConfigElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.MultipartFilter;

import java.util.Arrays;

/**
 * fileName       : SecurityConfig
 * author         : crlee
 * date           : 2023/06/10
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023/06/10        crlee       최초 생성
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	 //Http Methpd : Get 인증예외 List
	private String[] AUTH_GET_WHITELIST = {
			"/mainPage", //메인 화면 리스트 조회
			"/board", // 게시판 목록조회
			"/board/{bbsId}/{nttId}", // 게시물 상세조회
			"/boardFileAtch/{bbsId}", //게시판 파일 첨부가능 여부 조회
			"/schedule/daily", //일별 일정 조회
			"/schedule/week", //주간 일정 조회
			"/schedule/{schdulId}", //일정 상세조회
			"/image", //갤러리 이미지보기
             "/upload/**",
			"/upload/**", //업로드 파일(이미지 등) 정적 서빙
			"/api/backoffice/infra/vendor/product/images/**", //판매사몰 공개 페이지 - 상품 추가 이미지(갤러리) 조회(GET만, 업로드/삭제는 POST/DELETE라 별도 인증 필요)
			"/api/backoffice/infra/vendor/banner/cus/**", //판매사몰 공개 페이지 - 시즌 기획전 배너 조회(GET만)
			"/api/backoffice/infra/vendor/homepage/cus/**", //판매사몰 공개 페이지 - 홈페이지 조회수 증가(GET만)
			"/api/backoffice/board/faq/cus/**", //PartnerLogin(로그인 전) 화면 - FAQ 목록 조회(GET만)
			"/api/backoffice/board/post/cus/**", //판매사몰 공개 페이지 - 홈 화면 공지사항 조회(GET만)
			"/api/backoffice/infra/vendor/product/*.do", //판매사몰 공개 페이지 - 상품 상세 조회(comCode.do, GET만). 같은 접두사의 List.do/detailList.do(POST)와 productCheck/linkedProductCodes(하위 경로)는 이 패턴과 겹치지 않음
			"/api/backoffice/infra/vendor/*.do", //판매사몰 공개 페이지 - 거래처(판매사) 정보 조회(comCode.do, GET만) — 모든 판매사몰 페이지의 헤더/푸터 표시용
            "/api/user/cus/**",
			"/api/backoffice/uat/uia/manager/hello.do",
			"/uat/uia/actionRefreshToken.do",//refreshToken 로그인
			"Alpensia/roomStateInfo.do",
			"/api/interface/Alpensia/roomStateInfo.do", //알펜시아 요금표
			"/api/interface/Alpensia/roomTest.do", //알펜시아 요금표
			"/api/backoffice/infra/vendor/{comCode}.do", //판매사몰(VendorMall) 공개 페이지 - 판매사 정보 조회
			"/api/backoffice/infra/vendor/product/{comCode}.do", //판매사몰 공개 페이지 - 상품 상세 조회
			"/api/backoffice/sys/cmm/cde/combo/{codeId}.do", //판매사몰 공개 페이지 - 공통코드 콤보(카테고리/결제수단 등)
            "/api/backoffice/dashboard/reservationBatchUpdate.do", //Dashboard 예약
            "/api/backoffice/dashboard/regCustomerBatchUpdate.do", //Dashboard 예약
	};
	
	// 인증 예외 List
	private String[] AUTH_WHITELIST = {
			"/",
			"/api/login/**",
			"/api/loginJwt.do",//JWT 로그인
			"/api/backoffice/mail/mailTest.do",
			"/api/loginVendorJwt.do",//JWT 로그인(협력사/파트너)
			"/api/backoffice/order/sms/optionSmsCheck.do",//JWT 로그인
			"/api/backoffice/infra/product/msg/msgResult.do",//JWT 로그인
			"/api/backoffice/bas/holy/holyInsert.do",//JWT 로그인
			"/api/backoffice/sub/equiManage/pic/capture.do", //DID 장비가 캡처 화면을 업로드하는 기계 간 통신 API(로그인 불필요)
			"/api/login.do", //일반 로그인
			"/api/backoffice/order/**", //주문
			"/api/backoffice/res/**", //주문
			"/api/hello.do",
			"/api/user/cus/**",
			"/api/user/*.do", //고객 본인 상세 정보 조회(customerId.do) — 컨트롤러 내부에서 CustomerAuthHelper로 본인 확인
            "/api/backoffice/order/updateOrder.do", //고객 주문 정보
			"/api/backoffice/infra/vendor/product/List.do", //판매사몰 공개 페이지 - 상품 목록 조회
			"/api/backoffice/infra/vendor/product/detailList.do", //판매사몰 공개 페이지 - 예약 옵션 목록 조회
			"/api/backoffice/infra/vendor/user/idSearch.do", //파트너 로그인 - 협력사 아이디 찾기(로그인 전)
			"/api/backoffice/infra/vendor/user/pwdReset.do", //파트너 로그인 - 협력사 비밀번호 재발급(로그인 전, 본인확인 필수)
			"/api/backoffice/infra/vendor/privacyPolicy/cus/**", //판매사몰 공개 페이지 - 개인정보 보호 약관 조회
			"/file", //파일 다운로드
            "/upload/**",
			"/etc/**",//사용자단의 회원약관,회원가입,사용자아이디 중복여부체크 URL허용
			/* swagger*/
			"/v3/api-docs/**",
			"/swagger-resources",
			"/swagger-resources/**",
			"/swagger-ui.html",
			"/swagger-ui/**",
			"/api/interface/Alpensia/**"
			
	};
	private static final String[] ORIGINS_WHITELIST = {
			"http://localhost:3000",
			"http://localhost:3002",
	};
	
	@Bean
	public JwtAuthenticationFilter authenticationTokenFilterBean() throws Exception {
	    return new JwtAuthenticationFilter();
	}
	
	
	@Bean
	protected CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("HEAD","POST","GET","DELETE","PUT","PATCH"));
		configuration.setAllowedOrigins(Arrays.asList(ORIGINS_WHITELIST));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Bean
	public CharacterEncodingFilter characterEncodingFilter() {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		return characterEncodingFilter;
	}
	
	@Bean
	public HTMLTagFilter htmlTagFilter() {
		return new HTMLTagFilter();
	}
	
	//멀티파트 필터 빈	
	@Bean
	public MultipartFilter multipartFilter() {
		return new MultipartFilter();
	}
	
	//서블릿 컨테이너에 멀티파트 구성을 제공하기 위한 설정    
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxRequestSize(DataSize.ofMegabytes(100L));
		factory.setMaxFileSize(DataSize.ofMegabytes(100L));
		return factory.createMultipartConfig();
	}
	
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 방식 변경
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
					// antMatchers -> requestMatchers로 변경
					.requestMatchers("/members/**").hasRole("ADMIN")
					.requestMatchers(AUTH_WHITELIST).permitAll()
					.requestMatchers(HttpMethod.GET, AUTH_GET_WHITELIST).permitAll()
					.anyRequest().authenticated()
				)
				// 필터 추가 로직 (순서 주의)
				.addFilterBefore(characterEncodingFilter(), ChannelProcessingFilter.class)
				.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class)
				// Spring Boot 3에서는 MultipartFilter 처리가 자동화되므로 확인 필요
				.addFilterBefore(multipartFilter(), CsrfFilter.class) 
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(new JwtAuthenticationEntryPoint())
				)
				.build();
	}

}