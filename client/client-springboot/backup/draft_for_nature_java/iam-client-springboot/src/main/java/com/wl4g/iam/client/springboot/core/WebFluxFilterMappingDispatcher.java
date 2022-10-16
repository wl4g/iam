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
package com.wl4g.iam.client.springboot.core;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;

import static org.springframework.http.HttpStatus.OK;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.iam.client.configure.IamConfigurer;
import com.wl4g.iam.client.core.AbstractMappingDispatcher;
import com.wl4g.iam.client.core.HttpIamRequest;
import com.wl4g.iam.client.core.HttpIamResponse;
import com.wl4g.iam.client.core.HttpIamRequest.HttpCookie;
import com.wl4g.iam.client.handler.WebIamHandler;

import static reactor.core.publisher.Flux.just;
import reactor.core.publisher.Mono;

/**
 * {@link WebFluxFilterMappingDispatcher}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020-09-03
 * @since
 */
public class WebFluxFilterMappingDispatcher extends AbstractMappingDispatcher implements WebFilter {

	public WebFluxFilterMappingDispatcher(IamConfigurer configurer, List<WebIamHandler> handlers) {
		super(configurer, handlers);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		Throwable th = null;

		try {
			final boolean isContinue = doAuthenticatingHandlers(new WebFluxHttpIamRequest(exchange.getRequest()),
					new WebFluxHttpIamResponse(exchange.getResponse()));

			if (isContinue) {
				// do next
				return chain.filter(exchange);
			}

		} catch (Exception e) {
			log.error("", e);
			th = e;
		}

		// Rejected response
		RespBase<?> resp = RespBase.create();
		if (nonNull(th)) {
			resp.handleError(th);
		} else {
			resp.setMessage("Authenticity not defined");
		}
		ServerHttpResponse response = exchange.getResponse();
		DataBuffer buffer = response.bufferFactory().wrap(resp.asJson().getBytes(UTF_8));
		response.setStatusCode(OK);
		return response.writeWith(just(buffer));
	}

	/**
	 * {@link WebFluxHttpIamRequest}
	 * 
	 * @since
	 */
	public static class WebFluxHttpIamRequest implements HttpIamRequest {

		/** {@link ServerHttpRequest} */
		protected final ServerHttpRequest request;

		// --- Temporary fields. ---

		private transient Map<String, List<HttpCookie>> cookies;

		public WebFluxHttpIamRequest(ServerHttpRequest request) {
			notNullOf(request, "request");
			this.request = request;
		}

		@Override
		public Map<String, List<String>> getHeaders() {
			return request.getHeaders();
		}

		@Override
		public String getMethodValue() {
			return request.getMethodValue();
		}

		@Override
		public URI getURI() {
			return request.getURI();
		}

		@Override
		public String getId() {
			return request.getId();
		}

		@Override
		public String getContextPath() {
			return request.getPath().contextPath().value();
		}

		@Override
		public Map<String, List<String>> getQueryParams() {
			return request.getQueryParams();
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			return request.getRemoteAddress();
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			return request.getLocalAddress();
		}

		@Override
		public Map<String, List<HttpCookie>> getCookies() {
			if (isNull(this.cookies)) {
				synchronized (this) {
					if (isNull(this.cookies)) {
						this.cookies = safeMap(request.getCookies()).entrySet().stream()
								.map(e -> singletonMap(e.getKey(),
										safeList(e.getValue()).stream().map(c -> convertHttpCookie(c)).collect(toList())))
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
		private final HttpCookie convertHttpCookie(org.springframework.http.HttpCookie c) {
			return new HttpCookie(c.getName(), c.getValue());
		}

	}

	/**
	 * {@link WebFluxHttpIamResponse}
	 * 
	 * @since
	 */
	public static class WebFluxHttpIamResponse implements HttpIamResponse {

		/** {@link ServerHttpResponse} */
		protected final ServerHttpResponse response;

		//
		// --- Temporary fields. ---
		//

		private transient Map<String, List<HttpCookie>> cookies;

		public WebFluxHttpIamResponse(ServerHttpResponse response) {
			notNullOf(response, "response");
			this.response = response;
		}

		@Override
		public Map<String, List<String>> getHeaders() {
			return response.getHeaders();
		}

		@Override
		public boolean setStatusCode(com.wl4g.infra.common.remoting.standard.HttpStatus status) {
			return response.setStatusCode(org.springframework.http.HttpStatus.valueOf(status.value()));
		}

		@Override
		public com.wl4g.infra.common.remoting.standard.HttpStatus getStatusCode() {
			return com.wl4g.infra.common.remoting.standard.HttpStatus.valueOf(response.getStatusCode().value());
		}

		@Override
		public Map<String, List<HttpCookie>> getCookies() {
			if (isNull(this.cookies)) {
				synchronized (this) {
					if (isNull(this.cookies)) {
						this.cookies = safeMap(response.getCookies()).entrySet().stream()
								.map(e -> singletonMap(e.getKey(),
										safeList(e.getValue()).stream().map(c -> new HttpCookie(c.getName(), c.getValue()))
												.collect(toList())))
								.flatMap(e -> e.entrySet().stream()).collect(toMap(e -> e.getKey(), e -> e.getValue()));
					}
				}
			}
			return this.cookies;
		}

		@Override
		public void addCookie(HttpCookie c) {
			response.addCookie(ResponseCookie.from(c.getName(), c.getValue()).domain(c.getDomain()).maxAge(c.getMaxAge())
					.httpOnly(c.isHttpOnly()).path(c.getPath()).secure(c.isSecure()).sameSite(c.getSameSite()).build());
		}

		@Override
		public boolean isCommitted() {
			return response.isCommitted();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object write(Object body) {
			if (body instanceof Publisher) {
				return response.writeWith((Publisher<? extends DataBuffer>) body);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object writeAndFlushWith(Object body) {
			if (body instanceof Publisher) {
				return response.writeAndFlushWith((Publisher<? extends Publisher<? extends DataBuffer>>) body);
			}
			throw new UnsupportedOperationException();
		}

	}

}
