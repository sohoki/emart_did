package com.common.migration.model;

/**
 * Oracle 컬럼 정보
 *
 * @param name       컬럼명(대문자)
 * @param dataType   Oracle 타입명
 * @param dataLength 바이트 길이
 * @param kind       읽는 방법
 * @param chunks     원본 바이트를 몇 개의 2000바이트 조각으로 나눠 읽을지 (BYTE_TEXT / CLOB_TEXT 만 사용)
 */
public record OracleColumn(String name, String dataType, int dataLength, ColumnKind kind, int chunks) {

	/** SQL 에서 RAW 한 번에 꺼낼 수 있는 최대 바이트 */
	public static final int RAW_CHUNK = 2000;

	public OracleColumn withChunks(int newChunks) {
		return new OracleColumn(name, dataType, dataLength, kind, Math.max(1, newChunks));
	}

	/** PostgreSQL 컬럼명 (따옴표 없이 만든 테이블이라 소문자) */
	public String targetName() {
		return name.toLowerCase();
	}
}
