package egovframework.com.config;

import org.springframework.context.annotation.*;
import org.springframework.jmx.support.RegistrationPolicy;

@Configuration
@Import({
	EgovConfigAppAspect.class,
	EgovConfigAppCommon.class,
	EgovConfigAppDatasource.class,
	EgovConfigAppIdGen.class,
	EgovConfigAppProperties.class,
	EgovConfigAppMapper.class,
	EgovConfigAppTransaction.class,
})
@PropertySources({
	@PropertySource("classpath:/application.yml")
}) 
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class EgovConfigApp {

}
