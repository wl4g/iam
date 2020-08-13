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
package com.wl4g.iam.test.mock.filter;

import static com.wl4g.components.common.collection.Collections2.isEmptyArray;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Mock iam Http request wrapper
 *
 * @author wangl.sir
 * @version v1.0 2019年4月26日
 * @since
 */
public class MockHttpRequestWrapper extends HttpServletRequestWrapper {

	private HttpServletRequest orig;

	private List<Cookie> cookies = new ArrayList<>(8);
	private Map<String, String[]> parameterMap = new LinkedHashMap<>();

	public MockHttpRequestWrapper(HttpServletRequest request) {
		super(request);
		this.orig = request;
		Cookie[] _cs = super.getCookies();
		if (!isEmptyArray(_cs)) {
			this.cookies.addAll(asList(_cs));
		}
	}

	public HttpServletRequest getOrigRequest() {
		return orig;
	}

	@Override
	public synchronized Cookie[] getCookies() {
		return cookies.toArray(new Cookie[] {});
	}

	public synchronized MockHttpRequestWrapper addCookie(Cookie... cs) {
		if (nonNull(cs)) {
			for (Cookie c : cs) {
				this.cookies.add(c);
			}
		}
		return this;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	public synchronized MockHttpRequestWrapper putParameter(String key, String[] values) {
		if (!isNull(key)) {
			this.parameterMap.put(key, values);
		}
		return this;
	}

	@Override
	public String getParameter(String name) {
		String[] value = getParameterMap().get(name);
		if (nonNull(value) && value.length > 0) {
			return value[0];
		}
		return super.getParameter(name);
	}

}