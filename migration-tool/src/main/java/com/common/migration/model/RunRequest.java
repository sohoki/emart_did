package com.common.migration.model;

import java.util.List;

/**
 * 이관 실행 요청
 *
 * @param tables Oracle 테이블명 목록 (비어 있으면 전체)
 * @param dryRun true 면 모든 처리 후 ROLLBACK (변경될 건수만 확인)
 */
public record RunRequest(List<String> tables, boolean dryRun) {
}
