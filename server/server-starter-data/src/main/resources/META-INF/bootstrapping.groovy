/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 *
 * Reference to website: http://wl4g.com
 */

import static com.wl4g.infra.common.lang.ClassUtils2.isPresent
import static org.springframework.boot.context.config.ConfigFileApplicationListener.*

import org.springframework.boot.Banner

import com.wl4g.infra.core.boot.listener.IBootstrappingConfigurer

/**
 * IAM data implementation of {@link IBootstrappingConfigurer}
 */
class IamDataBootstrappingConfigurer implements IBootstrappingConfigurer {

    @Override
    def int getOrder() {
        return -100
    }

    @Override
    void defaultProperties(Properties prevDefaultProperties) {
        // Preset spring.config.name
        // for example: spring auto load for 'application-dev.yml/application-data-dev.yml'
        def configName = new StringBuffer("application,iam-data,iam-data-etc")

        // Preset spring.config.additional-location
        def additionalLocation = new StringBuffer("classpath:/")

        // According to different heterogeneous runtime environments (multi RPC frameworks),
        // automatically identify and contain different configuration directories.
        if (isPresent("org.springframework.cloud.openfeign.FeignClient") && isPresent("org.springframework.cloud.openfeign.FeignAutoConfiguration")) {
            configName.append(",iam-data-scf");
            additionalLocation.append(",classpath:/scf/")
        } else if (isPresent("com.wl4g.infra.integration.feign.core.annotation.FeignConsumer")) {
            configName.append(",iam-data-sbf");
            additionalLocation.append(",classpath:/sbf/")
        } else if (isPresent("com.alibaba.dubbo.rpc.Filter") && isPresent("com.alibaba.boot.dubbo.autoconfigure.DubboAutoConfiguration")) {
            configName.append(",iam-data-dubbo");
            additionalLocation.append(",classpath:/dubbo/")
        }

        // Preset 'spring.config.additional-location', external resources does not override resources in classpath.
        prevDefaultProperties.put(CONFIG_NAME_PROPERTY, configName.toString())
        prevDefaultProperties.put(CONFIG_ADDITIONAL_LOCATION_PROPERTY, additionalLocation.toString())
    }

}