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
public class LoggingProperties {
    // If the mandatory switch is not set, it is determined whether to enable
    // logging according to the preference switch, otherwise it is determined
    // whether to enable logging according to the mandatory switch, the default
    // mandatory switch is empty, the preference switch is enabled.
    private Boolean requiredPrintFlightLogging = null; //
    private boolean preferredPrintFlightLogging = true;
    private String preferredPrintFlightHeaderName = "X-Iam-Gateway-Logging-Enabled";
}
