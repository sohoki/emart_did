package com.common.backoffice.sts.agent.web;

import com.common.backoffice.sts.agent.service.DidAgentProtocolService;
import com.common.backoffice.sts.pic.web.DidMoniterPicController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartRequest;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/backoffice/sub")
@Tag(name = "DidAgentController", description = "DID 에이전트 레거시 주소 호환 API")
public class DidAgentController {

	private static final String TEXT_UTF8 = "text/plain;charset=UTF-8";

	private final DidMoniterPicController didMoniterPicController;
	private final DidAgentProtocolService protocolService;

	// 원본처럼 GET/POST 모두 허용(장비가 쿼리스트링/폼 어느 쪽으로 보내도 처리)
	@Operation(summary = "DID 장비 통신(JSON)", description = "json 파라미터의 command_type별 처리. 미등록 명령이면 NO_JSON.")
	@RequestMapping(value = "/operManage/jsonAuth.do", produces = TEXT_UTF8)
	public String jsonAuth(@RequestParam(value = "json", required = false, defaultValue = "") String json) {
		try {
			return protocolService.handleJson(unescapeHtml(json));
		} catch (Exception e) {
			log.error("jsonAuth error: {} / json={}", e.toString(), json);
			return "ERROR:" + e;
		}
	}

	@Operation(summary = "DID 장비 통신(XML)", description = "xml 파라미터의 command_type별 처리. 미등록 명령이면 NO_XMLNM.")
	@RequestMapping(value = "/operManage/xmlAuth.do", produces = TEXT_UTF8)
	public String xmlAuth(@RequestParam(value = "xml", required = false, defaultValue = "") String xml) {
		try {
			return protocolService.handleXml(unescapeHtml(xml));
		} catch (Exception e) {
			log.error("xmlAuth error: {} / xml={}", e.toString(), xml);
			return "ERROR:" + e;
		}
	}

	@Operation(summary = "DID 캡처화면 업로드(레거시 주소)", description = "기존 에이전트 주소. 응답은 O(성공)/0(실패) 문자열.")
	@PostMapping("/equiManage/Capture.do")
	public String capture(MultipartRequest mRequest, HttpServletRequest request) {
		return didMoniterPicController.insertDidPictureXml(mRequest, request);
	}

	// HTMLTagFilter 가 적용된 경우(" → &quot; 등) 원문으로 복원 — 필터 제외가 누락돼도 파싱되도록 하는 안전장치
	// &amp; 는 마지막에 복원해야 이중 복원(&amp;quot; → ")이 생기지 않는다
	private static String unescapeHtml(String value) {
		if (value == null || value.indexOf('&') < 0) {
			return value;
		}
		return value.replace("&quot;", "\"")
				.replace("&apos;", "'")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&amp;", "&");
	}
}
