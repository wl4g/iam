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

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.framework.proxy.SmartProxyProcessor;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;
import com.wl4g.component.rpc.springboot.feign.context.interceptor.FeignContextAutoConfiguration.FeignContextProxyProcessor;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.utils.IamSecurityHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.iam.common.constant.ContextIAMConstants.*;

/**
 * {@link IamContextAttributeServletInterceptor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-25
 * @sine v1.0
 * @see
 */
@Configuration
@ConditionalOnClass(RpcContextHolder.class)
public class IamContextAutoConfiguration {
	protected final SmartLogger log = getLogger(getClass());

	@Bean
	public IamContextAttributeProxyProcessor iamContextAttributeProxyProcessor() {
		return new IamContextAttributeProxyProcessor();
	}

	class IamContextAttributeProxyProcessor implements SmartProxyProcessor {

		@Override
		public int getOrder() {
			return FeignContextProxyProcessor.ORDER + 1;
		}

		@Override
		public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
			return FeignContextProxyProcessor.checkSupportTypeProxy(target, actualOriginalTargetClass);
		}

		@Override
		public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
			return FeignContextProxyProcessor.checkSupportMethodProxy(target, method, actualOriginalTargetClass, args);
		}

		@Override
		public Object[] preHandle(@NotNull Object target, @NotNull Method method, Object[] parameters) {
			// Bind iam current attributes to rpc context.
			IamPrincipal currentPrincipal = IamSecurityHolder.getPrincipalInfo();
			RpcContextHolder.get().set(CURRENT_IAM_PRINCIPAL_ID, currentPrincipal.getPrincipalId());
			RpcContextHolder.get().set(CURRENT_IAM_PRINCIPAL_USER, currentPrincipal.getName());

			// Set to reference type for performance optimization.
			RpcContextHolder.get().set(new RpcContextHolder.RefAttachmentKey(CURRENT_IAM_PRINCIPAL), currentPrincipal);

			return parameters;
		}

	}

}
