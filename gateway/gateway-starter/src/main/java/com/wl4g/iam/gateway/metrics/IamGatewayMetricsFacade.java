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
package com.wl4g.iam.gateway.metrics;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;

import java.util.List;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.collect.Lists;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link IamGatewayMetricsFacade}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-16 v3.0.0
 * @since v3.0.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class IamGatewayMetricsFacade {

    private final PrometheusMeterRegistry registry;

    public void counter(ServerWebExchange exchange, String metricsName, double amount, String... tags) {
        try {
            notNullOf(exchange, "exchange");
            hasTextOf(metricsName, "metricsName");
            Object route = exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (nonNull(route)) {
                String routeId = ((Route) route).getId();
                List<String> _tags = Lists.newArrayList(tags);
                _tags.add(MetricsTag.ROUTE_ID);
                _tags.add(routeId);
                registry.counter(metricsName, _tags.toArray(new String[0])).increment(amount);
            }
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}", metricsName, valueOf(amount)), e);
        }
    }

    public void counter(String metricsName, double amount, String... tags) {
        try {
            hasTextOf(metricsName, "metricsName");
            List<String> _tags = Lists.newArrayList(tags);
            registry.counter(metricsName, _tags.toArray(new String[0])).increment(amount);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}", metricsName, valueOf(amount)), e);
        }
    }

    public void counter(InstanceStatus status, String metricsName, double amount, String... tags) {
        try {
            notNullOf(status, "instanceStatus");
            notNullOf(status.getInstance(), "instanceStatus.instance");
            hasTextOf(metricsName, "metricsName");
            List<String> _tags = Lists.newArrayList(tags);
            _tags.add(MetricsTag.SERVICE_ID);
            _tags.add(status.getInstance().getServiceId());
            _tags.add(MetricsTag.INSTANCE_ID);
            _tags.add(LoadBalancerUtil.getInstanceId(status.getInstance()));
            registry.counter(metricsName, _tags.toArray(new String[0])).increment(amount);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}, instanceStatus: {}", metricsName,
                    valueOf(amount)), status, e);
        }
    }

    public static abstract class MetricsName {
        // Simple signature authorizer.
        public static final String SIMPLE_SIGN_BLOOM_TOTAL = "simple_sign_bloom_total";
        public static final String SIMPLE_SIGN_SUCCCESS_TOTAL = "simple_sign_success_total";
        public static final String SIMPLE_SIGN_FAIL_TOTAL = "simple_sign_fail_total";
        // Canary LoadBalacner chooser.
        public static final String CANARY_LB_CHOOSE_TOTAL = "canary_lb_choose_total";
        public static final String CANARY_LB_CHOOSE_FALLBACK_TOTAL = "canary_lb_choose_fallback_total";
        public static final String CANARY_LB_CHOOSE_MISSING_TOTAL = "canary_lb_choose_missing_total";
        public static final String CANARY_LB_CHOOSE_MAX_TRIES_TOTAL = "canary_lb_choose_max_tries_total";
        public static final String CANARY_LB_CHOOSE_EMPTY_INSTANCES_TOTAL = "canary_lb_choose_empty_instances_total";
        // Canary LoadBalacnerStats Active probe.
        public static final String CANARY_LB_STATS_TOTAL = "canary_lb_stats_total";
        public static final String CANARY_LB_STATS_TIMEOUT_TOTAL = "canary_lb_stats_timeout_total";
        public static final String CANARY_LB_STATS_CANCEL_ERROR_TOTAL = "canary_lb_stats_cancel_error_total";
        // Canary LoadBalacnerStats Passive probe.
        public static final String CANARY_LB_STATS_CONNECT_OPEN_TOTAL = "canary_lb_stats_connect_open_total";
        public static final String CANARY_LB_STATS_CONNECT_CLOSE_TOTAL = "canary_lb_stats_connect_close_total";
        // Canary LoadBalacnerStats Register.
        public static final String CANARY_LB_STATS_REGISTER_ALL_SERVICES_TOTAL = "canary_lb_stats_register_all_services_total";
        public static final String CANARY_LB_STATS_RESTART_PROBE_TASK_TOTAL = "canary_lb_stats_restart_probe_task_total";
        public static final String CANARY_LB_STATS_RESTART_PROBE_TASK_FAIL_TOTAL = "canary_lb_stats_restart_probe_task_fail_total";
        // Canary LoadBalacnerStats instance state changed.
        public static final String CANARY_LB_STATS_INSTANCE_STATE_CHANGED_TOTAL = "canary_lb_stats_instance_state_changed_total";
    }

    public static abstract class MetricsTag {
        public static final String LB = "lb";
        public static final String SERVICE_ID = "serviceId";
        public static final String INSTANCE_ID = "instanceId";
        public static final String ROUTE_ID = "routeId";
        public static final String ROUTE_IDS = "routeIds";
        public static final String MAX_TRIES = "maxTries";
        public static final String FAIL_ROUTE_SERVICE = "failRouteService";
        public static final String NEW_ALIVE = "newAlive";
    }

}
