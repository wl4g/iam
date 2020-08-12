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
package com.wl4g.iam.test.configure;

import static com.wl4g.components.common.lang.Assert2.isInstanceOf;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.KEY_SESSIONINFO_NAME;
import static java.lang.String.valueOf;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.web.model.SessionInfo;

/**
 * {@link MockAuthenticatingConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-11
 * @sine v1.0.0
 * @see
 */
public class MockAuthenticatingConfigurer implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	protected IamClientProperties config;

	@Autowired
	protected Environment environment;

	/**
	 * Mock iam client authentication accessToken(credentials) info.
	 */
	private String accessToken;

	/**
	 * Mock iam client authentication session info.
	 */
	private String sessionId;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		initMockAuthenticationInfo();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Initialization mock iam client authentication info
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initMockAuthenticationInfo() {
		String baseUri = "http://localhost:".concat(environment.getRequiredProperty("server.port"))
				.concat(environment.getRequiredProperty("server.servlet.context-path"));
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
	public static final String MOCK_AUTO_AUTHENTICATING_URI = "/mock-auto-authenticating";

}
