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
package com.wl4g.iam.gateway.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.auth.SignTokenAuthingFilter;

/**
 * {@link AuthingAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-01 v3.0.0
 * @since v3.0.0
 */
public class AuthingAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_AUTHING)
    public AuthingProperties authingProperties() {
        return new AuthingProperties();
    }

    @Bean
    public SignTokenAuthingFilter signTokenAuthingFilter(
            AuthingProperties authingConfig,
            StringRedisTemplate stringTemplate) {
        return new SignTokenAuthingFilter(authingConfig, stringTemplate);
    }

    // @Bean
    // public TokenRelayRefreshFilter
    // tokenRelayRefreshGatewayFilter(
    // ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
    // ReactiveClientRegistrationRepository clientRegistrationRepository) {
    // return new
    // TokenRelayRefreshFilterFactory(authorizedClientRepository,
    // clientRegistrationRepository);
    // }

}