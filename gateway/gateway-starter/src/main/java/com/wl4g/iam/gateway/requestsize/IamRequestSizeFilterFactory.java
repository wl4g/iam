/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.iam.gateway.requestsize;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.requestsize.config.IamRequestSizeProperties;
import com.wl4g.iam.gateway.requestsize.config.IamRequestSizeProperties.RequestSizeProperties;
import com.wl4g.iam.gateway.util.IamGatewayUtil.SafeFilterOrdered;
import com.wl4g.infra.common.bean.ConfigBeanUtils;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * {@link IamRequestSizeFilterFactory}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-16 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.filter.factory.RequestSizeGatewayFilterFactory}
 */
public class IamRequestSizeFilterFactory extends AbstractGatewayFilterFactory<IamRequestSizeFilterFactory.Config> {

    private static String PREFIX = "kMGTPE";
    private static String ERROR = "Request size is larger than permissible limit."
            + " Request size is %s where permissible limit is %s";

    private final IamRequestSizeProperties requestSizeConfig;

    public IamRequestSizeFilterFactory(IamRequestSizeProperties requestSizeConfig) {
        super(IamRequestSizeFilterFactory.Config.class);
        this.requestSizeConfig = notNullOf(requestSizeConfig, "requestSizeConfig");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private static String getErrorMessage(Long currentRequestSize, Long maxSize) {
        return String.format(ERROR, getReadableByteCount(currentRequestSize), getReadableByteCount(maxSize));
    }

    private static String getReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = Character.toString(PREFIX.charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public GatewayFilter apply(IamRequestSizeFilterFactory.Config config) {
        applyDefaultToConfig(config);
        config.validate();
        return new IamRequestSizeGatewayFilter(config);
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new Config(), config, requestSizeConfig.getRequestSize());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Config extends RequestSizeProperties {
    }

    @AllArgsConstructor
    class IamRequestSizeGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_REQUEST_SIZE;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String contentLength = request.getHeaders().getFirst("content-length");
            if (!ObjectUtils.isEmpty(contentLength)) {
                Long currentRequestSize = Long.valueOf(contentLength);
                if (currentRequestSize > config.getMaxBodySize().toBytes()) {
                    exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
                    if (!exchange.getResponse().isCommitted()) {
                        exchange.getResponse().getHeaders().add("errorMessage",
                                getErrorMessage(currentRequestSize, config.getMaxBodySize().toBytes()));
                    }
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        }

        @Override
        public String toString() {
            return filterToStringCreator(IamRequestSizeFilterFactory.this).append("max", config.getMaxBodySize()).toString();
        }

    }

    public static final String BEAN_NAME = "IamRequestSize";

}
