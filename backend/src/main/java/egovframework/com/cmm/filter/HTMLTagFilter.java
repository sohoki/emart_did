/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package egovframework.com.cmm.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Set;

public class HTMLTagFilter implements Filter{

	// DID 에이전트 장비 통신 주소 — json/xml 파라미터 원문을 그대로 받아야 하므로 HTML 이스케이프 제외
	// (" → &quot; 로 바뀌면 JSON 파싱 오류: Unexpected character (&))
	private static final Set<String> EXCLUDE_PATHS = Set.of(
			"/backoffice/sub/operManage/jsonAuth.do",
			"/backoffice/sub/operManage/xmlAuth.do"
	);

	@SuppressWarnings("unused")
	private FilterConfig config;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest)request;
		if (EXCLUDE_PATHS.contains(httpRequest.getServletPath())) {
			chain.doFilter(request, response);
			return;
		}
		chain.doFilter(new HTMLTagFilterRequestWrapper(httpRequest), response);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	@Override
	public void destroy() {

	}
}