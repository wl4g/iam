/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.iam.web.interceptor;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.utils.IamSecurityHolder;

/**
 * {@link IamContextAttributesInterceptor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-25
 * @sine v1.0
 * @see
 */
@Configuration
@ConditionalOnWebApplication(type = Type.SERVLET)
public class IamContextServletConfigurer implements WebMvcConfigurer {
	protected final SmartLogger log = getLogger(getClass());

	@Bean
	public IamContextAttributesInterceptor iamContextAttributesInterceptor() {
		return new IamContextAttributesInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(iamContextAttributesInterceptor()).addPathPatterns("/**");
	}

	@Order(Ordered.HIGHEST_PRECEDENCE + 10)
	class IamContextAttributesInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			IamPrincipal iamPrincipal = IamSecurityHolder.getPrincipalInfo();
			RpcContextHolder.get().set("IAM_PRINCIPAL", iamPrincipal);
			return true;
		}
	}

}
