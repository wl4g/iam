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
package com.wl4g.iam.gateway.logging.config;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

import java.util.function.BiFunction;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link LoggingProperties}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
public class LoggingProperties implements InitializingBean {

    /**
     * If the mandatory switch is not set, it is determined whether to enable
     * logging according to the preference switch, otherwise it is determined
     * whether to enable logging according to the mandatory switch, the default
     * mandatory switch is empty, the preference switch is enabled.
     */
    private @Nullable Boolean requiredFlightLogEnabled = null;

    /**
     * Whether to enable the switch for printing flight logs, it only takes
     * effect when the 'requiredFlightLogEnabled' switch is empty or true.
     */
    private boolean preferredFlightLogPrintEnabled = true;

    /**
     * The matching pattern used to match the current request header or
     * parameter.
     */
    private @NotNull MatchesMode preferredFlightLogMatchesMode = MatchesMode.EQ;

    /**
     * If the current request header matches the header name and value, print
     * flight log is enabled.
     */
    private @Nullable String preferredFlightLogMatchesHeader = "X-Iam-Gateway-Log";

    /**
     * (Optional) If the current request query parameters matches the parameter
     * name and value, print flight log is enabled.
     */
    private @Nullable String preferredFlightLogMatchesQuery = "__iam_gateway_log";

    private @Nullable String preferredFlightLogMatchesValue = "y";

    /**
     * The output level of flight log printing, similar to
     * {@linkplain https://github.com/kubernetes/kubectl} design value range:
     * 1-10, 1 is coarse-grained log, 10 is the most fine-grained log.
     */
    private int preferredFlightLogVerboseLevel = 1;

    //
    // Temporary attributes.
    //
    @Setter(AccessLevel.NONE)
    private String _preferredFlightLogMatchesHeaderName;
    @Setter(AccessLevel.NONE)
    private String _preferredFlightLogMatchesHeaderValue;
    @Setter(AccessLevel.NONE)
    private String _preferredFlightLogMatchesQueryName;
    @Setter(AccessLevel.NONE)
    private String _preferredFlightLogMatchesQueryValue;

    @Override
    public void afterPropertiesSet() throws Exception {
        // Initial matches header.
        if (!isBlank(getPreferredFlightLogMatchesHeader())) {
            isTrueOf(contains(getPreferredFlightLogMatchesHeader(), "="), getPreferredFlightLogMatchesHeader());
            String[] parts1 = split(getPreferredFlightLogMatchesHeader(), "=");
            this._preferredFlightLogMatchesHeaderName = parts1[0];
            this._preferredFlightLogMatchesHeaderValue = parts1[1];
            hasText(_preferredFlightLogMatchesHeaderName,
                    "invalid_format '_preferredFlightLogMatchesHeader' missing '=' symbol.");
            hasText(_preferredFlightLogMatchesHeaderValue,
                    "invalid_format '_preferredFlightLogMatchesHeader' missing '=' symbol.");
        }
        // Initial matches query.
        if (!isBlank(getPreferredFlightLogMatchesHeader())) {
            isTrueOf(contains(getPreferredFlightLogMatchesHeader(), "="), getPreferredFlightLogMatchesQuery());
            String[] parts2 = split(getPreferredFlightLogMatchesQuery(), "=");
            this._preferredFlightLogMatchesQueryName = parts2[0];
            this._preferredFlightLogMatchesQueryValue = parts2[1];
            hasText(_preferredFlightLogMatchesQueryName, "invalid_format: 'preferredFlightLogMatchesQuery' missing '=' symbol.");
            hasText(_preferredFlightLogMatchesQueryValue, "invalid_format: 'preferredFlightLogMatchesQuery' missing '=' symbol.");
        }
    }

    @Getter
    @AllArgsConstructor
    public static enum MatchesMode {
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
