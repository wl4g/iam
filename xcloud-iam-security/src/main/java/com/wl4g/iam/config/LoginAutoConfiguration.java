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
package com.wl4g.iam.config;

import com.wl4g.components.core.config.OptionalPrefixControllerAutoConfiguration;
import com.wl4g.iam.annotation.LoginAuthController;
import com.wl4g.iam.web.LoginAuthenticationEndpoint;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_LOGIN_BASE;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

/**
 * IAM authentication(login) configuration
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年1月8日
 * @since
 */
@AutoConfigureAfter({ IamAutoConfiguration.class })
public class LoginAutoConfiguration extends OptionalPrefixControllerAutoConfiguration {

	@Bean
	public LoginAuthenticationEndpoint loginAuthenticationEndpoint() {
		return new LoginAuthenticationEndpoint();
	}

	@Bean
	public PrefixHandlerMapping loginAuthenticationEndpointPrefixHandlerMapping() {
		return super.newPrefixHandlerMapping(URI_S_LOGIN_BASE, LoginAuthController.class);
	}

}