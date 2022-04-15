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

import java.util.ArrayList;
import java.util.List;

import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link LoadBalancerProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
public class LoadBalancerProperties extends org.springframework.cloud.gateway.config.LoadBalancerProperties {

    /**
     * Enabled status.
     */
    private boolean enabled = true;

    /**
     * The tag key used to get whether the discovery service satisfies the
     * canary traffic group.
     */
    private String canaryDiscoveryServiceLabelPrefix = DEFAULT_LB_CANARY_LABEL_KEY;

    /**
     * SPEL expressions that match canary requests.
     */
    private String selectExpression;

    /**
     * Match whether the rule definition for a canary request is satisfied.
     */
    private List<MatchHttpRequestRule> matchRuleDefinitions = new ArrayList<>();

    /**
     * When no canary condition is matched, whether all instances of the service
     * are candidates.
     */
    private boolean fallbackAllToCandidates = true;

    /**
     * The number of best-effort attempts to select an instance.
     */
    private int maxChooseTries = 10;

    /**
     * Health checking properties.
     */
    private PingProperties ping = new PingProperties();

    @Getter
    @Setter
    @ToString
    public static class PingProperties {

        /**
         * Ping request debug enabled.
         */
        private boolean debug = false;

        /**
         * Ping request on initial delay seconds.
         */
        private long initialMs = 3;

        /**
         * Ping request on delay seconds. </br>
         * default refer to:
         * {@link com.netflix.loadbalancer.BaseLoadBalancer#pingIntervalSeconds}
         */
        private long delayMs = 10;

        /**
         * Ping request timeout mills.
         */
        private long timeoutMs = 10_000;

        /**
         * Ping request path.
         */
        private String path = "/healthz";

        /**
         * Ping expect response status code. </br>
         * Note: only {@link #expectBody} takes effect when it is set at the
         * same time as {@link #expectBody}.
         */
        private int expectStatus = 200;

        /**
         * Ping expect response body. </br>
         * Note: only {@link #expectBody} takes effect when it is set at the
         * same time as {@link #expectStatus}.
         */
        private String expectBody;

        /**
         * Ping records cache queue size.
         */
        private int receiveQueue = 8;

    }

    public static final String DEFAULT_LB_CANARY_LABEL_KEY = "Iam-Gateway-Canary-Label";

}
