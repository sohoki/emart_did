package com.common.migration.service;

import com.common.migration.config.DataSourceConfig.PostgresHolder;
import com.common.migration.config.MigrationProperties;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 대상 PostgreSQL 쓰기 — 키가 같으면 UPDATE, 없으면 INSERT
 *
 * 대상 테이블에 PK/인덱스가 없어(이관 때 제약조건이 빠짐) 행마다 UPDATE 하면 매번 전체 스캔이 된다.
 * 그래서 테이블 하나를 한 트랜잭션으로 묶어
 *   1) 세션 임시 테이블(mig_stage, 커밋/롤백 시 자동 삭제)에 Oracle 데이터를 배치 INSERT
 *   2) UPDATE 대상 FROM mig_stage — 키가 같고 값이 하나라도 다른 행만 (깨진 한글이 고쳐짐)
 *   3) INSERT 대상 SELECT FROM mig_stage — 키가 대상에 없는 행만
 * 으로 한 번에 처리한다. dry run 이면 같은 처리 후 ROLLBACK 해서 건수만 확인한다.
 */
@Component
public class PostgresWriter {

	private static final String STAGE = "mig_stage";

	private final PostgresHolder postgres;
	private final MigrationProperties props;

	public PostgresWriter(PostgresHolder postgres, MigrationProperties props) {
		this.postgres = postgres;
		this.props = props;
	}

	/** 행 공급자 — 받은 consumer 에 한 행씩 넘김 */
	@FunctionalInterface
	public interface RowSource {
		long forEach(OracleReader.RowHandler consumer) throws Exception;
	}

	/** 결과 건수 */
	public record UpsertCount(long read, long updated, long inserted) {
	}

	private String schema() {
		String s = props.getPostgres().getSchema();
		return s == null || s.isBlank() ? "public" : s;
	}

	private static String q(String identifier) {
		return "\"" + identifier.replace("\"", "\"\"") + "\"";
	}

	private String qualified(String table) {
		return q(schema()) + "." + q(table);
	}

