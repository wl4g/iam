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
 * @version 2022-04-02 v3.0.0
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
     * Match whether the rule definition for a canary request is satisfied.
     */
    private List<MatchHttpRequestRule> matchRuleDefinitions = new ArrayList<>();

    /**
     * SPEL expressions that match canary requests.
     */
    private String selectExpression;

    /**
     * When no canary condition is matched, whether all instances of the service
     * are candidates.
     */
    private boolean fallbackAllToCandidates = true;

    /**
     * Health checking properties.
     */
    private StatsProperties stats = new StatsProperties();

    @Getter
    @Setter
    @ToString
    public static class StatsProperties {

        /**
         * Health checking on initial delay seconds.
         */
        private long initialDelaySeconds = 3;

        /**
         * Health checking on delay seconds. </br>
         * default refer to:
         * {@link com.netflix.loadbalancer.BaseLoadBalancer#pingIntervalSeconds}
         */
        private long delaySeconds = 10;

        /**
         * Expect ping response body.
         */
        private String expectContent;
    }

    public static final String DEFAULT_LB_CANARY_LABEL_KEY = "Iam-Gateway-Canary-Label";

}
