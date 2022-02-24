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

import static com.wl4g.infra.common.lang.Assert2.hasText;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.wl4g.infra.common.annotation.Nullable;

import io.netty.handler.codec.http.HttpMethod;

/**
 * {@link HttpIamRequest}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-03
 * @since
 */
public interface HttpIamRequest {

	/**
	 * Return the headers of this message.
	 * 
	 * @return a corresponding HttpHeaders object (never {@code null})
	 */
	Map<String, List<String>> getHeaders();

	/**
	 * Return the HTTP method of the request.
	 * 
	 * @return the HTTP method as an HttpMethod enum value, or {@code null} if
	 *         not resolvable (e.g. in case of a non-standard HTTP method)
	 * @see #getMethodValue()
	 * @see HttpMethod#resolve(String)
	 */
	@Nullable
	default HttpMethod getMethod() {
		return new HttpMethod(getMethodValue());
	}

	/**
	 * Return the HTTP method of the request as a String value.
	 * 
	 * @return the HTTP method as a plain String
	 * @since 5.0
	 * @see #getMethod()
	 */
	String getMethodValue();

	/**
	 * Return the URI of the request (including a query string if any, but only
	 * if it is well-formed for a URI representation).
	 * 
	 * @return the URI of the request (never {@code null})
	 */
	URI getURI();

	/**
	 * Return an id that represents the underlying connection, if available, or
	 * the request for the purpose of correlating log messages.
	 * 
	 * @since 5.1
	 * @see org.springframework.web.server.ServerWebExchange#getLogPrefix()
	 */
	default String getId() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the portion of the request URI that indicates the context of the
	 * request. The context path always comes first in a request URI. The path
	 * starts with a "/" character but does not end with a "/" character. For
	 * servlets in the default (root) context, this method returns "". The
	 * container does not decode this string.
	 *
	 * @return a <code>String</code> specifying the portion of the request URI
	 *         that indicates the context of the request
	 */
	public String getContextPath();

	/**
	 * Return a read-only map with parsed and decoded query parameter values.
	 */
	Map<String, List<String>> getQueryParams();

	/**
	 * Return a read-only map of cookies sent by the client.
	 */
	Map<String, List<HttpCookie>> getCookies();

	/**
	 * Return the remote address where this request is connected to, if
	 * available.
	 */
	@Nullable
	default InetSocketAddress getRemoteAddress() {
		return null;
	}

	/**
	 * Return the local address the request was accepted on, if available. 5.2.3
	 */
	@Nullable
	default InetSocketAddress getLocalAddress() {
		return null;
	}

	/**
	 * Represents an HTTP cookie as a name-value pair consistent with the
	 * content of the "Cookie" request header.
	 *
	 * @see <a href="https://tools.ietf.org/html/rfc6265">RFC 6265</a>
	 */
	public static class HttpCookie {

		private final String name;
		private final String value;

		//
		// Attributes encoded in the header's cookie fields.
		//

		// ;Comment=VALUE ... describes cookie's use ;Discard ... implied by
		// maxAge < 0
		private final String comment;
		// ;Domain=VALUE ... domain that sees cookie
		private final String domain;
		// ;Max-Age=VALUE ... cookies auto-expire
		private final int maxAge = -1;
		// ;Path=VALUE ... URLs that see the cookie
		private final String path;
		private final boolean secure; // ;Secure ... e.g. use SSL
		private final int version = 0; // ;Version=1 ... means RFC 2109++ style
		private final boolean isHttpOnly = false;
		@Nullable
		private final String sameSite;

		public HttpCookie(String name, @Nullable String value) {
			this(name, value, null, null, null, false, null);
		}

		public HttpCookie(String name, @Nullable String value, @Nullable String comment, @Nullable String domain,
				@Nullable String path, boolean secure, @Nullable String sameSite) {
			hasText(name, "'name' is required and must not be empty.");
			this.name = name;
			this.value = (value != null ? value : "");
			this.comment = comment;
			this.domain = domain;
			this.path = path;
			this.secure = secure;
			this.sameSite = sameSite;
		}

		/**
		 * Return the cookie name.
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Return the cookie value or an empty string (never {@code null}).
		 */
		public String getValue() {
			return this.value;
		}

		public String getComment() {
			return comment;
		}

		public String getDomain() {
			return domain;
		}

		public int getMaxAge() {
			return maxAge;
		}

		public String getPath() {
			return path;
		}

		public boolean isSecure() {
			return secure;
		}

		public int getVersion() {
			return version;
		}

		public boolean isHttpOnly() {
			return isHttpOnly;
		}

		public String getSameSite() {
			return sameSite;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof HttpCookie)) {
				return false;
			}
			HttpCookie otherCookie = (HttpCookie) other;
			return (this.name.equalsIgnoreCase(otherCookie.getName()));
		}

		@Override
		public String toString() {
			return this.name + '=' + this.value;
		}

	}

}
