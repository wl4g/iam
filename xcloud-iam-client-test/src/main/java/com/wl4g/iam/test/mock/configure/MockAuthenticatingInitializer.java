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
package com.wl4g.iam.test.mock.configure;

import static com.wl4g.components.common.lang.Assert2.isInstanceOf;
import static com.wl4g.iam.common.constant.GenericIAMConstants.KEY_SESSIONINFO_NAME;
import static java.lang.String.valueOf;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.core.web.model.SessionInfo;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockAuthcInfo;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockUserCredentials;

/**
 * {@link MockAuthenticatingInitializer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-11
 * @sine v1.0.0
 * @see
 */
public class MockAuthenticatingInitializer extends BaseConfigurationInitializer
		implements ApplicationListener<ApplicationReadyEvent> {

	/** {@link IamClientProperties} */
	@Autowired
	protected IamClientProperties config;

	/** {@link RestTemplate} */
	protected RestTemplate restTemplate;

	/** Mock auto authenticating URI */
	protected String mockAutoAuthcUri;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		// Mock auto authenticating URI
		String baseUri = "http://localhost:".concat(actx.getEnvironment().getRequiredProperty("server.port"))
				.concat(actx.getEnvironment().getRequiredProperty("server.servlet.context-path"));
		this.mockAutoAuthcUri = baseUri.concat(MOCK_AUTO_AUTHC_URI);

		// Mock internal authenticating request(IamClient)
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(10_000);
		factory.setReadTimeout(30_000);
		this.restTemplate = new RestTemplate(factory);
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		initMockAuthenticationInfo();
	}

	/**
	 * Initialization mock iam client authentication info
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initMockAuthenticationInfo() {

		// Init all mock user authz info
		for (MockUserCredentials cred : mockFactory.getMockUserCredentials()) {
			URI validatingUri = URI.create(mockAutoAuthcUri.concat("?").concat(MOCK_AUTO_AUTHC_PRINCIPAL).concat("=")
					.concat(cred.getAuthzInfo().getPrincipal()));

			RespBase<Map> resp = restTemplate.getForObject(validatingUri, RespBase.class);
			String accessToken = valueOf(resp.getData().get(config.getParam().getAccessTokenName()));

			Object session = resp.getData().get(KEY_SESSIONINFO_NAME);
			isInstanceOf(Map.class, session, "Shouldn't be here");
			String sessionId = valueOf(((Map) session).get(SessionInfo.KEY_SESSION_VALUE));

			// Storage mock user authz info
			cred.setAuthcInfo(new MockAuthcInfo(accessToken, sessionId));
		}

	}

	/** Mock auto authenticating URI */
	public static final String MOCK_AUTO_AUTHC_URI = "/mock-auto-authenticating";

	/** Mock auto authenticating principal name. */
	public static final String MOCK_AUTO_AUTHC_PRINCIPAL = "principal";

}
