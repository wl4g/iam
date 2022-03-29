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
package com.wl4g.iam.config;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_PREFIX;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_PREFIX;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_REGISTRATION_PREFIX;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.handler.oidc.v1.DefaultV1OidcAuthingHandler;
import com.wl4g.iam.handler.oidc.v1.V1OidcAuthingHandler;
import com.wl4g.iam.web.oidc.v1.V1OidcCoreAuthingController;
import com.wl4g.iam.web.oidc.v1.V1OidcDiscoveryAuthingController;
import com.wl4g.iam.web.oidc.v1.V1OidcRegistrationAuthingController;
import com.wl4g.infra.core.web.mapping.PrefixHandlerMappingSupport;

/**
 * IAM V1-OIDC endpoint server auto configuration
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年3月25日
 * @since
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@Configuration
@ConditionalOnBean(IamServerMarkerConfiguration.Marker.class)
public class OidcAutoConfiguration extends PrefixHandlerMappingSupport {

    @Bean
    public V1OidcAuthingHandler defaultV1OidcAuthingHandler(IamProperties config) {
        return new DefaultV1OidcAuthingHandler(config);
    }

    // V1 OIDC Core.

    @Bean
    public V1OidcCoreAuthingController v1OidcCoreAuthingController() {
        return new V1OidcCoreAuthingController();
    }

    @Bean
    public Object v1OidcCoreAuthingControllerPrefixHandlerMapping() {
        return super.newPrefixHandlerMapping(URI_IAM_OIDC_ENDPOINT_CORE_PREFIX,
                com.wl4g.iam.annotation.V1OidcCoreController.class);
    }

    // V1 OIDC Discovery.

    @Bean
    public V1OidcDiscoveryAuthingController v1OidcDiscoveryAuthingController() {
        return new V1OidcDiscoveryAuthingController();
    }

    @Bean
    public Object v1OidcDiscoveryAuthingControllerPrefixHandlerMapping() {
        return super.newPrefixHandlerMapping(URI_IAM_OIDC_ENDPOINT_PREFIX,
                com.wl4g.iam.annotation.V1OidcDiscoveryController.class);
    }

    // V1 OIDC Dynamic Registration.

    @Bean
    public V1OidcRegistrationAuthingController v1OidcRegistrationAuthingController() {
        return new V1OidcRegistrationAuthingController();
    }

    @Bean
    public Object v1OidcRegistrationAuthingControllerPrefixHandlerMapping() {
        return super.newPrefixHandlerMapping(URI_IAM_OIDC_ENDPOINT_REGISTRATION_PREFIX,
                com.wl4g.iam.annotation.V1OidcRegistrationController.class);
    }

}