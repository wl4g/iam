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
package com.wl4g.iam.gateway.loadbalance.stats;

import static com.wl4g.infra.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.net.URI;
import java.time.Duration;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.CanaryLoadBalancerFilterFactory.CanaryLoadBalancerGatewayFilter;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties.ProbeProperties;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;
import com.wl4g.infra.common.task.RunnerProperties;
import com.wl4g.infra.common.task.RunnerProperties.StartupMode;
import com.wl4g.infra.common.task.SafeScheduledTaskPoolExecutor;
import com.wl4g.infra.core.task.ApplicationTaskRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.netty.http.client.HttpClient;

/**
 * {@link DefaultLoadBalancerStats}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-13 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class DefaultLoadBalancerStats extends ApplicationTaskRunner<RunnerProperties> implements LoadBalancerStats {

    private final CanaryLoadBalancerProperties loadBalancerConfig;
    private @Autowired LoadBalancerRegistry loadBalancerRegistry;
    private @Autowired ReachableStrategy reachableStrategy;
    private @Autowired @Lazy RouteLocator routeRlocator;
    private @Autowired DiscoveryClient discoveryClient;
    private @Autowired IamGatewayMetricsFacade metricsFacade;
    private final AtomicBoolean initRegisterAllRouteServicesCompleted = new AtomicBoolean(false);
    private final Map<String, ScheduledFuture<?>> routeServicesProbeFutures = new ConcurrentHashMap<>(4);

    public DefaultLoadBalancerStats(CanaryLoadBalancerProperties loadBalancerConfig) {
        super(new RunnerProperties(StartupMode.ASYNC, loadBalancerConfig.getStatsSchedulerThread()));
        this.loadBalancerConfig = notNullOf(loadBalancerConfig, "loadBalancerConfig");
    }

    @Override
    protected void onApplicationStarted(ApplicationArguments args, SafeScheduledTaskPoolExecutor worker) throws Exception {
        getWorker().scheduleWithFixedDelay(() -> registerAllRouteServices(() -> {
            if (initRegisterAllRouteServicesCompleted.compareAndSet(false, true)) {
                restartProbeTask();
            }
        }), loadBalancerConfig.getRegisterRouteServicesInitialSeconds(),
                loadBalancerConfig.getRegisterRouteServicesDelaySeconds(), SECONDS);
    }

    @Override
    public synchronized void registerAllRouteServices(@Nullable Runnable callback) {
        try {
            discoveryClient.getServices();
            addCounterMetrics(MetricsName.CANARY_LB_STATS_REGISTER_ALL_SERVICES_TOTAL);

            routeRlocator.getRoutes().collectList().block().forEach(route -> {
                if (nonNull(route.getUri()) && equalsIgnoreCase("lb", route.getUri().getScheme())) {
                    String serviceId = hasText(route.getUri().getHost(), "invalid the LB route.uri. %s", route.getUri());

                    // Gets all canary loadBalancer filters.
                    List<CanaryLoadBalancerGatewayFilter> filters = safeList(route.getFilters()).stream()
                            .filter(f -> nonNull(f))
                            .filter(f -> f instanceof CanaryLoadBalancerGatewayFilter)
                            .map(f -> (CanaryLoadBalancerGatewayFilter) f)
                            .collect(toList());

                    // The ping probe needs to be started only if the canary
                    // LB is configured.
                    if (!isEmpty(filters)) {

                        // Check canary LB filter should only be configured
                        // with one.
                        if (filters.size() > 1) {
                            throw new IllegalStateException(format(
                                    "Only one canary LB filter is allowed to be configured under the same route. routeId=%s",
                                    route.getId()));
                        }
                        CanaryLoadBalancerGatewayFilter filter = filters.stream().findFirst().get();

                        // Convert to Service Instance Status and register.
                        safeList(discoveryClient.getInstances(serviceId)).stream().map(i -> new InstanceStatus(i)).forEach(
                                i -> loadBalancerRegistry.register(route.getId(), filter.getConfig(), i));
                    }
                }
            });
            callback.run();
        } catch (Exception e) {
            log.warn("Failed to update or register all route services instance to the canary LB probe registry.", e);
        }
    }

    @Override
    public synchronized void restartProbeTask(@Nullable String... routeIds) {
        addCounterMetrics(MetricsName.CANARY_LB_STATS_RESTART_PROBE_TASK_TOTAL, routeIds);

        List<String> _routeIds = safeArrayToList(routeIds);
        Map<String, ScheduledFuture<?>> updateRouteServicesProbeFutures = routeServicesProbeFutures.entrySet()
                .stream()
                .filter(e -> nonNull(e.getValue()))
                .filter(e -> _routeIds.isEmpty() || _routeIds.contains(e.getKey()))
                .collect(toMap(e -> e.getKey(), e -> e.getValue()));

        // Take the routeId as the primary subject, which can bring a lot of
        // flexibility. [Important]: serviceId and routeId are one to one.
        Map<String, RouteServiceStatus> routeServices = loadBalancerRegistry.getAllRouteServices()
                .entrySet()
                .stream()
                .filter(e -> nonNull(e.getValue()))
                .filter(e -> _routeIds.isEmpty() || _routeIds.contains(e.getKey()))
                .collect(toMap(e -> e.getKey(), e -> e.getValue()));

        // Stop the before probe tasks.
        updateRouteServicesProbeFutures.forEach((routeId, future) -> future.cancel(false));

        // Re-scheduling probe tasks.
        int count = 0, maxTries = routeServices.size() * 2;
        while (routeServices.size() > 0 && count < maxTries) {
            ++count;
            Iterator<Entry<String, RouteServiceStatus>> it = routeServices.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, RouteServiceStatus> entry = it.next();
                String routeId = entry.getKey();
                RouteServiceStatus routeService = entry.getValue();
                ProbeProperties probe = routeService.getConfig().getProbe();
                ScheduledFuture<?> future = updateRouteServicesProbeFutures.get(routeId);
                if (isNull(future) || (nonNull(future) && future.isDone())) {
                    Map<String, InstanceStatus> serviceInstances = routeService.getInstances();
                    getWorker().scheduleWithFixedDelay(() -> {
                        serviceInstances.values().forEach(instance -> {
                            try {
                                doPing(probe, instance);
                            } catch (Exception e) {
                                log.warn(
                                        format("Failed to the request probe. routeId=%s, serviceId=%s, instanceId=%s", routeId,
                                                instance.getInstance().getServiceId(), instance.getInstance().getInstanceId()),
                                        e);
                            }
                        });
                    }, probe.getInitialSeconds(), probe.getDelaySeconds(), SECONDS);
                    it.remove();
                }
            }
            if (routeServices.size() > 0) {
                // Give up the opportunity for short-term CPU to give other
                // threads execution, just like the sleep() method does not
                // release the lock.
                Thread.yield();
                continue;
            }
        }
        if (routeServices.size() > 0) {
            log.warn("Cannot to update route services instance probe tasks. routeIds={}", routeServices.keySet());
            addCounterMetrics(MetricsName.CANARY_LB_STATS_RESTART_PROBE_TASK_FAIL_TOTAL, routeIds);
        }
    }

    @Override
    public int connect(ServerWebExchange exchange, ServiceInstance instance) {
        Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        exchange.getAttributes().put(KEY_COST_TIME, currentTimeMillis());
        RouteServiceStatus routeService = loadBalancerRegistry.getRouteService(route.getId(), true);
        InstanceStatus instanceStatus = routeService.getInstances().get(LoadBalancerUtil.getInstanceId(instance));
        if (nonNull(instanceStatus)) {
            int count = instanceStatus.getStats().getConnections().addAndGet(1);
            loadBalancerRegistry.update(route.getId(), routeService, true);
            addCounterMetrics(exchange, MetricsName.CANARY_LB_STATS_CONNECT_OPEN_TOTAL, instance);
            return count;
        }
        return 0;
    }

    @Override
    public int disconnect(ServerWebExchange exchange, ServiceInstance instance) {
        Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        RouteServiceStatus routeService = loadBalancerRegistry.getRouteService(route.getId(), true);
        InstanceStatus instanceStatus = routeService.getInstances().get(LoadBalancerUtil.getInstanceId(instance));
        if (nonNull(instanceStatus)) {
            Stats stats = instanceStatus.getStats();
            long beginTime = exchange.getRequiredAttribute(KEY_COST_TIME);
            save(routeService.getConfig().getProbe(), instanceStatus, new PassiveProbe((currentTimeMillis() - beginTime), null));
            int count = stats.getConnections().addAndGet(-1);
            loadBalancerRegistry.update(route.getId(), routeService, true);
            addCounterMetrics(exchange, MetricsName.CANARY_LB_STATS_CONNECT_CLOSE_TOTAL, instance);
            return count;
        }
        return 0;
    }

    @Override
    public List<InstanceStatus> getReachableInstances(String routeId) {
        RouteServiceStatus routeService = loadBalancerRegistry.getRouteService(routeId, true);
        return safeMap(routeService.getInstances()).values()
                .stream()
                .filter(i -> LoadBalancerUtil.isAlive(routeService.getConfig(), i.getStats()))
                .collect(toList());
    }

    @Override
    public List<InstanceStatus> getAllInstances(String routeId) {
        RouteServiceStatus routeService = loadBalancerRegistry.getRouteService(routeId, true);
        return safeMap(routeService.getInstances()).values().stream().collect(toList());
    }

    @Override
    public @NotNull Map<String, RouteServiceStatus> getAllRouteServices() {
        return unmodifiableMap(loadBalancerRegistry.getAllRouteServices());
    }

    @Override
    public String toString() {
        return "DefaultLoadBalancerStats [loadBalancerConfig=" + loadBalancerConfig + ", loadBalancerRegistry="
                + loadBalancerRegistry + ", reachableStrategy=" + reachableStrategy + ", routeRlocator=" + routeRlocator
                + ", discoveryClient=" + discoveryClient + ", metricsFacade=" + metricsFacade
                + ", initRegisterAllRouteServicesCompleted=" + initRegisterAllRouteServicesCompleted
                + ", routeServicesProbeFutures=" + routeServicesProbeFutures + "]";
    }

    protected Disposable doPing(ProbeProperties probe, InstanceStatus status) {
        addCounterMetrics(status, MetricsName.CANARY_LB_STATS_TOTAL);

        /**
         * Notice: A timeout must be set when pinging an instance group to
         * prevent the group from being blocked for too long. Problems with
         * netflix ribbon implementation see:
         * {@link com.netflix.loadbalancer.BaseLoadBalancer.SerialPingStrategy}
         * and {@link com.netflix.loadbalancer.PingUrl#isAlive}
         * see:https://stackoverflow.com/questions/61843235/reactor-netty-not-getting-an-httpserver-response-when-the-httpclient-subscribes
         * see:https://github.com/reactor/reactor-netty/issues/151
         */
        Duration timeout = Duration.ofMillis(probe.getTimeoutMs());
        URI pingUri = buildUri(probe, status);
        log.debug("LoadBalancer stats probe to {}->'{}' ...", status, pingUri);
        if (!isBlank(probe.getExpectBody())) {
            return HttpClient.create()
                    .wiretap(probe.isWiretap())
                    .get()
                    .uri(pingUri)
                    .responseContent()
                    .aggregate()
                    .asString()
                    .timeout(timeout,
                            Mono.fromRunnable(
                                    () -> save(probe, status, new ActiveProbe(currentTimeMillis(), true, null, null, null))))
                    .doFinally(signal -> {
                        // Failed to request probe ping.
                        if (signal == SignalType.CANCEL) {
                            save(probe, status, new ActiveProbe(currentTimeMillis(), false, true, null, null));
                        }
                    })
                    // main thread non-blocking.
                    .subscribe(response -> {
                        log.debug("Probe success for instance status: {}, response: {}", status, response);
                        save(probe, status, new ActiveProbe(currentTimeMillis(), false, null, null, response));
                    }, ex -> {
                        log.debug("Probe error for instance status: {}", status);
                        save(probe, status, new ActiveProbe(currentTimeMillis(), false, null, null, null));
                    }, () -> {
                        log.debug("Ping completion for service instance status: {}", status);
                    });
        }
        return HttpClient.create()
                .wiretap(probe.isWiretap())
                .get()
                .uri(pingUri)
                .responseConnection((res, connection) -> Mono.just(res))
                .timeout(timeout,
                        Mono.fromRunnable(
                                () -> save(probe, status, new ActiveProbe(currentTimeMillis(), true, null, null, null))))
                .doFinally(signal -> {
                    // Failed to request probe ping.
                    if (signal == SignalType.CANCEL) {
                        save(probe, status, new ActiveProbe(currentTimeMillis(), false, true, null, null));
                    }
                })
                // main thread non-blocking.
                .subscribe(response -> {
                    log.debug("Probe success for instance status: {}, response: {}", status, response);
                    save(probe, status, new ActiveProbe(currentTimeMillis(), false, null, response.status(), null));
                }, ex -> {
                    log.debug("Probe error for instance status: {}", status);
                    save(probe, status, new ActiveProbe(currentTimeMillis(), false, null, null, null));
                }, () -> {
                    log.debug("Probe completion for instance status: {}", status);
                });
    }

    protected URI buildUri(ProbeProperties probe, InstanceStatus status) {
        ServiceInstance instance = status.getInstance();
        String scheme = instance.isSecure() ? "https://" : "http://";
        return URI.create(scheme.concat(instance.getHost()).concat(":").concat(instance.getPort() + "").concat(probe.getPath()));
    }

    protected synchronized void save(ProbeProperties probe, InstanceStatus status, ActiveProbe activeProbe) {
        Stats stats = status.getStats();
        Deque<ActiveProbe> queue = stats.getActiveProbes();
        if (queue.size() > probe.getReceiveQueue()) {
            queue.pollFirst();
        }
        queue.offer(activeProbe);
        reachableStrategy.updateStatus(probe, status);

        if (activeProbe.isTimeout()) {
            addCounterMetrics(status, MetricsName.CANARY_LB_STATS_TIMEOUT_TOTAL);
        }
        if (nonNull(activeProbe.getErrorOrCancel()) && activeProbe.getErrorOrCancel()) {
            addCounterMetrics(status, MetricsName.CANARY_LB_STATS_CANCEL_ERROR_TOTAL);
        }
    }

    protected synchronized void save(ProbeProperties probe, InstanceStatus status, PassiveProbe passiveProbe) {
        Stats stats = status.getStats();
        Deque<PassiveProbe> queue = stats.getPassiveProbes();
        if (queue.size() > probe.getReceiveQueue()) {
            queue.pollFirst();
        }
        queue.offer(passiveProbe);
        stats.setLatestCostTime(queue.peekLast().getCostTime());
        stats.setOldestCostTime(queue.peekLast().getCostTime());
        stats.setMaxCostTime(queue.stream().mapToDouble(p -> p.getCostTime()).max().getAsDouble());
        stats.setMinCostTime(queue.stream().mapToDouble(p -> p.getCostTime()).min().getAsDouble());
        stats.setAvgCostTime(queue.stream().mapToDouble(p -> p.getCostTime()).average().getAsDouble());
    }

    /**
     * Add register all route services metrics.
     */
    protected void addCounterMetrics(MetricsName metricsName) {
        metricsFacade.counter(metricsName, 1);
    }

    /**
     * Add restart probe task metrics.
     */
    protected void addCounterMetrics(MetricsName metricsName, String... routeIds) {
        if (isEmptyArray(routeIds)) {
            metricsFacade.counter(metricsName, 1);
        } else {
            metricsFacade.counter(metricsName, 1, MetricsTag.LB_ROUTE_IDS, asList(routeIds).toString());
        }
    }

    /**
     * Add restart probe task metrics.
     */
    protected void addCounterMetrics(
            MetricsName metricsName,
            Map<String, RouteServiceStatus> failRouteServices,
            int maxTries,
            String... routeIds) {

        String failRouteServiceStr = failRouteServices.values()
                .stream()
                .map(rs -> rs.getRouteId()
                        .concat("->")
                        .concat(rs.getInstances()
                                .values()
                                .stream()
                                .map(i -> i.getInstance().getServiceId().concat("/").concat(
                                        LoadBalancerUtil.getInstanceId(i.getInstance())))
                                .collect(toList())
                                .toString()))
                .collect(toList())
                .toString();

        metricsFacade.counter(metricsName, 1, MetricsTag.LB_FAIL_ROUTE_SERVICE, failRouteServiceStr, MetricsTag.LB_MAX_TRIES,
                valueOf(maxTries), MetricsTag.LB_ROUTE_IDS, asList(routeIds).toString());
    }

    /**
     * Add active probe metrics.
     */
    protected void addCounterMetrics(InstanceStatus status, MetricsName metricsName) {
        metricsFacade.counter(status, metricsName, 1, new String[0]);
    }

    /**
     * Add passive probe metrics.
     */
    protected void addCounterMetrics(ServerWebExchange exchange, MetricsName metricsName, ServiceInstance instance) {
        metricsFacade.counter(exchange, metricsName, 1, MetricsTag.LB_SERVICE_ID, instance.getServiceId(),
                MetricsTag.LB_INSTANCE_ID, LoadBalancerUtil.getInstanceId(instance));
    }

}
