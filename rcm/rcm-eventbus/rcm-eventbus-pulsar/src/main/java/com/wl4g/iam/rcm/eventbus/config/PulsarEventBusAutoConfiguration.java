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
package com.wl4g.iam.rcm.eventbus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.common.constant.RcmIAMConstants;
import com.wl4g.iam.rcm.eventbus.IamEventBusService;
import com.wl4g.iam.rcm.eventbus.PulsarIamEventBusService;

/**
 * {@link PulsarEventBusAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-31 v3.0.0
 * @since v3.0.0
 */
public class PulsarEventBusAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = RcmIAMConstants.CONF_PREFIX_IAM_RCM_EVENTBUS_PULSAR)
    public PulsarEventBusProperties pulsarEventBusProperties() {
        return new PulsarEventBusProperties();
    }

    @Bean
    public IamEventBusService<?> kafkaIamEventBusService(
            IamEventBusProperties eventBusConfig,
            PulsarEventBusProperties pulsarEventConfig) {
        return new PulsarIamEventBusService(eventBusConfig, pulsarEventConfig);
    }

}
