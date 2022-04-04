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

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

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

    /**
     * If the mandatory switch is not set, it is determined whether to enable
     * logging according to the preference switch, otherwise it is determined
     * whether to enable logging according to the mandatory switch, the default
     * mandatory switch is empty, the preference switch is enabled.
     */
    private @Nullable Boolean requiredFlightLogPrintEnabled = null;

    /**
     * Whether to enable the switch for printing flight logs, it only takes
     * effect when the 'requiredFlightLogPrintEnabled' switch is empty or true.
     */
    private boolean preferredFlightLogPrintEnabled = true;

    /**
     * If the header name of the switch first extracted from the request
     * headers.
     */
    private @NotBlank String preferredFlightLogEnableHeaderName = "X-Iam-Gateway-Logging-Enabled";

    /**
     * (Optional) If the switch first extracted from the request header is
     * empty, then the parameter name of the switch is obtained from the query
     * parameters.
     */
    private @Nullable String preferredFlightLogEnableFallbackQueryName = "__iam_gateway_log_enabled";

    /**
     * It is used to match the switch value extracted from the request header or
     * parameter. If it matches, it means that the flight log is enabled,
     * otherwise it is not enabled; this design is very flexible, for example,
     * only a certain user is allowed to print the flight log. (case
     * insensitive)
     */
    private String preferredFlightLogEnableMatchesValue = "y";

    /**
     * The output level of flight log printing, similar to
     * {@linkplain https://github.com/kubernetes/kubectl} design value range:
     * 1-10, 1 is coarse-grained log, 10 is the most fine-grained log.
     */
    private int preferredFlightLogPrintVerboseLevel = 1;

}
