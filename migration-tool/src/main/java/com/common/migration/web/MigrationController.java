package com.common.migration.web;

import com.common.migration.model.JobStatus;
import com.common.migration.model.RunRequest;
import com.common.migration.model.TablePlan;
import com.common.migration.service.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 이관 API (화면: http://localhost:8099)
 * - GET  /api/migration/config            접속 정보(비밀번호 제외)
 * - GET  /api/migration/tables            테이블별 이관 계획 (대상 테이블/키/제외 컬럼/문제)
 * - GET  /api/migration/preview?table=X   Oracle 값을 MS949 로 디코딩해서 보기 (PostgreSQL 변경 없음)
 * - POST /api/migration/run               {"tables":["TB_CENTERINFO"], "dryRun":true} — tables 가 비면 전체
 * - GET  /api/migration/status            진행 상태
 * - POST /api/migration/cancel            취소 (진행 중인 테이블은 롤백)
 */
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

	private final MigrationService service;

	@GetMapping("/config")
	public Map<String, Object> config() {
		return service.config();
	}

	@GetMapping("/tables")
	public List<TablePlan> tables() throws Exception {
		return service.plans();
	}

	@GetMapping("/preview")
	public Map<String, Object> preview(@RequestParam String table,
			@RequestParam(defaultValue = "20") int limit) throws Exception {
		return service.preview(table.toUpperCase(), limit);
	}

	@PostMapping("/run")
	public JobStatus run(@RequestBody RunRequest request) throws Exception {
		return service.run(request);
	}

	@GetMapping("/status")
	public JobStatus status() {
		return service.status();
	}

	@PostMapping("/cancel")
	public JobStatus cancel() {
		service.cancel();
		return service.status();
	}

	/** DB 접속 실패 등은 화면에 메세지로 보여줌 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handle(Exception e) {
		Throwable root = e;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		HttpStatus status = e instanceof IllegalStateException ? HttpStatus.CONFLICT : HttpStatus.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(status).body(Map.of("error", String.valueOf(root.getMessage())));
	}
}
