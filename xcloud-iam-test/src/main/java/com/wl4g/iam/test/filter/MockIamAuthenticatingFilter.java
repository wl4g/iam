/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.iam.test.filter;

import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.KEY_SESSIONINFO_NAME;
import static com.wl4g.iam.common.session.mgt.AbstractIamSessionManager.isInternalTicketRequest;
import static java.lang.String.valueOf;
import static org.apache.shiro.web.util.WebUtils.getPathWithinApplication;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.util.AntPathMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static com.wl4g.components.common.lang.Assert2.isInstanceOf;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.RespBase;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.web.model.SessionInfo;

/**
 * {@link MockIamAuthenticatingFilter}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-11
 * @since
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MockIamAuthenticatingFilter implements Filter, ApplicationListener<ApplicationReadyEvent> {

	protected final SmartLogger log = getLogger(getClass());

	@Autowired
	protected IamClientProperties config;

	@Autowired
	protected Environment env;

	/**
	 * Mock iam client authentication credentials info.
	 */
	private String accessToken;
	private String sessionId;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		initMockAuthenticationInfo();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestPath = getPathWithinApplication(toHttp(request));

		if (defaultExcludeUriMatcher.matchStart(MOCK_AUTO_AUTHENTICATING_URI, requestPath) || isInternalTicketRequest(request)) {
			chain.doFilter(request, response);
		} else {
			log.debug("Attaching mock iam authenticating requires parameters...");
			chain.doFilter(attachMockAuthenticatingRequest(request), response);
		}
	}

	/**
	 * Attach mock iam authenticating parameters to request.
	 * 
	 * @param req
	 * @return
	 * @see {@link com.wl4g.iam.common.mgt.IamSubjectFactory#getRequestAccessToken}
	 * @see {@link com.wl4g.iam.common.session.mgt.AbstractIamSessionManager#getSessionId}
	 */
	private ServletRequest attachMockAuthenticatingRequest(ServletRequest req) {
		MockHttpRequestWrapper wrap = new MockHttpRequestWrapper(toHttp(req));

		// Attach mock query parameters.
		wrap.putParameter(config.getParam().getAccessTokenName(), new String[] { accessToken });
		wrap.putParameter(config.getParam().getSid(), new String[] { sessionId });
		return wrap;
	}

	/**
	 * Initialization mock authentication info
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initMockAuthenticationInfo() {
		String baseUri = "http://localhost:".concat(env.getRequiredProperty("server.port"))
				.concat(env.getRequiredProperty("server.servlet.context-path"));
		String mockAutoAuthenticatingUri = baseUri.concat(MOCK_AUTO_AUTHENTICATING_URI);

		// Mock internal authenticating request(IamClient)
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(6_000);
		factory.setReadTimeout(30_000);
		RespBase<Map> resp = new RestTemplate(factory).getForObject(URI.create(mockAutoAuthenticatingUri), RespBase.class);
		this.accessToken = valueOf(resp.getData().get(config.getParam().getAccessTokenName()));

		Object session = resp.getData().get(KEY_SESSIONINFO_NAME);
		isInstanceOf(Map.class, session, "Shouldn't be here");
		this.sessionId = valueOf(((Map) session).get(SessionInfo.KEY_SESSION_VALUE)); // sessionId
	}

	/** Mock auto authenticating URI */
	private static final String MOCK_AUTO_AUTHENTICATING_URI = "/mock-auto-authenticating";

	/**
	 * Exclude mock URIs mapping matcher.
	 */
	final private static AntPathMatcher defaultExcludeUriMatcher = new AntPathMatcher();

}
