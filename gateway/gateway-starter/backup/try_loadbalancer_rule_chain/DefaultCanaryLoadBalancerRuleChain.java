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
package com.wl4g.iam.gateway.loadbalance.chooser.chain;

import static java.util.Objects.nonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.server.reactive.ServerHttpRequest;

import com.wl4g.iam.gateway.loadbalance.chooser.CanaryLoadBalancerChooser;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.ServiceInstanceStatus;

import lombok.AllArgsConstructor;

/**
 * {@link DefaultCanaryLoadBalancerRuleChain}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-15 v3.0.0
 * @since v3.0.0
 */
@AllArgsConstructor
public class DefaultCanaryLoadBalancerRuleChain implements CanaryLoadBalancerRuleChain {

    private final List<CanaryLoadBalancerRule> rules;
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public List<ServiceInstanceStatus> choose(
            LoadBalancerStats stats,
            String serviceId,
            ServerHttpRequest request,
            List<ServiceInstanceStatus> prevAvailableInstances,
            CanaryLoadBalancerRuleChain chain) {

        if (nonNull(rules)) {
            int _index = index.getAndIncrement();
            if (_index < (rules.size() - 1)) {
                rules.get(_index).choose(stats, serviceId, request);
            }
        }
        return prevAvailableInstances;
    }

}
