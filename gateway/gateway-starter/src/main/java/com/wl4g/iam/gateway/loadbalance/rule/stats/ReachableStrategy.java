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
package com.wl4g.iam.gateway.loadbalance.rule.stats;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.lang3.StringUtils;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ActivePing;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.Stats;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * Reachable search strategy.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-15 v3.0.0
 * @since v3.0.0
 */
public interface ReachableStrategy {

    public SmartLogger log = getLogger(ReachableStrategy.class);

    public static final ReachableStrategy DEFAULT = new ReachableStrategy() {
        @Override
        public ServiceInstanceStatus updateStatus(LoadBalancerProperties loadBalancerConfig, ServiceInstanceStatus status) {
            // Calculate health statistics status.
            Stats stats = status.getStats();
            ActivePing ping = stats.getActivePings().peekLast();
            if (ping.isTimeout()) {
                stats.setAlive(false);
                return status;
            }

            Boolean oldAlive = stats.getAlive();
            // see:https://github.com/Netflix/ribbon/blob/v2.7.18/ribbon-httpclient/src/main/java/com/netflix/loadbalancer/PingUrl.java#L129
            if (!isBlank(loadBalancerConfig.getPing().getExpectBody())) {
                if (StringUtils.equals(loadBalancerConfig.getPing().getExpectBody(), ping.getResponseBody())) {
                    stats.setAlive(true);
                } else {
                    stats.setAlive(false);
                }
            } else {
                if (ping.getStatus().code() == loadBalancerConfig.getPing().getExpectStatus()) {
                    stats.setAlive(true);
                } else {
                    stats.setAlive(false);
                }
            }

            // see:https://github.com/Netflix/ribbon/blob/v2.7.18/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/BaseLoadBalancer.java#L696
            if (oldAlive != stats.getAlive()) {
                log.warn("LoadBalancer server [{}/{}] status changed to {}", status.getInstance().getServiceId(),
                        status.getInstance().getInstanceId(), (stats.getAlive() ? "ALIVE" : "DEAD"));
            }

            return status;
        }
    };

    ServiceInstanceStatus updateStatus(LoadBalancerProperties loadBalancerConfig, ServiceInstanceStatus status);

}
