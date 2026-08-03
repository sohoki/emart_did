package egovframework.com.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName : EgovConfigAppMapper.java
 * @Description : Mapper 설정
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
@PropertySources({
	@PropertySource("classpath:/application.yml")
})
@Slf4j
@MapperScan(value= "com.common.backoffice.**.mapper, egovframework.**.mapper",sqlSessionFactoryRef="sqlSession")
public class EgovConfigAppMapper {


	@Value("${Globals.DbType}")
	private String dbType;

	//@Primary
	@Bean
	@Lazy
	public DefaultLobHandler lobHandler() {
		return new DefaultLobHandler();
	}
	//@Primary
	@Bean(name = "sqlSession")
	public SqlSessionFactoryBean sqlSession(@Qualifier("dataSource") DataSource dataSource) {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
		//mapper config 설정 (dbType에 맞는 설정 파일 사용 — 예: postgresql-config.xml)
		sqlSessionFactoryBean.setConfigLocation(
			pathMatchingResourcePatternResolver
				.getResource("classpath:/mapper/config/" + dbType + "-config.xml"));

		try {

			List<String> mapperLocations = new ArrayList<>();
			mapperLocations.add("classpath:/mapper/"+ dbType + "/**/*.xml");
			for (String mapperLocation : mapperLocations) {
				sqlSessionFactoryBean.setMapperLocations(
						pathMatchingResourcePatternResolver
							.getResources(mapperLocation));
			}
		} catch (IOException e) {
			log.error("IOException");
			// TODO Exception 처리 필요
		} catch (NullPointerException e) {
			log.error("NullPointerException");
			// TODO Exception 처리 필요
		}

		return sqlSessionFactoryBean;
	}
	@Primary
	@Bean(name="egovSqlSessionTemplate")
	public SqlSessionTemplate egovSqlSessionTemplate(@Qualifier("sqlSession") SqlSessionFactory sqlSession) {
		SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSession);
		return sqlSessionTemplate;
	}
}
