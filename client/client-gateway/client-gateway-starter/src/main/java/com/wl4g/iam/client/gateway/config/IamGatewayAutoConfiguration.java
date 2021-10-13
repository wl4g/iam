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
package com.wl4g.iam.client.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.wl4g.iam.client.gateway.authc.SimpleAPIAuthenticatingFilter;
import com.wl4g.iam.client.gateway.console.ConsoleAutoConfiguration;
import com.wl4g.iam.client.gateway.loadbalance.config.LoadbalanceAutoConfiguration;
import com.wl4g.iam.client.gateway.ratelimit.config.RateLimiterAutoConfiguration;
import com.wl4g.iam.client.gateway.route.config.RouteAutoConfiguration;

/**
 * IAM gateway auto configuration.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018年9月16日
 * @since
 */
@Configuration
@Import({ ConsoleAutoConfiguration.class, RouteAutoConfiguration.class, RateLimiterAutoConfiguration.class,
        LoadbalanceAutoConfiguration.class })
public class IamGatewayAutoConfiguration {

    @Bean
    public SimpleAPIAuthenticatingFilter tmpStandardApiAuthenticaingFilter() {
        return new SimpleAPIAuthenticatingFilter();
    }

}