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
package com.wl4g.iam.test.mock.filter;

import static com.wl4g.components.common.collection.Collections2.safeToList;
import static com.wl4g.components.common.lang.Assert2.notNull;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.components.common.web.WebUtils2.getFullRequestURL;
import static com.wl4g.components.common.web.WebUtils2.writeJson;
import static com.wl4g.iam.test.mock.configure.MockAuthenticatingConfigurer.MOCK_AUTO_AUTHC_URI;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static com.wl4g.iam.common.session.mgt.AbstractIamSessionManager.isInternalTicketRequest;
import static org.apache.shiro.web.util.WebUtils.getPathWithinApplication;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.collections.EnumerationUtils.toList;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static com.wl4g.iam.common.utils.IamAuthenticatingUtils.SESSION_STATUS_UNAUTHC;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.web.rest.RespBase;
import static com.wl4g.components.common.web.rest.RespBase.RetCode.UNAUTHC;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.test.mock.configure.MockAuthenticatingConfigurer;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockUserCredentials;
import com.wl4g.iam.test.mock.handler.NoSuchMockCredentialsException;

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

	/** {@link MockConfigurationFactory} */
	@Autowired
	protected MockConfigurationFactory mockFactory;

	/** {@link MockAuthenticatingConfigurer} */
	@Autowired
	protected MockAuthenticatingConfigurer configurer;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestPath = getPathWithinApplication(toHttp(request));

		if (defaultExcludeUriMatcher.matchStart(MOCK_AUTO_AUTHC_URI, requestPath) || isInternalTicketRequest(request)) {
			chain.doFilter(request, response);
		} else {
			log.debug("Attaching mock iam authenticating requires parameters...");
			try {
				ServletRequest attachRequest = attachMockAuthenticationInfo((HttpServletRequest) request);
				chain.doFilter(attachRequest, response);
			} catch (NoSuchMockCredentialsException e) { // Resp error
				log.error("", e);
				RespBase<String> resp = RespBase.create(SESSION_STATUS_UNAUTHC);
				writeJson(toHttp(response), resp.withCode(UNAUTHC).withThrowable(e).asJson());
			}
		}
	}

	/**
	 * Attach mock iam authentication info to request.
	 * 
	 * @param request
	 * @return
	 * @see {@link com.wl4g.iam.common.mgt.IamSubjectFactory#getRequestAccessToken}
	 * @see {@link com.wl4g.iam.common.session.mgt.AbstractIamSessionManager#getSessionId}
	 * @throws NoSuchMockCredentialsException
	 */
	@SuppressWarnings("unchecked")
	private ServletRequest attachMockAuthenticationInfo(HttpServletRequest request) throws NoSuchMockCredentialsException {
		// Matching mock credentials by configuration
		MockUserCredentials cred = mockFactory.matchMockCredentials(request);
		notNull(cred, NoSuchMockCredentialsException.class, () -> {
			String url = getFullRequestURL(request); // URL

			StringBuffer requestStr = new StringBuffer();
			if (configurer.isEnableVerbose()) {
				requestStr.append("--- Headers ---\n");
				toList(request.getHeaderNames()).forEach(n -> requestStr.append(request.getHeader(valueOf(n))).append("\n"));

				requestStr.append("--- Cookies ---").append("\n");
				safeToList(Cookie.class, request.getCookies()).forEach(c -> requestStr.append(toJSONString(c)).append("\n"));
			}

			return format("No mock authentication credentials were matched, please check config: %s. for requestURL: '%s'\n%s",
					configurer.getConfigURL(), url, requestStr);
		});

		MockHttpRequestWrapper wrap = new MockHttpRequestWrapper(toHttp(request));
		// Attach mock credentials
		wrap.putParameter(config.getParam().getAccessTokenName(), cred.getAuthzInfo().getAccessToken());
		wrap.putParameter(config.getParam().getSid(), cred.getAuthzInfo().getSessionId());
		return wrap;
	}

	/**
	 * Exclude mock URIs mapping matcher.
	 */
	final private static AntPathMatcher defaultExcludeUriMatcher = new AntPathMatcher();

}
