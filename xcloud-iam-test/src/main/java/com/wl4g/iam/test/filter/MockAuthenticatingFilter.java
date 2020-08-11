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
import static com.wl4g.iam.common.session.mgt.AbstractIamSessionManager.isInternalTicketRequest;
import static org.apache.shiro.web.util.WebUtils.getPathWithinApplication;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.util.AntPathMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.test.configure.MockAuthenticatingConfigurer;
import static com.wl4g.iam.test.configure.MockAuthenticatingConfigurer.MOCK_AUTO_AUTHENTICATING_URI;

/**
 * {@link MockAuthenticatingFilter}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-11
 * @since
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MockAuthenticatingFilter implements Filter {

	protected final SmartLogger log = getLogger(getClass());

	@Autowired
	protected IamClientProperties config;

	@Autowired
	protected MockAuthenticatingConfigurer mockIamConfigurer;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestPath = getPathWithinApplication(toHttp(request));

		if (defaultExcludeUriMatcher.matchStart(MOCK_AUTO_AUTHENTICATING_URI, requestPath) || isInternalTicketRequest(request)) {
			chain.doFilter(request, response);
		} else {
			log.debug("Attaching mock iam authenticating requires parameters...");
			chain.doFilter(attachMockAuthenticationInfo(request), response);
		}
	}

	/**
	 * Attach mock iam authentication info to request.
	 * 
	 * @param req
	 * @return
	 * @see {@link com.wl4g.iam.common.mgt.IamSubjectFactory#getRequestAccessToken}
	 * @see {@link com.wl4g.iam.common.session.mgt.AbstractIamSessionManager#getSessionId}
	 */
	private ServletRequest attachMockAuthenticationInfo(ServletRequest req) {
		MockHttpRequestWrapper wrap = new MockHttpRequestWrapper(toHttp(req));

		// Attach mock query parameters.
		wrap.putParameter(config.getParam().getAccessTokenName(), new String[] { mockIamConfigurer.getAccessToken() });
		wrap.putParameter(config.getParam().getSid(), new String[] { mockIamConfigurer.getSessionId() });
		return wrap;
	}

	/**
	 * Exclude mock URIs mapping matcher.
	 */
	final private static AntPathMatcher defaultExcludeUriMatcher = new AntPathMatcher();

}
