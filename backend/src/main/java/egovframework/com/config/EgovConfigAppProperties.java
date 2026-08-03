package egovframework.com.config;

import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName : EgovConfigAppProperties.java
 * @Description : Properties 설정
 *
 * @author : 윤주호
 * @since  : 2021. 7. 20
 * @version : 1.0
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일              수정자               수정내용
 *  -------------  ------------   ---------------------
 *   2021. 7. 20    윤주호               최초 생성
 * </pre>
 *
 */

@Configuration
public class EgovConfigAppProperties {
	
	// application.yml에는 Globals.fileStorePath가 없고 Common.filePath만 있음(업로드 경로 키)
	@Value("${Common.filePath:}")
	private String fileStorePath;

	// application.yml에는 Globals.addedOptions가 하위 키(pageUnit 등)만 있고 자체 스칼라 값은 없어 기본값을 둠
	@Value("${Globals.addedOptions:}")
	private String addedOptions;

	// pageUnit/pageSize/posblAtchFileSize는 application.yml에 Globals.addedOptions.* 로 중첩되어 있음
	@Value("${Globals.addedOptions.pageUnit:10}")
	private String pageUnit;
	@Value("${Globals.addedOptions.pageSize:10}")
	private String pageSize;
	@Value("${Globals.addedOptions.posblAtchFileSize:5000}")
	private String posblAtchFileSize;
	
	
	@Bean(destroyMethod = "destroy")
	public EgovPropertyServiceImpl propertiesService() {
		EgovPropertyServiceImpl egovPropertyServiceImpl = new EgovPropertyServiceImpl();

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("pageUnit", pageUnit);
		properties.put("pageSize", pageSize);
		properties.put("Globals.posblAtchFileSize", posblAtchFileSize);
		properties.put("Globals.fileStorePath", fileStorePath);
		properties.put("Globals.addedOptions", addedOptions);

		egovPropertyServiceImpl.setProperties(properties);
		return egovPropertyServiceImpl;
	}
}
