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
package com.wl4g.iam.gateway.loadbalance.config;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.wl4g.iam.gateway.loadbalance.chooser.CanaryLoadBalancerChooser.LoadBalancerAlgorithm;
import com.wl4g.infra.context.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link CanaryLoadBalancerProperties}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.config.LoadBalancerProperties}
 */
@Getter
@Setter
@ToString
public class CanaryLoadBalancerProperties {

    public static final CanaryLoadBalancerProperties DEFAULT = new CanaryLoadBalancerProperties();

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
     * The SPEL expressions that match canary requests.
     */
    private String canaryChooseExpression;

    /**
     * Match whether the rule definition for a canary request is satisfied.
     */
    private List<MatchHttpRequestRule> canaryMatchRuleDefinitions = new ArrayList<>();

    /**
     * The number of load balancer statistician scheduler worker threads.
     */
    private int statsSchedulerThread = 2;

    /**
     * The initial interval at which the instance list is periodically pulled
     * update register from the discovery service.
     */
    private int registerRouteServicesInitialSeconds = 1;

    /**
     * The interval at which the instance list is periodically pulled update
     * register from the discovery server.
     */
    private int registerRouteServicesDelaySeconds = 60;

    /**
     * LoadBalancer defaultChoose properties.
     */
    private ChooseProperties defaultChoose = new ChooseProperties();

    /**
     * Health defaultProbe properties.
     */
    private ProbeProperties defaultProbe = new ProbeProperties();

    @Getter
    @Setter
    @ToString
    public static class ChooseProperties {

        /**
         * When no canary condition is matched, whether all instances of the
         * service are candidates.
         */
        private boolean fallbackAllToCandidates = true;

        /**
         * Load balancer algorithm.
         */
        private LoadBalancerAlgorithm loadBalancerAlgorithm = LoadBalancerAlgorithm.R;

        /**
         * The number of best-effort attempts to select an instance.
         */
        private int maxChooseTries = 10;

        /**
         * Whether to consider these instance reachable by default when there is
         * not enough defaultProbe data (such as just started up).
         */
        private boolean nullPingToReachable = true;

    }

    @Getter
    @Setter
    @ToString
    public static class ProbeProperties {

        /** Enables wiretap debugging for Netty HttpClient. */
        private boolean wiretap;

        /**
         * Ping request on initial delay seconds.
         */
        private long initialSeconds = 3;

        /**
         * Ping request on delay seconds. </br>
         * default refer to:
         * {@link com.netflix.loadbalancer.BaseLoadBalancer#pingIntervalSeconds}
         */
        private long delaySeconds = 10;

        /**
         * Ping request timeout mills.
         */
        private long timeoutMs = 5_000;

        /**
         * Ping request path.
         */
        private String path = "/healthz";

        /**
         * Ping expect response status codes. (As long as any status code is
         * satisfied) </br>
         * Note: only {@link #expectBody} takes effect when it is set at the
         * same time as {@link #expectBody}.
         */
        private List<Integer> expectStatuses = asList(200);

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

    public static final String DEFAULT_LB_CANARY_LABEL_KEY = "Iscg-Canary-Label";

}
