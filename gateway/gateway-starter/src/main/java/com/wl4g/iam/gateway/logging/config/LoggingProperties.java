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

import java.util.ArrayList;
import java.util.List;

import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link LoggingMessageProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
public class LoggingProperties {

    /**
     * If this switch is enabled, then check whether the request header and
     * parameters meet the current conditions for printing flight logs. If so,
     * print according to the predetermined level, otherwise do not print. If
     * this switch is set to false, printing logs will be permanently disabled
     * (regardless of the request header). and whether the parameters meet the
     * conditions).
     */
    private boolean enabled = true;

    /**
     * The output default level of flight log printing, similar to
     * {@linkplain https://github.com/kubernetes/kubectl} design value range:
     * 1-10, 1 is coarse-grained log, 10 is the most fine-grained log.
     */
    private int defaultVerboseLevel = 1;

    /**
     * The request header name used to set the log output verbose level valid
     * only for the current request, This value takes precedence over
     * {@link #defaultVerboseLevel}
     */
    private String verboseLevelRequestHeader = "X-Iscg-Log-Level";

    /**
     * The status additional header name of the dyed log request, e.g which is
     * used to notify the back-end services to enable log printing for the
     * current request.
     */
    private String dyeingLogStateRequestHeader = "X-Iscg-Log-Dyeing-State";

    /**
     * In order to prevent the request body data from being too large, only the
     * first small part of the body data is obtained.
     */
    private int maxPrintRequestBodyLength = 1024;

    /**
     * In order to prevent the response body data from being too large, only the
     * first small part of the body data is obtained.
     */
    private int maxPrintResponseBodyLength = 1024;

    /**
     * Preferred to enable print logs match SPEL match expression. Default by
     * '#{true}', which means never match. </br>
     * </br>
     * Tip: The built-in support to get the current routeId, such as:
     * '#{routeId.get().test('my-service-route')}'
     */
    private String preferOpenMatchExpression = "#{true}";

    /**
     * Preferred to enable print logs match rule definitions.
     */
    private List<MatchHttpRequestRule> preferMatchRuleDefinitions = new ArrayList<>();

}
