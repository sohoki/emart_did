package com.common.migration.model;

import java.time.LocalDateTime;
import java.util.List;

/** 이관 작업 상태 (조회용 스냅샷) */
public record JobStatus(
		boolean running,
		boolean dryRun,
		LocalDateTime startedAt,
		LocalDateTime finishedAt,
		String currentTable,
		List<TableResult> results) {
}
