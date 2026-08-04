package com.common.backoffice.bas.mark.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.mark.mapper.BookMarksInfoManageMapper;
import com.common.backoffice.bas.mark.models.dto.BookMarksInfoReqDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 즐겨찾기(TB_BOOKMARKS) 관리 서비스.
 * basic/backend를 참조해서 이식하되, Redis 캐싱은 제외함(현재 로컬 환경에 Redis가 없어
 * 다른 서비스들도 전부 DB 직접 조회로 폴백 중 — 신규 코드에 새 Redis 의존을 추가하지 않음).
 */
@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BookMarksInfoManageService {

	private final BookMarksInfoManageMapper bookMarksInfoManageMapper;

	public List<Map<String, Object>> selectBookMarksInfoList(Map<String, Object> params) {
		return bookMarksInfoManageMapper.selectBookMarksInfoList(params);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int insertBookMarksInfo(List<BookMarksInfoReqDto> list) {
		return bookMarksInfoManageMapper.insertBookMarksInfo(list);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int updateBookMarksOrder(String userId, List<BookMarksInfoReqDto> list) {
		return bookMarksInfoManageMapper.updateBookMarksOrder(userId, list);
	}

	@Transactional(value = "txManager", readOnly = false)
	public int deleteBookMarksInfo(List<BookMarksInfoReqDto> list) {
		return bookMarksInfoManageMapper.deleteBookMarksInfo(list);
	}
}
