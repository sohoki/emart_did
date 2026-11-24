package com.common.backoffice.sts.agent.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DID 에이전트 서버 시간 동기화(레거시 serverDate.do 호환)
 *
 * - 레거시 주소는 /backoffice/sub/operManage/serverDate.do 였으나, 현재 배포된 에이전트
 *   (agentConstInfo.serverJsonUtrl02)는 /backoffice/operManage/serverDate.do(sub 없음)로 호출하므로
 *   두 경로를 모두 매핑한다.
 * - DidAgentController 는 클래스 레벨에 /backoffice/sub 접두어가 있어 sub 없는 경로를 둘 수 없으므로 별도 컨트롤러로 분리.
 * - 에이전트는 응답 문자열을 그대로 Convert.ToDateTime() 하므로 레거시와 동일한 포맷(yyyy.MM.dd HH:mm:ss)의 순수 텍스트로 반환한다.
 */
@Slf4j
@RestController
@Tag(name = "DidAgentServerDateController", description = "DID 에이전트 서버 시간 동기화(레거시 주소 호환)")
public class DidAgentServerDateController {

	private static final String TEXT_UTF8 = "text/plain;charset=UTF-8";
	private static final DateTimeFormatter SERVER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

	@Operation(summary = "서버 현재 시간", description = "yyyy.MM.dd HH:mm:ss 형식의 서버 현재 시간 문자열. 실패 시 빈 문자열.")
	@RequestMapping(value = {
			"/backoffice/sub/operManage/serverDate.do",
			"/backoffice/operManage/serverDate.do"
	}, produces = TEXT_UTF8)
	public String serverDate() {
		try {
			return LocalDateTime.now().format(SERVER_DATE_FORMAT);
		} catch (Exception e) {
			log.error("serverDate error: {}", e.toString());
			return "";
		}
	}
}
