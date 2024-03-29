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
package com.wl4g.iam.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.iam.common.constant.ServiceIAMConstants;

/**
 * {@link ServiceIamAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class ServiceIamAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = ServiceIAMConstants.CONF_PREFIX_IAM_SERVICE)
    public ServiceIamProperties serviceIamProperties() {
        return new ServiceIamProperties();
    }

}
