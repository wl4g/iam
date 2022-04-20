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
package com.wl4g.iam.gateway.requestlimit.key;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;

import reactor.core.publisher.Mono;

/**
 * {@link HeaderIamKeyResolver}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-30 v1.0.0
 * @since v1.0.0
 */
public class HeaderIamKeyResolver implements IamKeyResolver {

    private @Autowired IamRequestLimiterProperties rateLimiterConfig;

    @Override
    public KeyResolverType kind() {
        return KeyResolverType.HEADER;
    }

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        notNullOf(headers, "requestHeaders");

        String host = null;
        for (String header : rateLimiterConfig.getRateLimitConfig().getDefaultHeaderKeyResolverNames()) {
            host = headers.getFirst(header);
            if (!isBlank(host) && !"Unknown".equalsIgnoreCase(host)) {
                break;
            }
        }
        // Fall-back
        if (isBlank(host)) {
            host = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

}