package com.common.backoffice.sts.mhs.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 문화센터(MHS) 룸 단말 화면 — 레거시 주소(/backoffice/sub/roomManage/preView.do) 호환.
 * 기존 키오스크/관리자 팝업이 이 주소를 그대로 쓰므로 레거시 preView.jsp 를 정적 HTML
 * (classpath:/preview/mhsPreView.html)로 이관해 내려준다. 데이터는 페이지 JS 가
 * 로그인 없이 호출 가능한 device/preview.do 로 조회한다(mhsMonitorcd, searchDay 쿼리 파라미터 사용).
 */
@Slf4j
@RestController
@Tag(name = "MhsPreviewPageController", description = "문화센터 룸 단말 화면 레거시 주소 호환")
public class MhsPreviewPageController {

	private static final String PAGE_PATH = "/preview/mhsPreView.html";

	@Operation(summary = "문화센터 룸 단말 화면(레거시 주소)", description = "mhsMonitorcd(필수), searchDay(선택, YYYYMMDD) 쿼리 파라미터로 화면 표시. 인증 불필요.")
	@RequestMapping(value = "/backoffice/sub/roomManage/preView.do", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
	public ResponseEntity<String> preView() {
		try (InputStream in = getClass().getResourceAsStream(PAGE_PATH)) {
			if (in == null) {
				log.error("단말 화면 리소스 누락(classpath:{})", PAGE_PATH);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("preView page not found");
			}
			return ResponseEntity.ok(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (Exception e) {
			log.error("preView error: {}", e.toString());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("preView error");
		}
	}
}
