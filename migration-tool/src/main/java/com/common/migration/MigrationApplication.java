package com.common.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Oracle(EMARTCMS) → PostgreSQL 데이터 이관 도구
 * - 브라우저에서 http://localhost:8099 접속 후 테이블 선택 → 점검(dry run) → 실제 이관
 * - 한글은 Oracle(US7ASCII)에 저장된 원본 바이트를 그대로 꺼내 MS949 로 디코딩한다
 * - 대상 테이블에 같은 키가 있으면 UPDATE(값이 다른 행만), 없으면 INSERT
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MigrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MigrationApplication.class, args);
	}
}
