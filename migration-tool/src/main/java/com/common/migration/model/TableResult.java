package com.common.migration.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 테이블 하나의 실행 결과 (실행 중에도 상태 조회로 읽히므로 필드는 volatile) */
@Getter
public class TableResult {

	public enum Status { WAIT, RUNNING, DONE, SKIP, ERROR, CANCELED }

	private final String oracleTable;
	private final String targetTable;
	private volatile Status status = Status.WAIT;
	/** Oracle 에서 읽은 행 수 */
	private volatile long readRows;
	/** 키가 같고 값이 달라 UPDATE 한 행 수 (깨진 한글이 고쳐진 행 포함) */
	private volatile long updatedRows;
	/** 키가 없어 INSERT 한 행 수 */
	private volatile long insertedRows;
	/** MS949 로 정상 디코딩되지 않은 값 수 (끝 바이트 잘림 등) */
	private volatile long decodeWarnings;
	private final List<String> warningSamples = Collections.synchronizedList(new ArrayList<>());
	private volatile String message;
	private volatile long elapsedMs;

	public TableResult(String oracleTable, String targetTable) {
		this.oracleTable = oracleTable;
		this.targetTable = targetTable;
	}

	public void start() {
		status = Status.RUNNING;
	}

	public void setReadRows(long readRows) {
		this.readRows = readRows;
	}

	public void addDecodeWarning(String sample) {
		decodeWarnings++;
		if (warningSamples.size() < 5) {
			warningSamples.add(sample);
		}
	}

	public void finish(Status status, long updated, long inserted, String message, long elapsedMs) {
		this.updatedRows = updated;
		this.insertedRows = inserted;
		this.message = message;
		this.elapsedMs = elapsedMs;
		this.status = status;
	}

	public void skip(String message) {
		this.message = message;
		this.status = Status.SKIP;
	}
}
