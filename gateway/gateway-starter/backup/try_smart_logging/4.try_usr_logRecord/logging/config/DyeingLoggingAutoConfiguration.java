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
package com.wl4g.iam.gateway.logging.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_LOGGING;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.gateway.logging.RequestLoggingGlobalFilter;
import com.wl4g.iam.gateway.logging.ResponseLoggingGlobalFilter;

/**
 * {@link DyeingLoggingAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 */
public class DyeingLoggingAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_GATEWAY_LOGGING)
    public DyeingLoggingProperties dyeingLoggingProperties() {
        return new DyeingLoggingProperties();
    }

    @Bean
    public RequestDyeingLoggingFilter requestDyeingLoggingFilter(DyeingLoggingProperties loggingConfig) {
        return new RequestDyeingLoggingFilter(loggingConfig);
    }

    @Bean
    public ResponseDyeingLoggingFilter responseDyeingLoggingFilter(DyeingLoggingProperties loggingConfig) {
        return new ResponseDyeingLoggingFilter(loggingConfig);
    }

}
