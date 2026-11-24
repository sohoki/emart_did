package com.common.migration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** application.yml 의 migration.* 설정 */
@Getter
@Setter
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

	private Db oracle = new Db();
	private Db postgres = new Db();

	/** Oracle 에 저장된 한글 바이트 문자셋 */
	private String sourceCharset = "MS949";
	private int batchSize = 1000;
	private int fetchSize = 1000;

	/** Oracle 테이블명(대문자) → PostgreSQL 테이블명 */
	private Map<String, String> tableMap = new LinkedHashMap<>();
	/** Oracle 에 PK 가 없는 테이블의 비교 키 (Oracle 테이블명 → Oracle 컬럼명 목록) */
	private Map<String, List<String>> keyColumns = new LinkedHashMap<>();
	/** 이관하지 않을 Oracle 테이블 */
	private List<String> excludeTables = new ArrayList<>();

	@Getter
	@Setter
	public static class Db {
		private String url;
		private String username;
		private String password;
		private String schema;
	}

	/** Oracle 테이블명에 대응하는 PostgreSQL 테이블명 (지정이 없으면 소문자) */
	public String targetTableOf(String oracleTable) {
		String mapped = tableMap.get(oracleTable);
		return mapped != null ? mapped.toLowerCase() : oracleTable.toLowerCase();
	}
}