	/** 대상 테이블 컬럼 (컬럼명 → 타입). 테이블이 없으면 빈 Map */
	public Map<String, String> columns(String table) throws SQLException {
		Map<String, String> result = new LinkedHashMap<>();
		String sql = "SELECT column_name, data_type FROM information_schema.columns"
				+ " WHERE table_schema = ? AND table_name = ? ORDER BY ordinal_position";
		try (Connection conn = postgres.dataSource().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, schema());
			ps.setString(2, table);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					result.put(rs.getString(1), rs.getString(2));
				}
			}
		}
		return result;
	}

	/**
	 * UPDATE / INSERT 실행
	 *
	 * @param table      대상 테이블
	 * @param columns    대상 컬럼명(소문자) — 행 값 순서와 같음
	 * @param keyColumns 비교 키 컬럼명(소문자)
	 * @param onProgress 임시 테이블에 넣은 행 수 알림
	 */
	public UpsertCount upsert(String table, List<String> columns, List<String> keyColumns, boolean dryRun,
			RowSource source, java.util.function.LongConsumer onProgress) throws Exception {
		try (Connection conn = postgres.dataSource().getConnection()) {
			conn.setAutoCommit(false);
			try {
				String colList = columns.stream().map(PostgresWriter::q).collect(Collectors.joining(", "));

				// 1) 임시 테이블 — 대상과 같은 컬럼 타입, 커밋/롤백 시 자동 삭제
				try (Statement st = conn.createStatement()) {
					st.execute("DROP TABLE IF EXISTS " + STAGE);
					st.execute("CREATE TEMP TABLE " + STAGE + " ON COMMIT DROP AS SELECT " + colList
							+ " FROM " + qualified(table) + " WITH NO DATA");
				}

				String insertStage = "INSERT INTO " + STAGE + " (" + colList + ") VALUES ("
						+ columns.stream().map(c -> "?").collect(Collectors.joining(", ")) + ")";
				long read;
				try (PreparedStatement ps = conn.prepareStatement(insertStage)) {
					int batchSize = props.getBatchSize();
					long[] pending = { 0, 0 };   // [0] 배치에 쌓인 수, [1] 전체 수
					read = source.forEach(row -> {
						for (int i = 0; i < row.length; i++) {
							bind(ps, i + 1, row[i]);
						}
						ps.addBatch();
						pending[0]++;
						pending[1]++;
						if (pending[0] >= batchSize) {
							ps.executeBatch();
							pending[0] = 0;
							onProgress.accept(pending[1]);
						}
					});
					if (pending[0] > 0) {
						ps.executeBatch();
					}
					onProgress.accept(read);
				}

				long updated = 0;
				long inserted;
				try (Statement st = conn.createStatement()) {
					// 임시 테이블은 자동 통계가 없어 조인 계획이 나빠질 수 있음
					st.execute("ANALYZE " + STAGE);

					// 안전장치 : 원본에 같은 키가 여러 행이면 어느 값으로 UPDATE 될지 정해지지 않으므로 중단(롤백)
					String keyList = keyColumns.stream().map(PostgresWriter::q).collect(Collectors.joining(", "));
					try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM (SELECT " + keyList + " FROM " + STAGE
							+ " GROUP BY " + keyList + " HAVING COUNT(*) > 1) d")) {
						rs.next();
						long duplicated = rs.getLong(1);
						if (duplicated > 0) {
							throw new IllegalStateException("원본에 같은 키(" + String.join(", ", keyColumns) + ")가 중복된 값 "
									+ duplicated + "개 — 키 지정을 확인하세요");
						}
					}

					String keyMatch = keyColumns.stream()
							.map(k -> "t." + q(k) + " = s." + q(k))
							.collect(Collectors.joining(" AND "));

					// 2) UPDATE — 키 외 컬럼 값이 하나라도 다른 행만
					List<String> valueColumns = columns.stream().filter(c -> !keyColumns.contains(c)).toList();
					if (!valueColumns.isEmpty()) {
						String set = valueColumns.stream().map(c -> q(c) + " = s." + q(c)).collect(Collectors.joining(", "));
						// json/xml 처럼 비교 연산자가 없는 타입도 있어 text 로 바꿔 비교 (임시 테이블과 대상 타입이 같아 표현이 같음)
						String targetRow = valueColumns.stream().map(c -> "t." + q(c) + "::text").collect(Collectors.joining(", "));
						String stageRow = valueColumns.stream().map(c -> "s." + q(c) + "::text").collect(Collectors.joining(", "));
						updated = st.executeUpdate("UPDATE " + qualified(table) + " t SET " + set
								+ " FROM " + STAGE + " s WHERE " + keyMatch
								+ " AND ROW(" + targetRow + ") IS DISTINCT FROM ROW(" + stageRow + ")");
					}

					// 3) INSERT — 대상에 키가 없는 행만
					inserted = st.executeUpdate("INSERT INTO " + qualified(table) + " (" + colList + ")"
							+ " SELECT " + columns.stream().map(c -> "s." + q(c)).collect(Collectors.joining(", "))
							+ " FROM " + STAGE + " s WHERE NOT EXISTS (SELECT 1 FROM " + qualified(table) + " t WHERE " + keyMatch + ")");
				}

				if (dryRun) {
					conn.rollback();
				} else {
					conn.commit();
				}
				return new UpsertCount(read, updated, inserted);
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		}
	}

	/** 값 바인딩 — 문자열은 서버가 컬럼 타입으로 변환(stringtype=unspecified) */
	private static void bind(PreparedStatement ps, int idx, Object value) throws SQLException {
		if (value == null) {
			ps.setNull(idx, Types.OTHER);
		} else if (value instanceof byte[] bytes) {
			ps.setBytes(idx, bytes);
		} else {
			ps.setString(idx, value.toString());
		}
	}
}
