package egovframework.com.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @ClassName : EgovConfigAppDatasource.java
 * @Description : DataSource 설정
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
@Slf4j
@Configuration
public class EgovConfigAppDatasource {

	

	@Value("${Globals.postgresql.Url}")
	private String url;

	@Value("${Globals.postgresql.UserName}")
	private String userName;

	@Value("${Globals.postgresql.Password}")
	private String password;

	// local 프로파일에서만 log4jdbc 프록시 드라이버(net.sf.log4jdbc.sql.jdbcapi.DriverSpy)로 오버라이드해서
	// SQL 로그를 여러 줄로 예쁘게 찍는다(resources/log4jdbc.log4j2.properties 참고). prod/server는
	// 해당 프로퍼티가 없으므로 기본값인 순정 postgresql 드라이버를 그대로 사용.
	@Value("${Globals.postgresql.DriverClassName:org.postgresql.Driver}")
	private String driverClassName;

	private DataSource basicDataSource() throws Exception {

		// Globals.postgresql.* 값은 현재 평문 — AES 암호화 적용 전까지는 그대로 사용
		log.info("================================================");
		log.info("jdbc url: {}", url);
		log.info("jdbc driver: {}", driverClassName);
		log.info("================================================");

		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName(driverClassName);
		ds.setJdbcUrl(url);
		ds.setUsername(userName);
		ds.setPassword(password);

		ds.setMinimumIdle(3);
		ds.setMaximumPoolSize(50);
		ds.setConnectionTimeout(30000);
		ds.setIdleTimeout(600000);
		ds.setMaxLifetime(1800000);
		ds.setConnectionTestQuery("SELECT 1");

		return ds;
	}

	/**
	 * @return [DataSource 설정]
	 * @throws Exception
	 */
	@Primary
	@Bean(name = {"dataSource", "egov.dataSource", "egovDataSource"})
	public DataSource dataSource() throws Exception {

		 DataSource ds = basicDataSource();

		// 여기서 1회 커넥션을 열어 메타데이터 로깅
		/*
		try (Connection c = ds.getConnection()) {
			DatabaseMetaData md = c.getMetaData();
			log.info("================================================");
			log.info("JDBC Driver : {} {}", md.getDriverName(), md.getDriverVersion());
			log.info("DB Product  : {} {}", md.getDatabaseProductName(), md.getDatabaseProductVersion());
			log.info("================================================");
		} catch (SQLException e) {
			// 실패 시 로그만 남기고 기동은 진행하고 싶다면 warn 수준으로
			log.warn("DB metadata logging failed during DataSource bean creation", e);
			// 만약 실패 시 기동을 중단하고 싶다면 throw
			// throw e;
		}
		*/
		return ds;

	}
}
