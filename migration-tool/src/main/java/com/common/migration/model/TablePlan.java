package com.common.migration.model;

import java.util.List;

/**
 * 테이블 하나의 이관 계획
 *
 * @param oracleTable    Oracle 테이블명
 * @param targetTable    PostgreSQL 테이블명
 * @param targetExists   PostgreSQL 에 테이블이 있는지
 * @param numRows        Oracle 통계상 행 수 (ALL_TABLES.NUM_ROWS, 정확하지 않을 수 있음)
 * @param columns        이관할 컬럼 (Oracle·PostgreSQL 양쪽에 있는 컬럼)
 * @param keyColumns     비교 키 (Oracle 컬럼명)
 * @param keySource      키 출처 (PK / UNIQUE / 설정 / 없음)
 * @param skippedColumns Oracle 에만 있어 이관하지 않는 컬럼
 * @param problem        이관할 수 없는 사유 (없으면 null)
 */
public record TablePlan(
		String oracleTable,
		String targetTable,
		boolean targetExists,
		Long numRows,
		List<OracleColumn> columns,
		List<String> keyColumns,
		String keySource,
		List<String> skippedColumns,
		String problem) {

	public boolean runnable() {
		return problem == null;
	}
}
