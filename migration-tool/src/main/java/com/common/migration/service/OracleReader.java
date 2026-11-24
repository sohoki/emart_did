package com.common.migration.service;

import com.common.migration.config.DataSourceConfig.OracleHolder;
import com.common.migration.config.MigrationProperties;
import com.common.migration.model.ColumnKind;
import com.common.migration.model.OracleColumn;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 원본 Oracle 읽기
 *
 * Oracle DB 문자셋이 US7ASCII 인데 한글이 MS949 바이트 그대로 들어 있어서, JDBC 로 문자열을 그냥 읽으면
 * 드라이버가 0x80 이상 바이트를 깨뜨린다. 그래서 문자 컬럼은 UTL_RAW.CAST_TO_RAW 로 원본 바이트를
 * 꺼내 Java 에서 MS949 로 디코딩한다.
 * - SQL 의 RAW 는 최대 2000 바이트라 VARCHAR2(4000)·CLOB 은 2000 바이트 조각으로 나눠 꺼낸 뒤 합친다.
 */
@Component
public class OracleReader {

	private final OracleHolder oracle;
	private final MigrationProperties props;
	private final Charset charset;

	public OracleReader(OracleHolder oracle, MigrationProperties props) {
		this.oracle = oracle;
		this.props = props;
		this.charset = Charset.forName(props.getSourceCharset());
	}

	/** 한 행 처리기 */
	@FunctionalInterface
	public interface RowHandler {
		void handle(Object[] row) throws Exception;
	}

	private String owner() {
		return props.getOracle().getSchema().toUpperCase();
	}

	private static String q(String identifier) {
		return "\"" + identifier.replace("\"", "\"\"") + "\"";
	}

