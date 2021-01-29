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
package com.wl4g.iam.core.rpc;

import com.wl4g.component.common.bridge.FeignRpcContextProcessorBridgeUtils;
import com.wl4g.component.common.bridge.RpcContextHolderBridgeUtils;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.framework.proxy.SmartProxyProcessor;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.utils.IamSecurityHolder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.iam.common.constant.RpcContextIAMConstants.*;

/**
 * {@link RpcContextIamSecurityAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-25
 * @sine v1.0
 * @see
 */
@ConditionalOnClass(name = RpcContextHolderBridgeUtils.rpcContextHolderClassName)
public class RpcContextIamSecurityAutoConfiguration {

	@Bean
	public SecurityRpcContextProcessor securityRpcContextProcessor() {
		return new SecurityRpcContextProcessor();
	}

	class SecurityRpcContextProcessor implements SmartProxyProcessor {
		protected final SmartLogger log = getLogger(getClass());

		@Override
		public int getOrder() {
			return FeignRpcContextProcessorBridgeUtils.invokeFieldOrder() + 1;
		}

		@Override
		public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
			return FeignRpcContextProcessorBridgeUtils.invokeCheckSupportTypeProxy(target, actualOriginalTargetClass);
		}

		@Override
		public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
			return FeignRpcContextProcessorBridgeUtils.invokeCheckSupportMethodProxy(target, method, actualOriginalTargetClass,
					args);
		}

		@Override
		public Object[] preHandle(@NotNull Object target, @NotNull Method method, Object[] parameters) {
			// Bind iam current attributes to rpc context.
			if (RpcContextHolderBridgeUtils.hasRpcContextHolderClass()) { // Distributed?
				IamPrincipal currentPrincipal = IamSecurityHolder.getPrincipalInfo();

				RpcContextHolderBridgeUtils.invokeSet(CURRENT_IAM_PRINCIPAL_ID, currentPrincipal.getPrincipalId());
				RpcContextHolderBridgeUtils.invokeSet(CURRENT_IAM_PRINCIPAL_USER, currentPrincipal.getName());

				// Set to reference type for performance optimization.
				RpcContextHolderBridgeUtils.invokeSetRef(CURRENT_IAM_PRINCIPAL, currentPrincipal);
			}
			return parameters;
		}

	}

}
