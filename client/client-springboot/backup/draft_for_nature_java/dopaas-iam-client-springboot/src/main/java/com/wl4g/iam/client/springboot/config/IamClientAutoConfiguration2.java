/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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
package com.wl4g.iam.client.springboot.config;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;

import com.wl4g.iam.client.configure.IamConfigurer;
import com.wl4g.iam.client.configure.StandardEnvironmentIamConfigurer;
import com.wl4g.iam.client.core.RequestMappingDispatcher;
import com.wl4g.iam.client.core.ServletFilterMappingDispatcher;
import com.wl4g.iam.client.handler.StandardSignApiWebHandler;
import com.wl4g.iam.client.handler.WebIamHandler;
import com.wl4g.iam.client.handler.authc.FastCasWebHandler;
import com.wl4g.iam.client.springboot.core.WebFluxFilterMappingDispatcher;

/**
 * {@link IamClientAutoConfiguration2}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020-09-08
 * @since
 */
public class IamClientAutoConfiguration2 {

	@Bean
	@ConditionalOnMissingBean
	public IamConfigurer standardEnvironmentIamConfigurer() {
		return new StandardEnvironmentIamConfigurer();
	}

	@Bean
	public WebIamHandler fastCasWebHandler(IamConfigurer configurer) {
		return new FastCasWebHandler(configurer);
	}

	@Bean
	public WebIamHandler standardSignApiWebHandler(IamConfigurer configurer) {
		return new StandardSignApiWebHandler(configurer);
	}

	// --- Webflux dispatcher. ---

	@Bean
	@ConditionalOnClass(EnableWebFlux.class)
	public RequestMappingDispatcher webfluxFilterMappingDispatcher(IamConfigurer configurer, List<WebIamHandler> handlers) {
		return new WebFluxFilterMappingDispatcher(configurer, handlers);
	}

	// --- Servlet dispatcher. ---

	@Bean
	@ConditionalOnMissingBean
	public RequestMappingDispatcher servletFilterMappingDispatcher(IamConfigurer configurer, List<WebIamHandler> handlers) {
		return new ServletFilterMappingDispatcher(configurer, handlers);
	}

	@Bean
	@ConditionalOnBean(ServletFilterMappingDispatcher.class)
	public FilterRegistrationBean<ServletFilterMappingDispatcher> servletFilterMappingDispatcherFilterBean(
			ServletFilterMappingDispatcher filter) {
		FilterRegistrationBean<ServletFilterMappingDispatcher> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(true);
		registration.setOrder(HIGHEST_PRECEDENCE + 1);
		registration.addUrlPatterns("/*"); // urlPatterns
		return registration;
	}

}