	/** 테이블 목록과 통계상 행 수 (휴지통 BIN$ 제외) */
	public Map<String, Long> tables() throws SQLException {
		Map<String, Long> result = new LinkedHashMap<>();
		String sql = "SELECT TABLE_NAME, NUM_ROWS FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME NOT LIKE 'BIN$%' ORDER BY TABLE_NAME";
		try (Connection conn = oracle.dataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, owner());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					BigDecimal rows = rs.getBigDecimal(2);
					result.put(rs.getString(1), rows == null ? null : rows.longValue());
				}
			}
		}
		return result;
	}

	public List<OracleColumn> columns(String table) throws SQLException {
		List<OracleColumn> result = new ArrayList<>();
		String sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH FROM ALL_TAB_COLUMNS WHERE OWNER = ? AND TABLE_NAME = ? ORDER BY COLUMN_ID";
		try (Connection conn = oracle.dataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, owner());
			ps.setString(2, table);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					// TIMESTAMP(6) 처럼 괄호가 붙은 타입명 정리
					String type = rs.getString(2).replaceAll("\\(.*\\)", "").trim();
					int length = rs.getInt(3);
					ColumnKind kind = ColumnKind.of(type);
					int chunks = kind == ColumnKind.BYTE_TEXT ? (int) Math.ceil(length / (double) OracleColumn.RAW_CHUNK) : 1;
					result.add(new OracleColumn(rs.getString(1), type, length, kind, Math.max(1, chunks)));
				}
			}
		}
		return result;
	}

	/**
	 * 비교 키 — PK, 없으면 첫 번째 UNIQUE 제약
	 *
	 * @return [0] = 키 출처(PK/UNIQUE), [1..] = 컬럼명. 키가 없으면 빈 목록
	 */
	public List<String> keyColumns(String table) throws SQLException {
		String sql = "SELECT C.CONSTRAINT_TYPE, C.CONSTRAINT_NAME, CC.COLUMN_NAME"
				+ " FROM ALL_CONSTRAINTS C"
				+ " JOIN ALL_CONS_COLUMNS CC ON CC.OWNER = C.OWNER AND CC.CONSTRAINT_NAME = C.CONSTRAINT_NAME AND CC.TABLE_NAME = C.TABLE_NAME"
				+ " WHERE C.OWNER = ? AND C.TABLE_NAME = ? AND C.CONSTRAINT_TYPE IN ('P', 'U') AND C.STATUS = 'ENABLED'"
				+ " ORDER BY DECODE(C.CONSTRAINT_TYPE, 'P', 0, 1), C.CONSTRAINT_NAME, CC.POSITION";
		List<String> result = new ArrayList<>();
		try (Connection conn = oracle.dataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, owner());
			ps.setString(2, table);
			try (ResultSet rs = ps.executeQuery()) {
				String firstConstraint = null;
				while (rs.next()) {
					if (firstConstraint == null) {
						firstConstraint = rs.getString(2);
						result.add("P".equals(rs.getString(1)) ? "PK" : "UNIQUE");
					}
					// 첫 번째 제약조건의 컬럼만 사용
					if (!firstConstraint.equals(rs.getString(2))) {
						break;
					}
					result.add(rs.getString(3));
				}
			}
		}
		return result;
	}

	/** CLOB 컬럼의 가장 긴 값이 2000 바이트 조각 몇 개인지 */
	public int clobChunks(String table, String column) throws SQLException {
		String sql = "SELECT NVL(MAX(DBMS_LOB.GETLENGTH(" + q(column) + ")), 0) FROM " + q(owner()) + "." + q(table);
		try (Connection conn = oracle.dataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			rs.next();
			long maxLength = rs.getLong(1);
			return (int) Math.max(1, Math.ceil(maxLength / (double) OracleColumn.RAW_CHUNK));
		}
	}

	/** 원본 바이트를 꺼내는 SELECT 문 */
	private String buildSelect(String table, List<OracleColumn> columns, Integer limit) {
		List<String> exprs = new ArrayList<>();
		for (OracleColumn col : columns) {
			String c = q(col.name());
			switch (col.kind()) {
				case BYTE_TEXT -> {
					if (col.chunks() == 1) {
						exprs.add("UTL_RAW.CAST_TO_RAW(" + c + ")");
					} else {
						for (int k = 0; k < col.chunks(); k++) {
							exprs.add("UTL_RAW.CAST_TO_RAW(SUBSTRB(" + c + ", " + (1 + k * OracleColumn.RAW_CHUNK) + ", " + OracleColumn.RAW_CHUNK + "))");
						}
					}
				}
				case CLOB_TEXT -> {
					for (int k = 0; k < col.chunks(); k++) {
						exprs.add("UTL_RAW.CAST_TO_RAW(DBMS_LOB.SUBSTR(" + c + ", " + OracleColumn.RAW_CHUNK + ", " + (1 + k * OracleColumn.RAW_CHUNK) + "))");
					}
				}
				default -> exprs.add(c);
			}
		}
		String sql = "SELECT " + String.join(", ", exprs) + " FROM " + q(owner()) + "." + q(table);
		if (limit != null) {
			sql = "SELECT * FROM (" + sql + ") WHERE ROWNUM <= " + limit;
		}
		return sql;
	}

	/**
	 * 테이블을 한 행씩 읽어 처리기에 넘김
	 * - 행 값은 columns 순서대로 String / byte[] / null
	 *
	 * @param onWarning 디코딩 경고 (컬럼명, 내용)
	 * @return 읽은 행 수
	 */
	public long stream(String table, List<OracleColumn> columns, Integer limit,
			BiConsumer<String, String> onWarning, RowHandler handler) throws Exception {
		String sql = buildSelect(table, columns, limit);
		long count = 0;
		try (Connection conn = oracle.dataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setFetchSize(props.getFetchSize());
			try (ResultSet rs = ps.executeQuery()) {
				Object[] row = new Object[columns.size()];
				while (rs.next()) {
					int idx = 1;
					for (int i = 0; i < columns.size(); i++) {
						OracleColumn col = columns.get(i);
						switch (col.kind()) {
							case BYTE_TEXT, CLOB_TEXT -> {
								byte[] bytes = readChunks(rs, idx, col.chunks());
								idx += col.chunks();
								row[i] = bytes == null ? null : decode(bytes, col.name(), onWarning);
							}
							case NUMBER -> {
								BigDecimal n = rs.getBigDecimal(idx++);
								row[i] = n == null ? null : n.toPlainString();
							}
							case DATE -> {
								Timestamp ts = rs.getTimestamp(idx++);
								row[i] = ts == null ? null : formatTimestamp(ts);
							}
							case BINARY -> row[i] = rs.getBytes(idx++);
							default -> row[i] = rs.getString(idx++);
						}
					}
					handler.handle(row);
					count++;
				}
			}
		}
		return count;
	}

	/** 2000 바이트 조각들을 합침 (모두 NULL 이면 NULL) */
	private static byte[] readChunks(ResultSet rs, int startIdx, int chunks) throws SQLException {
		if (chunks == 1) {
			return rs.getBytes(startIdx);
		}
		ByteArrayOutputStream out = null;
		for (int k = 0; k < chunks; k++) {
			byte[] part = rs.getBytes(startIdx + k);
			if (part != null) {
				if (out == null) {
					out = new ByteArrayOutputStream();
				}
				out.writeBytes(part);
			}
		}
		return out == null ? null : out.toByteArray();
	}

	/**
	 * 원본 바이트 → 문자열 (MS949)
	 * - 정상 디코딩이 안 되면: 끝 1바이트를 빼고 다시 시도(컬럼 길이 때문에 한글 마지막 바이트가 잘린 경우)
	 *   그래도 안 되면 대체문자(�)로 디코딩하고 경고를 남김
	 * - PostgreSQL 텍스트에 넣을 수 없는 NUL(0x00) 문자는 제거
	 */
	String decode(byte[] bytes, String column, BiConsumer<String, String> onWarning) {
		String text;
		try {
			text = strictDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			text = null;
			if (bytes.length > 1) {
				try {
					text = strictDecoder().decode(ByteBuffer.wrap(Arrays.copyOf(bytes, bytes.length - 1))).toString();
					if (onWarning != null) {
						onWarning.accept(column, "끝 바이트 잘림 → 마지막 1바이트 제외: " + abbreviate(text));
					}
				} catch (CharacterCodingException ignore) {
					text = null;
				}
			}
			if (text == null) {
				text = new String(bytes, charset);
				if (onWarning != null) {
					onWarning.accept(column, "디코딩 불가 문자 포함(�로 대체): " + abbreviate(text));
				}
			}
		}
		return text.indexOf('\u0000') >= 0 ? text.replace("\u0000", "") : text;
	}

	private CharsetDecoder strictDecoder() {
		return charset.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
	}

	private static String abbreviate(String text) {
		return text.length() > 60 ? text.substring(0, 60) + "…" : text;
	}

	/** 시간 부분이 없으면 초까지, 있으면 소수점 포함 (PostgreSQL 이 date/timestamp/문자 컬럼 모두 받는 형식) */
	private static String formatTimestamp(Timestamp ts) {
		String s = ts.toString();   // yyyy-MM-dd HH:mm:ss.fffffffff
		return ts.getNanos() == 0 ? s.substring(0, 19) : s;
	}
}
