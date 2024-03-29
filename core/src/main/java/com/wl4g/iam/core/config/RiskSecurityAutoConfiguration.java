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
package com.wl4g.iam.core.config;

import static com.wl4g.iam.core.config.RiskSecurityProperties.KEY_RISK_PREFIX;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.core.risk.SimpleRequestIpRiskSecurityHandler;

/**
 * Risk security auto configuration.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-06-14 v1.0.0
 * @see v1.0.0
 */
public class RiskSecurityAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = KEY_RISK_PREFIX + ".enabled", matchIfMissing = true)
    @ConfigurationProperties(prefix = KEY_RISK_PREFIX)
    public RiskSecurityProperties riskSecurityProperties() {
        return new RiskSecurityProperties();
    }

    @Bean
    @ConditionalOnBean(RiskSecurityProperties.class)
    public SimpleRequestIpRiskSecurityHandler simpleRequestIpRiskSecurityHandler() {
        return new SimpleRequestIpRiskSecurityHandler();
    }

}