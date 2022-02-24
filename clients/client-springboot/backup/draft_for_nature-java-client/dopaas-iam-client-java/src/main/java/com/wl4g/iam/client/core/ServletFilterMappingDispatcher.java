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
package com.wl4g.iam.client.core;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wl4g.infra.common.remoting.standard.HttpStatus;
import com.wl4g.infra.common.web.WebUtils2;
import com.wl4g.iam.client.configure.IamConfigurer;
import com.wl4g.iam.client.core.HttpIamRequest.HttpCookie;
import com.wl4g.iam.client.handler.WebIamHandler;

/**
 * {@link ServletFilterMappingDispatcher}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-03
 * @since
 */
public class ServletFilterMappingDispatcher extends AbstractMappingDispatcher implements Filter {

	public ServletFilterMappingDispatcher(IamConfigurer configurer, List<WebIamHandler> handlers) {
		super(configurer, handlers);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		try {
			final boolean isContinue = doAuthenticatingHandlers(new ServletHttpIamRequest((HttpServletRequest) request),
					new ServletHttpIamResponse((HttpServletResponse) response));

			if (isContinue) {
				// do next
				chain.doFilter(request, response);
			}

		} catch (Exception e) {
			log.error("", e);
		}
		// No continue
	}

	/**
	 * {@link ServletHttpIamRequest}
	 * 
	 * @since
	 */
	public static class ServletHttpIamRequest implements HttpIamRequest {

		/** {@link HttpServletRequest} */
		protected final HttpServletRequest request;

		// --- Temporary fields. ---

		private transient URI uri;
		private transient Map<String, List<String>> queryParams;
		private transient InetSocketAddress remoteAddress, localAddress;
		private transient Map<String, List<HttpCookie>> cookies;

		public ServletHttpIamRequest(HttpServletRequest request) {
			notNullOf(request, "request");
			this.request = request;
		}

		@Override
		public Map<String, List<String>> getHeaders() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getMethodValue() {
			return request.getMethod();
		}

		@Override
		public URI getURI() {
			if (isNull(uri)) {
				synchronized (this) {
					if (isNull(uri)) {
						this.uri = URI.create(request.getRequestURL().toString());
					}
				}
			}
			return this.uri;
		}

		@Override
		public String getContextPath() {
			return request.getContextPath();
		}

		@Override
		public Map<String, List<String>> getQueryParams() {
			if (isNull(queryParams)) {
				synchronized (this) {
					if (isNull(queryParams)) {
						this.queryParams = WebUtils2.parseMatrixVariables(request.getQueryString());
					}
				}
			}
			return this.queryParams;
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			if (isNull(remoteAddress)) {
				synchronized (this) {
					if (isNull(remoteAddress)) {
						return this.remoteAddress = new InetSocketAddress(request.getRemoteAddr(), request.getRemotePort());
					}
				}
			}
			return this.remoteAddress;
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			if (isNull(localAddress)) {
				synchronized (this) {
					if (isNull(localAddress)) {
						return this.localAddress = new InetSocketAddress(request.getLocalAddr(), request.getLocalPort());
					}
				}
			}
			return this.localAddress;
		}

		@Override
		public Map<String, List<HttpCookie>> getCookies() {
			if (isNull(this.cookies)) {
				synchronized (this) {
					if (isNull(this.cookies)) {
						this.cookies = safeList(asList(request.getCookies())).stream()
								.map(c -> singletonMap(c.getName(), singletonList(convertHttpCookie(c))))
								.flatMap(e -> e.entrySet().stream()).collect(toMap(e -> e.getKey(), e -> e.getValue()));
					}
				}
			}
			return this.cookies;
		}

		/**
		 * Converting tangible cookie to {@link HttpCookie}
		 * 
		 * @param c
		 * @return
		 */
		private final HttpCookie convertHttpCookie(Cookie c) {
			return new HttpCookie(c.getName(), c.getValue(), c.getComment(), c.getDomain(), c.getPath(), c.getSecure(), null);
		}

	}

	/**
	 * {@link ServletHttpIamResponse}
	 * 
	 * @since
	 */
	public static class ServletHttpIamResponse implements HttpIamResponse {

		/** {@link HttpServletResponse} */
		protected final HttpServletResponse response;

		//
		// --- Temporary fields. ---
		//

		private transient Map<String, List<String>> headers;

		public ServletHttpIamResponse(HttpServletResponse response) {
			notNullOf(response, "response");
			this.response = response;
		}

		@Override
		public Map<String, List<String>> getHeaders() {
			if (isNull(headers)) {
				synchronized (this) {
					if (isNull(headers)) {
						for (String hname : response.getHeaderNames()) {
							headers.put(hname, new ArrayList<>(response.getHeaders(hname)));
						}
					}
				}
			}
			return headers;
		}

		@Override
		public boolean setStatusCode(HttpStatus status) {
			try {
				response.setStatus(status.value());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		public HttpStatus getStatusCode() {
			return HttpStatus.valueOf(response.getStatus());
		}

		@Override
		public Map<String, List<HttpCookie>> getCookies() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addCookie(HttpCookie c) {
			Cookie cookie = new Cookie(c.getName(), c.getValue());
			cookie.setComment(c.getComment());
			cookie.setPath(c.getPath());
			cookie.setSecure(c.isSecure());
			cookie.setHttpOnly(c.isHttpOnly());
			cookie.setVersion(c.getVersion());
			response.addCookie(cookie);
		}

		@Override
		public boolean isCommitted() {
			return response.isCommitted();
		}

		@Override
		public Object write(Object body) {
			try {
				if (body instanceof String) {
					response.getWriter().write((String) body);
				} else if (body instanceof byte[]) {
					response.getOutputStream().write((byte[]) body);
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return null;
		}

		@Override
		public Object writeAndFlushWith(Object body) {
			try {
				write(body);
				response.flushBuffer();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return null;
		}

	}

}
