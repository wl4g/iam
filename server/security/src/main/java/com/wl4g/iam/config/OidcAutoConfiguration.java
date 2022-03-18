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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.iam.common.constant.OidcIAMConstants;
import com.wl4g.iam.web.oidc.V1OidcServerAuthenticatingController;
import com.wl4g.infra.core.web.mapping.PrefixHandlerMappingSupport;

/**
 * IAM OIDC server auto configuration
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年3月25日
 * @since
 */
@Configuration
@ConditionalOnBean(IamServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter({ LoginAutoConfiguration.class })
public class OidcAutoConfiguration extends PrefixHandlerMappingSupport {

    @Bean
    public V1OidcServerAuthenticatingController v1OidcServerAuthenticatingController() {
        return new V1OidcServerAuthenticatingController();
    }

    @Bean
    public Object v1OidcServerAuthenticatingControllerPrefixHandlerMapping() {
        return super.newPrefixHandlerMapping(OidcIAMConstants.URI_IAM_OIDC_V1_SERVER,
                com.wl4g.iam.annotation.V1OidcServerController.class);
    }

}