package com.common.migration.service;

import com.common.migration.config.MigrationProperties;
import com.common.migration.model.ColumnKind;
import com.common.migration.model.JobStatus;
import com.common.migration.model.OracleColumn;
import com.common.migration.model.RunRequest;
import com.common.migration.model.TablePlan;
import com.common.migration.model.TableResult;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 이관 작업 — 테이블 계획 / 미리보기 / 실행(백그라운드 1건) / 상태 / 취소 */
@Slf4j
@Service
public class MigrationService {

	private final OracleReader reader;
	private final PostgresWriter writer;
	private final MigrationProperties props;
	private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "migration");
		t.setDaemon(true);
		return t;
	});

	// 현재(또는 마지막) 작업 상태
	private volatile boolean running;
	private volatile boolean cancelRequested;
	private volatile boolean dryRun;
	private volatile LocalDateTime startedAt;
	private volatile LocalDateTime finishedAt;
	private volatile String currentTable;
	private volatile List<TableResult> results = List.of();

	public MigrationService(OracleReader reader, PostgresWriter writer, MigrationProperties props) {
		this.reader = reader;
		this.writer = writer;
		this.props = props;
	}

	@PreDestroy
	public void shutdown() {
		cancelRequested = true;
		executor.shutdownNow();
	}

	// ------------------------------------------------------------------
	// 계획
	// ------------------------------------------------------------------

	/** 전체 테이블 계획 (제외 테이블 빼고) */
	public List<TablePlan> plans() throws Exception {
		List<TablePlan> list = new ArrayList<>();
		for (Map.Entry<String, Long> e : reader.tables().entrySet()) {
			if (isExcluded(e.getKey())) {
				continue;
			}
			list.add(plan(e.getKey(), e.getValue()));
		}
		return list;
	}

	private boolean isExcluded(String table) {
		return props.getExcludeTables().stream().anyMatch(t -> t.equalsIgnoreCase(table));
	}

	public TablePlan plan(String oracleTable, Long numRows) throws Exception {
		String target = props.targetTableOf(oracleTable);
		Map<String, String> targetColumns = writer.columns(target);
		boolean targetExists = !targetColumns.isEmpty();

		List<OracleColumn> oracleColumns = reader.columns(oracleTable);
		List<OracleColumn> common = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for (OracleColumn col : oracleColumns) {
			if (targetColumns.containsKey(col.targetName())) {
				common.add(col);
			} else {
				skipped.add(col.name());
			}
		}

		// 비교 키 : 설정 > Oracle PK > Oracle UNIQUE
		List<String> keys;
		String keySource;
		List<String> configured = props.getKeyColumns().get(oracleTable);
		if (configured != null && !configured.isEmpty()) {
			keys = configured.stream().map(String::toUpperCase).toList();
			keySource = "설정";
		} else {
			List<String> found = reader.keyColumns(oracleTable);
			keySource = found.isEmpty() ? "없음" : found.get(0);
			keys = found.isEmpty() ? List.of() : found.subList(1, found.size());
		}

		String problem = null;
		if (!targetExists) {
			problem = "PostgreSQL 에 대상 테이블 없음 (" + target + ")";
		} else if (common.isEmpty()) {
			problem = "양쪽에 공통 컬럼 없음";
		} else if (keys.isEmpty()) {
			problem = "비교 키 없음 (Oracle PK/UNIQUE 없음) — application.yml migration.key-columns 에 지정 필요";
		} else {
			List<String> commonNames = common.stream().map(OracleColumn::name).toList();
			List<String> missingKeys = keys.stream().filter(k -> !commonNames.contains(k)).toList();
			if (!missingKeys.isEmpty()) {
				problem = "키 컬럼이 대상 테이블에 없음: " + missingKeys;
			}
		}
		return new TablePlan(oracleTable, target, targetExists, numRows, common, keys, keySource, skipped, problem);
	}

	// ------------------------------------------------------------------
	// 미리보기 — Oracle 값을 MS949 로 디코딩한 결과 (PostgreSQL 은 건드리지 않음)
	// ------------------------------------------------------------------

	public Map<String, Object> preview(String oracleTable, int limit) throws Exception {
		List<OracleColumn> columns = withClobChunks(oracleTable, reader.columns(oracleTable));
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		reader.stream(oracleTable, columns, Math.max(1, Math.min(limit, 500)),
				(col, msg) -> warnings.add(col + ": " + msg),
				row -> {
					Map<String, Object> r = new LinkedHashMap<>();
					for (int i = 0; i < columns.size(); i++) {
						Object v = row[i];
						r.put(columns.get(i).name(), v instanceof byte[] b ? "(binary " + b.length + " bytes)" : v);
					}
					rows.add(r);
				});
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("table", oracleTable);
		result.put("columns", columns.stream().map(c -> c.name() + " " + c.dataType()).toList());
		result.put("rows", rows);
		result.put("warnings", warnings);
		return result;
	}

	/** CLOB 은 가장 긴 값 기준으로 조각 수를 정함 */
	private List<OracleColumn> withClobChunks(String oracleTable, List<OracleColumn> columns) throws Exception {
		List<OracleColumn> result = new ArrayList<>();
		for (OracleColumn col : columns) {
			result.add(col.kind() == ColumnKind.CLOB_TEXT ? col.withChunks(reader.clobChunks(oracleTable, col.name())) : col);
		}
		return result;
	}

	// ------------------------------------------------------------------
	// 실행
	// ------------------------------------------------------------------

	public synchronized JobStatus run(RunRequest request) throws Exception {
		if (running) {
			throw new IllegalStateException("이미 실행 중인 작업이 있습니다.");
		}
		Map<String, Long> allTables = reader.tables();
		List<String> tables = request.tables() == null || request.tables().isEmpty()
				? allTables.keySet().stream().filter(t -> !isExcluded(t)).toList()
				: request.tables().stream().map(String::toUpperCase).toList();

		List<TablePlan> plans = new ArrayList<>();
		List<TableResult> newResults = new ArrayList<>();
		for (String table : tables) {
			if (!allTables.containsKey(table)) {
				TableResult r = new TableResult(table, "-");
				r.skip("Oracle 에 테이블 없음");
				newResults.add(r);
				continue;
			}
			TablePlan plan = plan(table, allTables.get(table));
			TableResult r = new TableResult(table, plan.targetTable());
			if (!plan.runnable()) {
				r.skip(plan.problem());
			} else {
				plans.add(plan);
			}
			newResults.add(r);
		}

		results = newResults;
		dryRun = request.dryRun();
		cancelRequested = false;
		running = true;
		startedAt = LocalDateTime.now();
		finishedAt = null;
		currentTable = null;

		executor.submit(() -> runPlans(plans, newResults, request.dryRun()));
		return status();
	}

	private void runPlans(List<TablePlan> plans, List<TableResult> resultList, boolean dry) {
		try {
			for (TablePlan plan : plans) {
				TableResult result = resultList.stream()
						.filter(r -> r.getOracleTable().equals(plan.oracleTable()))
						.findFirst().orElseThrow();
				if (cancelRequested) {
					result.finish(TableResult.Status.CANCELED, 0, 0, "취소됨 (실행 전)", 0);
					continue;
				}
				currentTable = plan.oracleTable();
				runTable(plan, result, dry);
			}
		} finally {
			currentTable = null;
			finishedAt = LocalDateTime.now();
			running = false;
		}
	}

	private void runTable(TablePlan plan, TableResult result, boolean dry) {
		long start = System.currentTimeMillis();
		result.start();
		try {
			List<OracleColumn> columns = withClobChunks(plan.oracleTable(), plan.columns());
			List<String> targetColumns = columns.stream().map(OracleColumn::targetName).toList();
			List<String> targetKeys = plan.keyColumns().stream().map(String::toLowerCase).toList();

			PostgresWriter.UpsertCount count = writer.upsert(plan.targetTable(), targetColumns, targetKeys, dry,
					consumer -> reader.stream(plan.oracleTable(), columns, null,
							(col, msg) -> result.addDecodeWarning(col + ": " + msg),
							row -> {
								if (cancelRequested) {
									throw new CancellationException();
								}
								consumer.handle(row);
							}),
					result::setReadRows);

			result.setReadRows(count.read());
			String msg = (dry ? "[점검] 롤백함 — " : "") + "UPDATE " + count.updated() + "건 / INSERT " + count.inserted() + "건";
			if (!plan.skippedColumns().isEmpty()) {
				msg += " (대상에 없어 제외한 컬럼: " + String.join(", ", plan.skippedColumns()) + ")";
			}
			result.finish(TableResult.Status.DONE, count.updated(), count.inserted(), msg, System.currentTimeMillis() - start);
			log.info("{} → {} : {}", plan.oracleTable(), plan.targetTable(), msg);
		} catch (CancellationException e) {
			result.finish(TableResult.Status.CANCELED, 0, 0, "취소됨 — 이 테이블은 롤백함", System.currentTimeMillis() - start);
		} catch (Exception e) {
			Throwable root = e;
			while (root.getCause() != null && root.getCause() != root) {
				root = root.getCause();
			}
			log.error("{} 이관 실패", plan.oracleTable(), e);
			result.finish(TableResult.Status.ERROR, 0, 0, "롤백함 — " + root.getMessage(), System.currentTimeMillis() - start);
		}
	}

	public JobStatus status() {
		return new JobStatus(running, dryRun, startedAt, finishedAt, currentTable, results);
	}

	public void cancel() {
		cancelRequested = true;
	}

	/** 화면 표시용 접속 정보 (비밀번호 제외) */
	public Map<String, Object> config() {
		Map<String, Object> c = new LinkedHashMap<>();
		c.put("oracleUrl", props.getOracle().getUrl());
		c.put("oracleUser", props.getOracle().getUsername());
		c.put("oracleSchema", props.getOracle().getSchema());
		c.put("oraclePasswordSet", props.getOracle().getPassword() != null && !props.getOracle().getPassword().isBlank());
		c.put("postgresUrl", props.getPostgres().getUrl());
		c.put("postgresUser", props.getPostgres().getUsername());
		c.put("postgresSchema", props.getPostgres().getSchema());
		c.put("postgresPasswordSet", props.getPostgres().getPassword() != null && !props.getPostgres().getPassword().isBlank());
		c.put("sourceCharset", props.getSourceCharset());
		c.put("tableMap", props.getTableMap());
		c.put("keyColumns", props.getKeyColumns());
		c.put("excludeTables", props.getExcludeTables());
		return c;
	}
}
