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
package com.wl4g.iam.gateway.util.http;

import java.util.List;

import javax.annotation.Nullable;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.config.HttpClientFactory;
import org.springframework.cloud.gateway.config.HttpClientProperties;

import reactor.netty.http.client.HttpClient;

/**
 * {@link ReactiveHttpClientBuilder}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-29 v3.0.0
 * @since v3.0.0
 */
public abstract class ReactiveHttpClientBuilder {

    /**
     * Build a reactive based http client.
     * 
     * @see refer to:
     *      {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration.NettyConfiguration#gatewayHttpClient}
     */
    public static HttpClient build(
            HttpClientProperties httpCilentProperties,
            ServerProperties serverProperties,
            @Nullable List<HttpClientCustomizer> customizers) {
        try {
            HttpClientFactory factory = new HttpClientFactory(httpCilentProperties, serverProperties, customizers);
            factory.setSingleton(false);
            return factory.getObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Build a reactive based http client.
     * 
     * @see refer to:
     *      {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration.NettyConfiguration#gatewayHttpClient}
     */
    public static HttpClient build(HttpClientProperties httpCilentProperties, @Nullable List<HttpClientCustomizer> customizers) {
        return build(httpCilentProperties, new ServerProperties(), customizers);
    }

}
