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
package com.wl4g.iam.service.utils;

import static com.wl4g.component.common.lang.ClassUtils2.isPresent;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.iam.common.constant.ContextIAMConstants.CURRENT_IAM_PRINCIPAL;
import static com.wl4g.iam.common.constant.ContextIAMConstants.CURRENT_IAM_PRINCIPAL_ID;
import static com.wl4g.iam.common.constant.ContextIAMConstants.CURRENT_IAM_PRINCIPAL_USER;

import java.lang.reflect.Method;

import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder.RefAttachmentKey;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.common.subject.SimpleIamPrincipal;

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
		if (hasRpcContextHolderClass) { // Distributed?
			return RpcContextHolder.get().get(new RefAttachmentKey(CURRENT_IAM_PRINCIPAL), SimpleIamPrincipal.class);
		}
		// Local mode
		return (IamPrincipal) invokeMethod(IAMSECURITYHOLDER_GETPRINCIPALINFO_METHOD, null);
	}

	public static String currentIamPrincipalId() {
		if (hasRpcContextHolderClass) { // Distributed?
			return RpcContextHolder.get().get(CURRENT_IAM_PRINCIPAL_ID, String.class);
		}
		// Local mode
		return currentIamPrincipal().getPrincipalId();
	}

	public static String currentIamPrincipalName() {
		if (hasRpcContextHolderClass) { // Distributed?
			return RpcContextHolder.get().get(CURRENT_IAM_PRINCIPAL_USER, String.class);
		}
		// Local mode
		return currentIamPrincipal().getPrincipal();
	}

	public static final boolean hasRpcContextHolderClass = isPresent(
			"com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder", null);
	public static final Method IAMSECURITYHOLDER_GETPRINCIPALINFO_METHOD = findMethod(
			resolveClassNameNullable("com.wl4g.iam.core.utils.IamSecurityHolder"), "getPrincipalInfo");

}
