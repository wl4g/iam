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

import java.util.List;
import java.util.Map;

import com.wl4g.infra.common.annotation.Nullable;
import com.wl4g.infra.common.remoting.standard.HttpStatus;
import com.wl4g.iam.client.core.HttpIamRequest.HttpCookie;

/**
 * {@link HttpIamResponse}
 *
 * @author James Wong <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-03
 * @since
 */
public interface HttpIamResponse {

	/**
	 * Return the headers of this message.
	 * 
	 * @return a corresponding HttpHeaders object (never {@code null})
	 */
	Map<String, List<String>> getHeaders();

	/**
	 * Set the HTTP status code of the response.
	 * 
	 * @param status
	 *            the HTTP status as an {@link HttpStatus} enum value
	 * @return {@code false} if the status code has not been set because the
	 *         HTTP response is already committed, {@code true} if successfully
	 *         set.
	 */
	boolean setStatusCode(@Nullable HttpStatus status);

	/**
	 * Return the status code set via {@link #setStatusCode}, or if the status
	 * has not been set, return the default status code from the underlying
	 * server response. The return value may be {@code null} if the status code
	 * value is outside the {@link HttpStatus} enum range, or if the underlying
	 * server response does not have a default value.
	 */
	@Nullable
	HttpStatus getStatusCode();

	/**
	 * Return a mutable map with the cookies to send to the server.
	 */
	Map<String, List<HttpCookie>> getCookies();

	/**
	 * Add the given {@code ResponseCookie}.
	 * 
	 * @param cookie
	 *            the cookie to add
	 * @throws IllegalStateException
	 *             if the response has already been committed
	 */
	void addCookie(HttpCookie cookie);

	/**
	 * Whether the HttpOutputMessage is committed.
	 */
	boolean isCommitted();

	/**
	 * To write the body of the HttpOutputMessage to the underlying HTTP layer.
	 * 
	 * @param body
	 *            the body content publisher
	 */
	Object write(Object body);

	/**
	 * To write the body of the HttpOutputMessage to the underlying HTTP layer
	 * and flushing.
	 * 
	 * @param body
	 *            the body content publisher
	 */
	Object writeAndFlushWith(Object body);

}
