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

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
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
    private @Nullable Boolean requiredFlightLogEnabled = null;

    /**
     * The output level of flight log printing, similar to
     * {@linkplain https://github.com/kubernetes/kubectl} design value range:
     * 1-10, 1 is coarse-grained log, 10 is the most fine-grained log.
     */
    private int preferredFlightLogVerboseLevel = 1;

    /**
     * Preferred to enable print flight log match group list, multiple match
     * groups are combined with OR for final result.
     */
    private List<PreferredFlightLogMatch> preferredFlightLogMatches = new ArrayList<>();

    @Getter
    @Setter
    @ToString
    public static class PreferredFlightLogMatch {

        /**
         * (Optional) The value used to match the current request HTTP schema.
         * </br>
         * for example: https://
         */
        private @Nullable String matchHttpSchema;

        /**
         * (Optional)The value used to match the current request HTTP method.
         * </br>
         * for example: POST
         */
        private @Nullable String matchHttpMethod;

        /**
         * (Optional) The value used to match the current request HTTP host.
         * </br>
         * for example: example.com
         */
        private @Nullable String matchHttpHost;

        /**
         * (Optional)The name used to match the current request HTTP port. </br>
         * for example: 443
         */
        private @Nullable Integer matchHttpPort;

        /**
         * (Optional) The name-value used to match the current request HTTP
         * header.
         */
        private @Nullable MatchPredicate matchHttpHeader = new MatchPredicate(MatchMode.EQ, "X-Iam-Gateway-Log", "y");

        /**
         * (Optional) The name-value used to match the current request HTTP
         * query parameter. </br>
         * for example: __iam_gateway_log
         */
        private @Nullable MatchPredicate matchHttpQuery;

    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class MatchPredicate {

        /**
         * The matching mode used to match the current request parameter value.
         */
        private @NotNull MatchMode matchMode;

        /**
         * Object key.
         */
        private @NotBlank String key;

        /**
         * Object value.
         */
        private @NotBlank String value;

        public MatchPredicate() {
            this.matchMode = MatchMode.EQ;
        }

        public MatchPredicate validate() {
            notNull(matchMode, "matchMode is required");
            hasText(key, "key is required");
            hasText(value, "value is required");
            return this;
        }
    }

    @Getter
    @AllArgsConstructor
    public static enum MatchMode {
        EQ((v1, v2) -> StringUtils.equals(v1, v2)),

        IGNORECASE_EQ((v1, v2) -> StringUtils.equalsIgnoreCase(v1, v2)),

        PREFIX((v1, v2) -> StringUtils.startsWith(v1, v2)),

        IGNORECASE_PREFIX((v1, v2) -> StringUtils.endsWithIgnoreCase(v1, v2)),

        SUFFIX((v1, v2) -> StringUtils.endsWith(v1, v2)),

        IGNORECASE_SUFFIX((v1, v2) -> StringUtils.equalsIgnoreCase(v1, v2)),

        INCLUDE((v1, v2) -> StringUtils.contains(v1, v2)),

        IGNORECASE_INCLUDE((v1, v2) -> StringUtils.containsIgnoreCase(v1, v2));

        private final BiFunction<String, String, Boolean> function;
    }

}
