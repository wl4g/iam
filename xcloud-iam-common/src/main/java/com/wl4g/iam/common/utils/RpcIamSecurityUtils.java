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
package com.wl4g.iam.common.utils;

import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.common.subject.SimpleIamPrincipal;

import static java.util.Objects.nonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.iam.common.constant.ContextIAMConstants.*;

/**
 * {@link RpcIamSecurityUtils}
 *
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-05
 * @sine v1.0
 * @see {@link com.wl4g.iam.web.interceptor.IamContextAutoConfiguration}
 */
public final class RpcIamSecurityUtils {

	private RpcIamSecurityUtils() {
	}

	public static IamPrincipal currentIamPrincipal() {
		if (nonNull(rpcContextHolderClass) && nonNull(refAttachmentKeyClass)) { // Distributed?
			try {
				Constructor<?> defaultConstr = refAttachmentKeyClass.getConstructor(String.class);
				Object obj = defaultConstr.newInstance(CURRENT_IAM_PRINCIPAL);
				return (IamPrincipal) invokeMethod(RPC_CONTEXT_HOLDER_GET_METHOD2, currentRpcContextHolder(), obj,
						SimpleIamPrincipal.class);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		// Local mode
		return (IamPrincipal) invokeMethod(IAMSECURITYHOLDER_GETPRINCIPALINFO_METHOD, null);
	}

	public static String currentIamPrincipalId() {
		if (nonNull(rpcContextHolderClass)) { // Distributed?
			return (String) invokeMethod(RPC_CONTEXT_HOLDER_GET_METHOD1, currentRpcContextHolder(), CURRENT_IAM_PRINCIPAL_ID,
					String.class);
		}
		// Local mode
		return currentIamPrincipal().getPrincipalId();
	}

	public static String currentIamPrincipalName() {
		if (nonNull(rpcContextHolderClass)) { // Distributed?
			return (String) invokeMethod(RPC_CONTEXT_HOLDER_GET_METHOD1, currentRpcContextHolder(), CURRENT_IAM_PRINCIPAL_USER,
					String.class);
		}
		// Local mode
		return currentIamPrincipal().getPrincipal();
	}

	private static Object currentRpcContextHolder() {
		Object currentRpcContextHolder = invokeMethod(RPC_CONTEXT_HOLDER_GET_METHOD0, null);
		return notNullOf(currentRpcContextHolder, "currentRpcContextHolder");
	}

	public static final Class<?> rpcContextHolderClass = resolveClassNameNullable(
			"com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder");

	public static final Class<?> refAttachmentKeyClass = resolveClassNameNullable(
			"com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder.RefAttachmentKey");

	public static final Method RPC_CONTEXT_HOLDER_GET_METHOD0 = findMethodNullable(rpcContextHolderClass, "get");
	public static final Method RPC_CONTEXT_HOLDER_GET_METHOD1 = findMethodNullable(rpcContextHolderClass, "get", String.class,
			Class.class);
	public static final Method RPC_CONTEXT_HOLDER_GET_METHOD2 = findMethodNullable(rpcContextHolderClass, "get",
			refAttachmentKeyClass, Class.class);
	public static final Method IAMSECURITYHOLDER_GETPRINCIPALINFO_METHOD = findMethodNullable(
			resolveClassNameNullable("com.wl4g.iam.core.utils.IamSecurityHolder"), "getPrincipalInfo");

}
