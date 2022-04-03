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
package com.wl4g.iam.gateway.loadbalance.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link LoggingProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
public class LoadBalancerProperties {
    private String versionGrayLoadBalanceMetadataKey = DEFAULT_VERSION_GRAY_LB_METADATA_KEY;
    private String versionGrayLoadBalanceRequestHeader = DEFAULT_VERSION_GRAY_LB_HEADER;
    // Default definitions.
    public static final String DEFAULT_VERSION_GRAY_LB_METADATA_KEY = "Iam-Gateway-Gray-Version";
    public static final String DEFAULT_VERSION_GRAY_LB_HEADER = "X-".concat(DEFAULT_VERSION_GRAY_LB_METADATA_KEY);
}
