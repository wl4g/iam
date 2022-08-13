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
package com.wl4g.iam.gateway.loadbalance.stats;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties.ProbeProperties;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.ActiveProbe;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.Stats;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * Reachable search strategy.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-15 v3.0.0
 * @since v3.0.0
 */
public interface ReachableStrategy {

    public SmartLogger log = getLogger(ReachableStrategy.class);

    InstanceStatus updateStatus(ProbeProperties probe, InstanceStatus status);

    public static class DefaultLatestReachableStrategy implements ReachableStrategy {
        private @Autowired IamGatewayMetricsFacade metricsFacade;

        @Override
        public InstanceStatus updateStatus(ProbeProperties probe, InstanceStatus status) {
            // Calculate health statistics status.
            Stats stats = status.getStats();
            ActiveProbe activeProbe = stats.getActiveProbes().peekLast();
            if (activeProbe.isTimeout()) {
                stats.setAlive(false);
                return status;
            }

            Boolean oldAlive = stats.getAlive();
            // see:https://github.com/Netflix/ribbon/blob/v2.7.18/ribbon-httpclient/src/main/java/com/netflix/loadbalancer/PingUrl.java#L129
            if (!isBlank(probe.getExpectBody())) {
                stats.setAlive(StringUtils.equals(probe.getExpectBody(), activeProbe.getResponseBody()));
            } else {
                if (isNull(activeProbe.getResponseStatus())) {
                    stats.setAlive(nonNull(activeProbe.getErrorOrCancel()) && !activeProbe.getErrorOrCancel());
                } else {
                    stats.setAlive(safeList(probe.getExpectStatuses()).contains(activeProbe.getResponseStatus().code()));
                }
            }

            // see:https://github.com/Netflix/ribbon/blob/v2.7.18/ribbon-loadbalancer/src/main/java/com/netflix/loadbalancer/BaseLoadBalancer.java#L696
            if (oldAlive != stats.getAlive()) {
                log.warn("Canary loadBalancer upstream server({}->{}) status changed to {}", status.getInstance().getServiceId(),
                        LoadBalancerUtil.getInstanceId(status.getInstance()), (stats.getAlive() ? "ALIVE" : "DEAD"));
                metricsFacade.counter(MetricsName.CANARY_LB_STATS_INSTANCE_STATE_CHANGED_TOTAL, 1, MetricsTag.LB_SERVICE_ID,
                        status.getInstance().getServiceId(), MetricsTag.LB_INSTANCE_ID,
                        LoadBalancerUtil.getInstanceId(status.getInstance()));
            }

            return status;
        }
    }

}
