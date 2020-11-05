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
package com.wl4g.iam.test.mock.config;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_BASE;

import org.springframework.context.annotation.Bean;

import com.wl4g.components.core.config.mapping.AbstractHandlerMappingSupport;
import com.wl4g.components.core.config.mapping.PrefixHandlerMapping;
import com.wl4g.iam.common.annotation.IamController;
import com.wl4g.iam.test.mock.configure.MockAuthenticatingInitializer;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory;
import com.wl4g.iam.test.mock.filter.MockAuthenticatingFilter;
import com.wl4g.iam.test.mock.handler.MockCentralAuthenticatingHandler;
import com.wl4g.iam.test.mock.web.MockCentralAuthenticatingEndpoint;

/**
 * IAM mock auto configuration.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-20
 * @since
 */
public class MockIamAutoConfiguration extends AbstractHandlerMappingSupport {

	@Bean
	public MockConfigurationFactory mockConfigurationFactory() {
		return new MockConfigurationFactory();
	}

	@Bean
	public MockAuthenticatingInitializer mockAuthenticatingConfigurer() {
		return new MockAuthenticatingInitializer();
	}

	@Bean
	public MockAuthenticatingFilter mockAuthenticatingFilter() {
		return new MockAuthenticatingFilter();
	}

	@Bean
	public MockCentralAuthenticatingHandler mockCentralAuthenticatingHandler() {
		return new MockCentralAuthenticatingHandler();
	}

	// ==============================
	// Mock IAM endpoint's
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