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
package com.wl4g.iam.gateway.circuitbreaker;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wl4g.infra.common.web.rest.RespBase;

import lombok.CustomLog;
import reactor.core.publisher.Mono;

/**
 * {@link DefaultFallbackController}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-16 v3.0.0
 * @since v3.0.0
 */
@CustomLog
@RestController
@SuppressWarnings("unchecked")
public class DefaultFallbackController {

    public static final String URI_FALLBACK_PATH = "/_fallback";

    @RequestMapping(path = { URI_FALLBACK_PATH }, method = { HEAD, GET, POST, PUT, DELETE })
    public Mono<RespBase<?>> doGetFallback(ServerHttpRequest request) {
        log.warn("called:fallback '{}' from '{}', method={}, headers={}, queryParameters={}", () -> URI_FALLBACK_PATH,
                () -> request.getRemoteAddress().getHostString(), () -> request.getMethod(), () -> request.getHeaders(),
                () -> request.getQueryParams());

        return Mono.justOrEmpty(RespBase.create().withMessage("This is fallback respnose !"));
    }

}
