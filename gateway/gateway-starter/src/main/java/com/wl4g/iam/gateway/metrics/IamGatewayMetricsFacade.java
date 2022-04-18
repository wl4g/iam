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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.collect.Lists;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
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
public class IamGatewayMetricsFacade implements InitializingBean {

    private @Autowired PrometheusMeterRegistry registry;

    /**
     * see:{@link org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration#simpleDiscoveryProperties}
     */
    private @Autowired InetUtils inet;

    private @Autowired Environment environment;

    /**
     * see:{@link org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties#local}
     */
    private DefaultServiceInstance localInstance;

    @Override
    public void afterPropertiesSet() throws Exception {
        initLocalInstance();
    }

    public void counter(ServerWebExchange exchange, MetricsName metricsName, double amount, String... tags) {
        try {
            notNullOf(exchange, "exchange");
            notNullOf(metricsName, "metricsName");
            Object route = exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (nonNull(route)) {
                String routeId = ((Route) route).getId();
                List<String> _tags = Lists.newArrayList(tags);
                _tags.add(MetricsTag.SELF_INSTANCE_ID);
                _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
                _tags.add(MetricsTag.ROUTE_ID);
                _tags.add(routeId);
                counter(metricsName, _tags.toArray(new String[0])).increment(amount);
            }
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}", metricsName, valueOf(amount)), e);
        }
    }

    public void counter(MetricsName metricsName, double amount, String... tags) {
        try {
            notNullOf(metricsName, "metricsName");
            List<String> _tags = Lists.newArrayList(tags);
            _tags.add(MetricsTag.SELF_INSTANCE_ID);
            _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
            counter(metricsName, _tags.toArray(new String[0])).increment(amount);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}", metricsName, valueOf(amount)), e);
        }
    }

    public void counter(InstanceStatus status, MetricsName metricsName, double amount, String... tags) {
        try {
            notNullOf(status, "instanceStatus");
            notNullOf(status.getInstance(), "instanceStatus.instance");
            notNullOf(metricsName, "metricsName");
            List<String> _tags = Lists.newArrayList(tags);
            _tags.add(MetricsTag.SELF_INSTANCE_ID);
            _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
            _tags.add(MetricsTag.LB_SERVICE_ID);
            _tags.add(status.getInstance().getServiceId());
            _tags.add(MetricsTag.LB_INSTANCE_ID);
            _tags.add(LoadBalancerUtil.getInstanceId(status.getInstance()));
            counter(metricsName, _tags.toArray(new String[0])).increment(amount);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}, instanceStatus: {}", metricsName,
                    valueOf(amount)), status, e);
        }
    }

    public Counter counter(MetricsName metricsName, String... tags) {
        return Counter.builder(metricsName.getName()).description(metricsName.getHelp()).tags(tags).register(registry);
    }

    public Gauge gauge(MetricsName metricsName, Supplier<Number> supplier, String... tags) {
        return Gauge.builder(metricsName.getName(), supplier).description(metricsName.getHelp()).tags(tags).register(registry);
    }

    public void timer(ServerWebExchange exchange, MetricsName metricsName, long beginNanoTime, String... tags) {
        notNullOf(exchange, "exchange");
        notNullOf(metricsName, "metricsName");
        Duration cost = Duration.ofNanos(nanoTime() - beginNanoTime);
        try {
            Object route = exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (nonNull(route)) {
                String routeId = ((Route) route).getId();
                List<String> _tags = Lists.newArrayList(tags);
                _tags.add(MetricsTag.SELF_INSTANCE_ID);
                _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
                _tags.add(MetricsTag.ROUTE_ID);
                _tags.add(routeId);
                timer(metricsName, _tags.toArray(new String[0])).record(cost);
            }
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, cost: {}ns", metricsName, valueOf(cost.getNano())), e);
        }
    }

    public Timer timer(MetricsName metricsName, String... tags) {
        return Timer.builder(metricsName.getName())
                .distributionStatisticBufferLength(10240)
                // .distributionStatisticExpiry(Duration.ofDays(1))
                .description(metricsName.getHelp())
                .tags(tags)
                .register(registry);
    }

    public DistributionSummary distributionSummary(MetricsName metricsName, String unit, String... tags) {
        return DistributionSummary.builder(metricsName.getName())
                .distributionStatisticBufferLength(10240)
                // .distributionStatisticExpiry(Duration.ofDays(1))
                .baseUnit(unit)
                .description(metricsName.getHelp())
                .tags(tags)
                .register(registry);
    }

    private void initLocalInstance() throws Exception {
        boolean secure = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        String serviceId = environment.getRequiredProperty("spring.application.name");
        String host = inet.findFirstNonLoopbackHostInfo().getHostname();
        int port = environment.getRequiredProperty("server.port", Integer.class);
        String instanceId = host.concat(":").concat(valueOf(port));
        this.localInstance = new DefaultServiceInstance(instanceId, serviceId, host, port, secure);
    }

    @Getter
    @AllArgsConstructor
    public static enum MetricsName {
        // Simple signature authorizer.
        SIMPLE_SIGN_BLOOM_SUCCESS_TOTAL("simple_sign_bloom_success_total",
                "The total number of success bloom validate for simple signature authenticator"),

        SIMPLE_SIGN_BLOOM_FAIL_TOTAL("simple_sign_bloom_fail_total",
                "The total number of failed bloom validate for simple signature authenticator"),

        SIMPLE_SIGN_SUCCCESS_TOTAL("simple_sign_success_total",
                "The total number of successful authentication by the simple signature authenticator"),

        SIMPLE_SIGN_FAIL_TOTAL("simple_sign_fail_total",
                "The total number of failure authentication by the simple sign authenticator"),

        // Canary LoadBalacner chooser.
        CANARY_LB_CHOOSE_TOTAL("canary_lb_choose_total", "The total number of instances selected by the canary LoadBalancer"),

        CANARY_LB_CHOOSE_FALLBACK_TOTAL("canary_lb_choose_fallback_total",
                "The total number of times that Canary LoadBalancer chose to fallback on instances"),

        CANARY_LB_CHOOSE_MISSING_TOTAL("canary_lb_choose_missing_total", "Total number of canary load balancing misses"),

        CANARY_LB_CHOOSE_MAX_TRIES_TOTAL("canary_lb_choose_max_tries_total",
                "The total number of times the canary load balancer selects a reachable instance to retry"),

        CANARY_LB_CHOOSE_EMPTY_INSTANCES_TOTAL("canary_lb_choose_empty_instances_total",
                "The total number of times the available instances was not found by canary load balancer"),

        // Canary LoadBalacnerStats Active probe.
        CANARY_LB_STATS_TOTAL("canary_lb_stats_total", "The total number of active probes of the canary load balancer"),

        CANARY_LB_STATS_TIMEOUT_TOTAL("canary_lb_stats_timeout_total",
                "The total number of active probe timeouts of the canary load balancing statistic"),

        CANARY_LB_STATS_CANCEL_ERROR_TOTAL("canary_lb_stats_cancel_error_total",
                "The total number of active probe cancel or error of the canary load balancing statistic"),

        // Canary LoadBalacnerStats Passive probe.
        CANARY_LB_STATS_CONNECT_OPEN_TOTAL("canary_lb_stats_connect_open_total",
                "The total number of connections opened by the canary load balancer passive probe"),

        CANARY_LB_STATS_CONNECT_CLOSE_TOTAL("canary_lb_stats_connect_close_total",
                "The total number of connections closed by the canary load balancer passive probe"),

        // Canary LoadBalacnerStats Register.
        CANARY_LB_STATS_REGISTER_ALL_SERVICES_TOTAL("canary_lb_stats_register_all_services_total",
                "The total number of times the canary load balancer stats is updated and registered according to the router service instance"),

        CANARY_LB_STATS_RESTART_PROBE_TASK_TOTAL("canary_lb_stats_restart_probe_task_total",
                "The total number of detection tasks based on router update or restart canary load balancer"),

        CANARY_LB_STATS_RESTART_PROBE_TASK_FAIL_TOTAL("canary_lb_stats_restart_probe_task_fail_total",
                "The total number of detection tasks based on router update or restart canary load balancer"),

        // Canary LoadBalacnerStats instance state changed.
        CANARY_LB_STATS_INSTANCE_STATE_CHANGED_TOTAL("canary_lb_stats_instance_state_changed_total",
                "The total number of failed restart or update loadbalancer probe tasks"),

        SIMPLE_SIGN_TIME("simple_sign_time", "The total number of simple signature execution cost time"),

        CANARY_LB_CHOOSE_TIME("canary_lb_choose_time", "The total number of canary loadbalancer choose cost time");

        private final String name;
        private final String help;
    }

    public static abstract class MetricsTag {
        public static final String SELF_INSTANCE_ID = "self";
        public static final String ROUTE_ID = "routeId";

        public static final String SIGN_ALG = "signAlg";
        public static final String SIGN_HASH = "signHash";

        public static final String LB = "lb";
        public static final String LB_SERVICE_ID = "serviceId";
        public static final String LB_INSTANCE_ID = "instanceId";
        public static final String LB_ROUTE_IDS = "routeIds";
        public static final String LB_MAX_TRIES = "maxTries";
        public static final String LB_FAIL_ROUTE_SERVICE = "failRouteService";
    }

}
