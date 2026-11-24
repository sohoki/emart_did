package com.common.migration.model;

/** Oracle 컬럼을 읽는 방법 */
public enum ColumnKind {
	/** VARCHAR2 / CHAR — US7ASCII DB 라 원본 바이트(UTL_RAW.CAST_TO_RAW)로 꺼내 MS949 디코딩 */
	BYTE_TEXT,
	/** CLOB — 2000 바이트 조각으로 원본 바이트를 꺼내 MS949 디코딩 */
	CLOB_TEXT,
	/** NCHAR / NVARCHAR2 / NCLOB — 유니코드(AL16UTF16)라 그대로 읽음 */
	NATIONAL_TEXT,
	NUMBER,
	DATE,
	/** BLOB / RAW / LONG RAW */
	BINARY,
	/** 그 밖의 타입(LONG 등) — 문자열로 그대로 읽음 */
	OTHER;

	public static ColumnKind of(String oracleType) {
		String t = oracleType.toUpperCase();
		if (t.equals("VARCHAR2") || t.equals("CHAR") || t.equals("VARCHAR")) {
			return BYTE_TEXT;
		}
		if (t.equals("CLOB")) {
			return CLOB_TEXT;
		}
		if (t.equals("NCHAR") || t.equals("NVARCHAR2") || t.equals("NCLOB")) {
			return NATIONAL_TEXT;
		}
		if (t.equals("NUMBER") || t.equals("FLOAT") || t.startsWith("BINARY_")) {
			return NUMBER;
		}
		if (t.equals("DATE") || t.startsWith("TIMESTAMP")) {
			return DATE;
		}
		if (t.equals("BLOB") || t.equals("RAW") || t.equals("LONG RAW")) {
			return BINARY;
		}
		return OTHER;
	}
}
