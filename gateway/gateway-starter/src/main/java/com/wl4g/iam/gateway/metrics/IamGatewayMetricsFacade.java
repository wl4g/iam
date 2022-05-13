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
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.collect.Lists;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;
import com.wl4g.iam.gateway.util.IamGatewayUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;
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

    private final Map<String, MetricFamilySamples> sampleRegistry = new ConcurrentHashMap<>(16);

    private @Autowired PrometheusMeterRegistry meterRegistry;

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

    private void initLocalInstance() throws Exception {
        boolean secure = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        String serviceId = environment.getRequiredProperty("spring.application.name");
        String host = inet.findFirstNonLoopbackHostInfo().getHostname();
        int port = environment.getRequiredProperty("server.port", Integer.class);
        String instanceId = host.concat(":").concat(valueOf(port));
        this.localInstance = new DefaultServiceInstance(instanceId, serviceId, host, port, secure);
    }

    //
    // The Meter Metrics Active Recorder
    //

    public void counter(ServerWebExchange exchange, MetricsName metricsName, double amount, String... tags) {
        try {
            notNullOf(exchange, "exchange");
            notNullOf(metricsName, "metricsName");
            String routeId = IamGatewayUtil.getRouteId(exchange);
            if (nonNull(routeId)) {
                List<String> _tags = Lists.newArrayList(tags);
                _tags.add(MetricsTag.ROUTE_ID);
                _tags.add(routeId);
                getCounter(metricsName, _tags.toArray(new String[0])).increment(amount);
            }
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}", metricsName, valueOf(amount)), e);
        }
    }

    public void counter(MetricsName metricsName, double amount, String... tags) {
        try {
            notNullOf(metricsName, "metricsName");
            getCounter(metricsName, tags).increment(amount);
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
            _tags.add(MetricsTag.LB_SERVICE_ID);
            _tags.add(status.getInstance().getServiceId());
            _tags.add(MetricsTag.LB_INSTANCE_ID);
            _tags.add(LoadBalancerUtil.getInstanceId(status.getInstance()));
            getCounter(metricsName, _tags.toArray(new String[0])).increment(amount);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}, instanceStatus: {}", metricsName,
                    valueOf(amount)), status, e);
        }
    }

    public void counter(MetricsName metricsName, String routeId, double amount, String... tags) {
        try {
            List<String> _tags = Lists.newArrayList(tags);
            _tags.add(MetricsTag.ROUTE_ID);
            _tags.add(routeId);
            getCounter(metricsName, _tags.toArray(new String[0])).increment(amount);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, amount: {}, routeId: {}", metricsName, valueOf(amount)),
                    routeId, e);
        }
    }

    public Counter getCounter(MetricsName metricsName, String... tags) {
        List<String> _tags = Lists.newArrayList(tags);
        _tags.add(MetricsTag.SELF_INSTANCE_ID);
        _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
        return Counter.builder(metricsName.getName()).description(metricsName.getHelp()).tags(tags).register(meterRegistry);
    }

    public Gauge gauge(MetricsName metricsName, Supplier<Number> supplier, String... tags) {
        return Gauge.builder(metricsName.getName(), supplier).description(metricsName.getHelp()).tags(tags).register(
                meterRegistry);
    }

    public void timer(ServerWebExchange exchange, MetricsName metricsName, long beginNanoTime, String... tags) {
        notNullOf(exchange, "exchange");
        notNullOf(metricsName, "metricsName");
        Duration cost = Duration.ofNanos(nanoTime() - beginNanoTime);
        try {
            String routeId = IamGatewayUtil.getRouteId(exchange);
            if (nonNull(routeId)) {
                List<String> _tags = Lists.newArrayList(tags);
                _tags.add(MetricsTag.SELF_INSTANCE_ID);
                _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
                _tags.add(MetricsTag.ROUTE_ID);
                _tags.add(routeId);
                getTimer(metricsName, _tags.toArray(new String[0])).record(cost);
            }
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, cost: {}ns", metricsName, valueOf(cost.getNano())), e);
        }
    }

    public void timer(MetricsName metricsName, String routeId, long beginNanoTime, String... tags) {
        notNullOf(metricsName, "metricsName");
        hasTextOf(routeId, "routeId");
        Duration cost = Duration.ofNanos(nanoTime() - beginNanoTime);
        try {
            List<String> _tags = Lists.newArrayList(tags);
            _tags.add(MetricsTag.SELF_INSTANCE_ID);
            _tags.add(LoadBalancerUtil.getInstanceId(localInstance));
            _tags.add(MetricsTag.ROUTE_ID);
            _tags.add(routeId);
            getTimer(metricsName, _tags.toArray(new String[0])).record(cost);
        } catch (Exception e) {
            log.warn(format("Cannot add to counter metrics name: %s, cost: {}ns", metricsName, valueOf(cost.getNano())), e);
        }
    }

    public Timer getTimer(MetricsName metricsName, String... tags) {
        return Timer.builder(metricsName.getName())
                .distributionStatisticBufferLength(10240)
                // .distributionStatisticExpiry(Duration.ofDays(1))
                .description(metricsName.getHelp())
                .tags(tags)
                .register(meterRegistry);
    }

    public DistributionSummary getDistributionSummary(MetricsName metricsName, String unit, String... tags) {
        return DistributionSummary.builder(metricsName.getName())
                .distributionStatisticBufferLength(10240)
                // .distributionStatisticExpiry(Duration.ofDays(1))
                .baseUnit(unit)
                .description(metricsName.getHelp())
                .tags(tags)
                .register(meterRegistry);
    }

    //
    // The Metrics Passive Sampler
    //

    public GaugeMetricFamily createGauge(MetricsName metricsName, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(metricsName.getName());
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(metricsName.getName());
                if (isNull(samples)) {
                    sampleRegistry.put(metricsName.getName(),
                            new GaugeMetricFamily(metricsName.getName(), metricsName.getHelp(), asList(labelNames)));
                }
            }
        }
        return (GaugeMetricFamily) samples;
    }

    public CounterMetricFamily createCounter(MetricsName metricsName, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(metricsName.getName());
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(metricsName.getName());
                if (isNull(samples)) {
                    sampleRegistry.put(metricsName.getName(),
                            new CounterMetricFamily(metricsName.getName(), metricsName.getHelp(), asList(labelNames)));
                }
            }
        }
        return (CounterMetricFamily) samples;
    }

    public SummaryMetricFamily createSummary(MetricsName metricsName, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(metricsName.getName());
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(metricsName.getName());
                if (isNull(samples)) {
                    sampleRegistry.put(metricsName.getName(),
                            new SummaryMetricFamily(metricsName.getName(), metricsName.getHelp(), asList(labelNames)));
                }
            }
        }
        return (SummaryMetricFamily) samples;
    }

    @Getter
    @AllArgsConstructor
    public static enum MetricsName {

        // Simple signature authorizer.

        SIMPLE_SIGN_BLOOM_SUCCESS_TOTAL("iam_gw_simple_sign_bloom_success_total",
                "The total number of success bloom validate for simple signature authenticator"),

        SIMPLE_SIGN_BLOOM_FAIL_TOTAL("iam_gw_simple_sign_bloom_fail_total",
                "The total number of failed bloom validate for simple signature authenticator"),

        SIMPLE_SIGN_SUCCCESS_TOTAL("iam_gw_simple_sign_success_total",
                "The total number of successful authentication by the simple signature authenticator"),

        SIMPLE_SIGN_FAIL_TOTAL("iam_gw_simple_sign_fail_total",
                "The total number of failure authentication by the simple sign authenticator"),

        SIMPLE_SIGN_TIME("iam_gw_simple_sign_time", "The number of simple signature execution cost time"),

        // Canary LoadBalacner.

        // current instance status.
        CANARY_LB_STATS_CURRENT_INSTANCE("iam_gw_canary_lb_stats_current_instance",
                "The current state instance of the canary load balancer statistic"),

        CANARY_LB_STATS_CURRENT_INSTANCE_CONNECTIONS("iam_gw_canary_lb_stats_current_instance_connections",
                "The number of instance connections of the canary load balancer statistic"),

        // chooser statistic

        CANARY_LB_CHOOSE_TOTAL("iam_gw_canary_lb_choose_total",
                "The total number of instances selected by the canary LoadBalancer"),

        CANARY_LB_CHOOSE_FALLBACK_TOTAL("iam_gw_canary_lb_choose_fallback_total",
                "The total number of times that Canary LoadBalancer chose to fallback on instances"),

        CANARY_LB_CHOOSE_MISSING_TOTAL("iam_gw_canary_lb_choose_missing_total", "Total number of canary load balancing misses"),

        CANARY_LB_CHOOSE_MAX_TRIES_TOTAL("iam_gw_canary_lb_choose_max_tries_total",
                "The total number of times the canary load balancer selects a reachable instance to retry"),

        CANARY_LB_CHOOSE_EMPTY_INSTANCES_TOTAL("iam_gw_canary_lb_choose_empty_instances_total",
                "The total number of times the available instances was not found by canary load balancer"),

        // active probe statistic.

        CANARY_LB_STATS_TOTAL("iam_gw_canary_lb_stats_total", "The total number of active probes of the canary load balancer"),

        CANARY_LB_STATS_TIMEOUT_TOTAL("iam_gw_canary_lb_stats_timeout_total",
                "The total number of active probe timeouts of the canary load balancing statistic"),

        CANARY_LB_STATS_CANCEL_ERROR_TOTAL("iam_gw_canary_lb_stats_cancel_error_total",
                "The total number of active probe cancel or error of the canary load balancing statistic"),

        // passive probe statistic.

        CANARY_LB_STATS_CONNECT_OPEN_TOTAL("iam_gw_canary_lb_stats_connect_open_total",
                "The total number of connections opened by the canary load balancer passive probe"),

        CANARY_LB_STATS_CONNECT_CLOSE_TOTAL("iam_gw_canary_lb_stats_connect_close_total",
                "The total number of connections closed by the canary load balancer passive probe"),

        CANARY_LB_STATS_REGISTER_ALL_SERVICES_TOTAL("iam_gw_canary_lb_stats_register_all_services_total",
                "The total number of times the canary load balancer stats is updated and registered according to the router service instance"),

        CANARY_LB_STATS_RESTART_PROBE_TASK_TOTAL("iam_gw_canary_lb_stats_restart_probe_task_total",
                "The total number of detection tasks based on router update or restart canary load balancer"),

        CANARY_LB_STATS_RESTART_PROBE_TASK_FAIL_TOTAL("iam_gw_canary_lb_stats_restart_probe_task_fail_total",
                "The total number of detection tasks based on router update or restart canary load balancer"),

        CANARY_LB_STATS_INSTANCE_STATE_CHANGED_TOTAL("iam_gw_canary_lb_stats_instance_state_changed_total",
                "The total number of failed restart or update loadbalancer probe tasks"),

        CANARY_LB_CHOOSE_TIME("iam_gw_canary_lb_choose_time", "The number of canary loadbalancer choose cost time"),

        // Request limiter.

        REDIS_RATELIMIT_TOTAL("iam_gw_redis_ratelimit_total", "The number of total processing in the redis rate limiter"),

        REDIS_RATELIMIT_HITS_TOTAL("iam_gw_redis_ratelimit_hits_total", "The number of total hits in the redis rate limiter"),

        REDIS_RATELIMIT_TIME("iam_gw_redis_ratelimit_time", "The number of redis ratelimit cost time"),

        REDIS_QUOTALIMIT_TOTAL("iam_gw_redis_quotalimit_total", "The number of total processing in the redis quota limiter"),

        REDIS_QUOTALIMIT_HITS_TOTAL("iam_gw_redis_quotalimit_hits_total", "The number of total hits in the redis quota limiter"),

        REDIS_QUOTALIMIT_TIME("iam_gw_redis_quotalimit_time", "The number of redis quota limit cost time");

        private final String name;
        private final String help;
    }

    public static abstract class MetricsTag {
        public static final String SELF_INSTANCE_ID = "self";
        public static final String ROUTE_ID = "routeId";

        public static final String SIGN_ALG = "alg";
        public static final String SIGN_HASH = "hash";

        public static final String LB = "lb";
        public static final String LB_SERVICE_ID = "serviceId";
        public static final String LB_INSTANCE_ID = "instanceId";
        public static final String LB_ROUTE_IDS = "routeIds";
        public static final String LB_MAX_TRIES = "maxTries";
        public static final String LB_FAIL_ROUTE_SERVICE = "failRouteService";
    }

}
