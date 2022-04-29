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
package com.wl4g.iam.gateway.traffic.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.traffic.TrafficReplicationFilterFactory;

/**
 * {@link TrafficAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-26 v3.0.0
 * @since v3.0.0
 */
public class TrafficAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_TRAFFIC)
    public TrafficProperties trafficProperties() {
        return new TrafficProperties();
    }

    @Bean
    public TrafficReplicationFilterFactory trafficReplicationFilterFactory(
            TrafficProperties trafficConfig,
            ObjectProvider<List<HttpHeadersFilter>> headersFilters,
            List<HttpClientCustomizer> customizers) {
        return new TrafficReplicationFilterFactory(trafficConfig, headersFilters, customizers);
    }

}
