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
package com.wl4g.iam.gateway.ipfilter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.ipfilter.IpSubnetFilterFactory;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer;
import com.wl4g.iam.gateway.ipfilter.configurer.RedisIpFilterConfigurer;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;

/**
 * {@link IpFilterAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public class IpFilterAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_IPFILTER)
    public IpFilterProperties ipFilterProperties() {
        return new IpFilterProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public IpFilterConfigurer redisIpListConfigurer() {
        return new RedisIpFilterConfigurer();
    }

    @Bean
    public IpSubnetFilterFactory ipSubnetFilterFactory(
            IpFilterProperties ipListConfig,
            IpFilterConfigurer configurer,
            IamGatewayMetricsFacade metricsFacade) {
        return new IpSubnetFilterFactory(ipListConfig, configurer, metricsFacade);
    }

}
