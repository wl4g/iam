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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ActivePingRecord;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.Stats;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * Reachable search strategy.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-15 v3.0.0
 * @since v3.0.0
 */
public interface ReachableStrategy {

    public static final ReachableStrategy DEFAULT = new LatestReachableStrategy();

    ServiceInstanceStatus calculateStatus(LoadBalancerProperties loadBalancerConfig, ServiceInstanceStatus status);

    @Slf4j
    class LatestReachableStrategy implements ReachableStrategy {

        private LatestReachableStrategy() {
        }

        @Override
        public ServiceInstanceStatus calculateStatus(LoadBalancerProperties loadBalancerConfig, ServiceInstanceStatus status) {
            // Calculate health statistics status.
            Stats stats = status.getStats();
            Queue<ActivePingRecord> queue = stats.getActivePingRecords();
            // TODO use least element.
            for (int i = 0, lastAvailableSize = queue.size() / 4; i < lastAvailableSize; i++) {
                ActivePingRecord ping = queue.peek();
                Boolean oldAlive = stats.getAlive();
                if (ping.getStatus() == HttpResponseStatus.OK && !ping.isTimeout()) {
                    // see:https://github.com/Netflix/ribbon/blob/v2.7.18/ribbon-httpclient/src/main/java/com/netflix/loadbalancer/PingUrl.java#L129
                    if (!isBlank(loadBalancerConfig.getStats().getExpectContent())) {
                        if (StringUtils.equals(loadBalancerConfig.getStats().getExpectContent(), ping.getResponseBody())) {
                            stats.setAlive(true);
                        }
                    } else {
                        stats.setAlive(true);
                    }
                } else {
                    stats.setAlive(false);
                }
                // see:https://github.com/Netflix/ribbon/blob/v2.7.18/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/BaseLoadBalancer.java#L696
                if (oldAlive != stats.getAlive()) {
                    log.warn("LoadBalancer server [{}/{}] status changed to {}", status.getInstance().getServiceId(),
                            status.getInstance().getInstanceId(), (stats.getAlive() ? "ALIVE" : "DEAD"));
                }
            }
            return status;
        }
    }

}
