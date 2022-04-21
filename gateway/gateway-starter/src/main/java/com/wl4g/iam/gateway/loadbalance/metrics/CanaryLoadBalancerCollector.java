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
package com.wl4g.iam.gateway.loadbalance.metrics;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

/**
 * {@link CanaryLoadBalancerCollector}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration}
 */
public class CanaryLoadBalancerCollector extends Collector {

    private @Autowired CanaryLoadBalancerProperties loadBalancerConfig;
    private @Autowired LoadBalancerStats loadBalancerStats;
    private @Autowired IamGatewayMetricsFacade metricsFacade;

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new LinkedList<>();

        // Instance current alive of status.
        GaugeMetricFamily aliveGauge = metricsFacade.createGauge(MetricsName.CANARY_LB_STATS_CURRENT_INSTANCE,
                MetricsTag.ROUTE_ID, MetricsTag.LB_SERVICE_ID, MetricsTag.LB_INSTANCE_ID);
        result.add(aliveGauge);

        loadBalancerStats.getAllRouteServices().forEach((routeId, routeService) -> {
            safeMap(routeService.getInstances()).forEach((instanceId, i) -> {
                List<String> values = Lists.newArrayList();
                values.add(routeId);
                values.add(i.getInstance().getServiceId());
                values.add(i.getInstance().getInstanceId());
                // TODO use routeId config.choose.isNullProbeToReacable
                boolean isAlive = LoadBalancerUtil.isAlive(loadBalancerConfig.getDefaultChoose(), i.getStats());
                aliveGauge.addMetric(values, isAlive ? 1 : 0);
            });
        });

        // Instance current connections.
        GaugeMetricFamily connectionGauge = metricsFacade.createGauge(MetricsName.CANARY_LB_STATS_CURRENT_INSTANCE_CONNECTIONS,
                MetricsTag.ROUTE_ID, MetricsTag.LB_SERVICE_ID, MetricsTag.LB_INSTANCE_ID);
        result.add(connectionGauge);

        loadBalancerStats.getAllRouteServices().forEach((routeId, routeService) -> {
            safeMap(routeService.getInstances()).forEach((instanceId, i) -> {
                List<String> values = Lists.newArrayList();
                values.add(routeId);
                values.add(i.getInstance().getServiceId());
                values.add(i.getInstance().getInstanceId());
                connectionGauge.addMetric(values, i.getStats().getConnections().get());
            });
        });

        return result;
    }

}
