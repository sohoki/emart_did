package com.common.migration.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/** 원본 Oracle / 대상 PostgreSQL DataSource */
@Configuration
public class DataSourceConfig {

	@Bean(name = "oracleDataSource", destroyMethod = "close")
	public DataSource oracleDataSource(MigrationProperties props) {
		HikariDataSource ds = new HikariDataSource();
		ds.setPoolName("oracle");
		ds.setDriverClassName("oracle.jdbc.OracleDriver");
		ds.setJdbcUrl(props.getOracle().getUrl());
		ds.setUsername(props.getOracle().getUsername());
		ds.setPassword(props.getOracle().getPassword());
		ds.setMaximumPoolSize(3);
		ds.setReadOnly(true);
		// 기동 시 접속 실패해도 앱은 뜨게 함 (화면에서 오류 확인)
		ds.setInitializationFailTimeout(-1);
		return ds;
	}

	@Bean(name = "postgresDataSource", destroyMethod = "close")
	public DataSource postgresDataSource(MigrationProperties props) {
		HikariDataSource ds = new HikariDataSource();
		ds.setPoolName("postgres");
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setJdbcUrl(props.getPostgres().getUrl());
		ds.setUsername(props.getPostgres().getUsername());
		ds.setPassword(props.getPostgres().getPassword());
		ds.setMaximumPoolSize(3);
		// 문자열 파라미터를 서버가 컬럼 타입(숫자/날짜 등)으로 변환하도록 함
		ds.addDataSourceProperty("stringtype", "unspecified");
		// 배치 INSERT 를 다건 VALUES 로 묶어 전송 (대용량 테이블 속도)
		ds.addDataSourceProperty("reWriteBatchedInserts", "true");
		ds.setInitializationFailTimeout(-1);
		return ds;
	}

	@Bean
	public OracleHolder oracleHolder(@Qualifier("oracleDataSource") DataSource ds) {
		return new OracleHolder(ds);
	}

	@Bean
	public PostgresHolder postgresHolder(@Qualifier("postgresDataSource") DataSource ds) {
		return new PostgresHolder(ds);
	}

	/** 같은 타입(DataSource) 두 개를 주입받을 때 헷갈리지 않도록 감싼 클래스 */
	public record OracleHolder(DataSource dataSource) {
	}

	public record PostgresHolder(DataSource dataSource) {
	}
}
