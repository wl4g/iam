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
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.infra.common.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link HeaderIamKeyResolver}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-30 v1.0.0
 * @since v1.0.0
 */
public class HeaderIamKeyResolver extends AbstractIamKeyResolver<HeaderIamKeyResolver.HeaderKeyResolverStrategy> {

    @Override
    public KeyResolverProvider kind() {
        return KeyResolverProvider.Header;
    }

    @Override
    public Mono<String> resolve(HeaderKeyResolverStrategy strategy, ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        notNullOf(headers, "requestHeaders");

        String host = null;
        for (String header : strategy.getHeaderNames()) {
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

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HeaderKeyResolverStrategy extends IamKeyResolver.KeyResolverStrategy {

        /**
         * The according to the list of header names of the request header
         * current limiter, it can usually be used to obtain the actual IP after
         * being forwarded by the proxy to limit the current, or it can be
         * flexibly used for other purposes.
         */
        private List<String> headerNames = DEFAULT_HEADER_NAMES;

        @Override
        public void applyDefaultIfNecessary(IamRequestLimiterProperties config) {
            List<String> defaultHeaderNames = config.getDefaultKeyResolver().getHeader().getHeaderNames();
            if (!isEqualCollection(defaultHeaderNames, getHeaderNames())) {
                setHeaderNames(defaultHeaderNames);
            }
        }
    }

    public static final List<String> DEFAULT_HEADER_NAMES = asList(WebUtils.HEADER_REAL_IP);

}