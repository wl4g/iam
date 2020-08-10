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
package com.wl4g.iam.test.config;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_BASE;

import org.springframework.context.annotation.Bean;

import com.wl4g.components.core.config.OptionalPrefixControllerAutoConfiguration;
import com.wl4g.iam.common.annotation.IamController;
import com.wl4g.iam.test.handler.MockCentralAuthenticatingHandler;
import com.wl4g.iam.test.web.MockCentralAuthenticatingEndpoint;

/**
 * Embedded test IAM server auto configuration.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-20
 * @since
 */
public class MockIamAutoConfiguration extends OptionalPrefixControllerAutoConfiguration {

	@Bean
	public MockCentralAuthenticatingHandler mockCentralAuthenticatingHandler() {
		return new MockCentralAuthenticatingHandler();
	}

	// ==============================
	// Mock iam endpoint's
	// ==============================

	@Bean
	public MockCentralAuthenticatingEndpoint mockCentralValidationEndpoint() {
		return new MockCentralAuthenticatingEndpoint();
	}

	@Bean
	public PrefixHandlerMapping mockCentralValidationEndpointPrefixHandlerMapping() {
		return super.newPrefixHandlerMapping(URI_S_BASE, IamController.class);
	}

}