package egovframework.com.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.com.config.HtmlCharacterEscapes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * fileName       : WebMvcConfig
 * author         : crlee
 * date           : 2023/07/13
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023/07/13        crlee       최초 생성
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final ObjectMapper objectMapper;

	// 업로드 파일 실제 저장 경로 (프로파일별 application.yml 값) — 정적 리소스로 서빙하기 위해 매핑.
	// application.yml에는 webinfPath.url이 아니라 Common.filePath 키로 정의되어 있음
	// (CenterInfoManageController 등 다른 곳에서도 동일한 키를 씀)
	@Value("${Common.filePath}")
	private String uploadPath;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new CustomAuthenticationPrincipalResolver());
    }

    // 업로드된 파일(이미지 등)을 /upload/** 경로로 조회할 수 있도록 정적 리소스 핸들러 등록.
    // 컨테이너에 별도 웹서버(nginx 등)가 없어 Spring이 직접 서빙해야 한다.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = (uploadPath.endsWith("/") || uploadPath.endsWith("\\"))
                ? uploadPath
                : uploadPath + "/";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + location);
    }

    @Bean
    public HttpMessageConverter<?> htmlEscapingConverter() {
        ObjectMapper copy = objectMapper.copy();
        copy.getFactory().setCharacterEscapes(new HtmlCharacterEscapes());
        return new MappingJackson2HttpMessageConverter(copy);
    }
    
}