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
package com.wl4g.iam.gateway.error;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.infra.common.web.rest.RespBase;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link SmartErrorUpstreamHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class SmartErrorUpstreamHandler implements ErrorWebExceptionHandler, Ordered {

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        RespBase<String> resp = RespBase.create();
        if (ex instanceof NotFoundException) {
            resp.setCode(HttpStatus.NOT_FOUND.value());
            resp.setMessage("Not found services");
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatus = (ResponseStatusException) ex;
            resp.setCode(responseStatus.getStatus().value());
            resp.setMessage(responseStatus.getMessage());
        } else {
            resp.setCode(HttpStatus.SERVICE_UNAVAILABLE.value());
            resp.setMessage("Unknown internal error");
        }
        log.warn("Failed to forward for '{}', cause by: {}", exchange.getRequest().getPath(), ex.getMessage());

        byte[] bytes = resp.asJson().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatusCode(HttpStatus.valueOf(resp.getCode()));
        return exchange.getResponse().writeWith(Flux.just(buffer));
    }

}