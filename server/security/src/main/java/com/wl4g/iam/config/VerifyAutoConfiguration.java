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

import com.wl4g.infra.core.web.mapping.PrefixHandlerMappingSupport;
import com.wl4g.iam.annotation.VerifyAuthController;
import com.wl4g.iam.web.verifiy.VerificationController;

import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_VERIFY_BASE;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IAM authentication(verify) configuration
 *
 * @author James Wong <983708408@qq.com>
 * @version v1.0 2019年1月8日
 * @since
 */
@Configuration
@ConditionalOnBean(IamServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter({ LoginAutoConfiguration.class })
public class VerifyAutoConfiguration extends PrefixHandlerMappingSupport {

    @Bean
    public VerificationController verifyAuthenticationEndpoint() {
        return new VerificationController();
    }

    @Bean
    public Object verifyAuthenticationEndpointPrefixHandlerMapping() {
        return super.newPrefixHandlerMapping(URI_IAM_SERVER_VERIFY_BASE, VerifyAuthController.class);
    }

}