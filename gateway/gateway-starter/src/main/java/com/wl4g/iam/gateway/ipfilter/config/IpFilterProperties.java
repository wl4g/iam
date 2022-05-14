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

import static java.util.Arrays.asList;

import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.ipfilter.IpSubnetFilterFactory;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer.FilterStrategy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IpFilterProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@Validated
@ToString
public class IpFilterProperties {

    private String configPrefix = GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_IPFILTER_CONF;

    private DefaultFilterProperties defaultFilter = new DefaultFilterProperties();

    @Getter
    @Setter
    @Validated
    @ToString
    public static class DefaultFilterProperties extends IpSubnetFilterFactory.Config {
        private FilterStrategy strategy = FilterStrategy.builder().cidrs(asList("127.0.0.1")).build();
    }

}
