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
package com.wl4g.iam.gateway.loadbalancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.AbstractCanaryLoadBalancerRule;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;

/**
 * {@link CannaryLoadBalancerRuleTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-13 v3.0.0
 * @since v3.0.0
 */
public class CannaryLoadBalancerRuleTests {

    @Test
    public void testSuccessMatchedHeaderWithFindCandidateInstances() {
        List<String> matchedRuleNames = new ArrayList<>();
        matchedRuleNames.add("v1-canary-header");

        List<ServiceInstance> candidates = doOnlyFindCandidateInstances(matchedRuleNames);
        System.out.println(candidates);

        assert candidates.size() == 1;
    }

    @Test
    public void testSuccessMatchedQueryWithFindCandidateInstances() {
        List<String> matchedRuleNames = new ArrayList<>();
        matchedRuleNames.add("v1-canary-query");

        List<ServiceInstance> candidates = doOnlyFindCandidateInstances(matchedRuleNames);
        System.out.println(candidates);

        assert candidates.size() == 1;
    }

    @Test
    public void testFailNoMatchedWithFindCandidateInstances() {
        List<String> matchedRuleNames = new ArrayList<>();

        List<ServiceInstance> candidates = doOnlyFindCandidateInstances(matchedRuleNames);

        assert candidates.size() == 0;
    }

    private List<ServiceInstance> doOnlyFindCandidateInstances(List<String> matchedRuleNames) {
        CanaryLoadBalancerProperties config = new CanaryLoadBalancerProperties();
        config.setFallbackAllToCandidates(true);
        config.setCanaryDiscoveryServiceLabelPrefix("Iscg-Canary-Label");

        AbstractCanaryLoadBalancerRule rule = new AbstractCanaryLoadBalancerRule(config, new SimpleDiscoveryClient(null)) {
            @Override
            protected ServiceInstance doChooseInstance(
                    ServerWebExchange exchange,
                    LoadBalancerStats stats,
                    String serviceId,
                    List<ServiceInstance> candidateInstances) {
                return null; // Ignore
            }

            @Override
            public LoadBalancerAlgorithm kind() {
                return null; // Ignore
            }
        };

        List<ServiceInstance> instances = new ArrayList<>();
        // instance1
        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("Iscg-Canary-Label-Header", "v1-canary-header");
        metadata1.put("Iscg-Canary-Label-Query", "v1-canary-query");
        instances.add(new DefaultServiceInstance("n1.portal.wl4g.io:8080", "portal-service", "n1.portal.wl4g.io", 8080, false,
                metadata1));
        // instance2
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("Iscg-Canary-Label-Header", "v2-canary-header");
        metadata2.put("Iscg-Canary-Label-Query", "v2-canary-query");
        instances.add(new DefaultServiceInstance("n2.portal.wl4g.io:8080", "portal-service", "n2.portal.wl4g.io", 8080, false,
                metadata2));

        List<ServiceInstance> candidates = rule.findCandidateInstances(instances, matchedRuleNames);
        System.out.println(candidates);

        return candidates;
    }

}
