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
package com.wl4g.iam.core.security.xss.resolver;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.wl4g.iam.core.security.xss.DefaultXssHttpRequestWrapper;

/**
 * XSS security resolver.
 *
 * @author wangl.sir
 * @version v1.0 2019年4月29日
 * @since
 */
public interface XssSecurityResolver {

	/**
	 * Perform parsing to convert XSS attack strings to safe strings.
	 *
	 * @param method
	 *            Current method of parsing XSS
	 * @param index
	 *            Parameter number of the current method for parsing XSS
	 * @param value
	 *            The parameter value of the current method of parsing XSS
	 * @return
	 */
	String doResolve(final Object controller, final Method method, final int index, final String value);

	/**
	 * Newly created XSS secure HttpServletRequestWrapper object
	 *
	 * @param request
	 * @return
	 */
	default HttpServletRequestWrapper newXssRequestWrapper(HttpServletRequest request) {
		return new DefaultXssHttpRequestWrapper(request);
	}

}