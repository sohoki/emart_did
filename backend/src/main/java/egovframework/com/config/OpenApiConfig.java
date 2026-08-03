package egovframework.com.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	
	private static final String API_NAME = "호텔 예약 관리 공통 영역";
	private static final String API_VERSION = "0.9.0";
	private static final String API_DESCRIPTION = "호텔 예약 관리 공통 영역 명세서";

	@Bean
	public OpenAPI api() {
		
			
		return new OpenAPI()
				.info(new Info().title(API_NAME)
				.description(API_DESCRIPTION)
				.version(API_VERSION)
				.contact(new Contact().name("eGovFrame").url("https://www.egovframe.go.kr/").email("egovframesupport@gmail.com"))
				.license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
				.components(new Components()
						.addSecuritySchemes("Authorization", new SecurityScheme()
								.name("Authorization")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER))
				)
                .externalDocs(new ExternalDocumentation()
				.description(API_DESCRIPTION)
				.url("https://github.com/eGovFramework/egovframe-template-simple-backend/wiki"));
	}
}
