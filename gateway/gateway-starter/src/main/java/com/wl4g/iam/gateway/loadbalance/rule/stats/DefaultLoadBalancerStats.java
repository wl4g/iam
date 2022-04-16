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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;
import java.time.Duration;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.ApplicationArguments;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.CanaryLoadBalancerClientFilter;
import com.wl4g.infra.common.task.RunnerProperties;
import com.wl4g.infra.common.task.RunnerProperties.StartupMode;
import com.wl4g.infra.common.task.SafeScheduledTaskPoolExecutor;
import com.wl4g.infra.core.task.ApplicationTaskRunner;

import lombok.ToString;
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
@ToString
public class DefaultLoadBalancerStats extends ApplicationTaskRunner<RunnerProperties> implements LoadBalancerStats {

    private final CanaryLoadBalancerClientFilter.Config config;
    private final LoadBalancerCache loadBalancerCache;
    private final ReachableStrategy reachableStrategy;

    public DefaultLoadBalancerStats(CanaryLoadBalancerClientFilter.Config config, LoadBalancerCache loadBalancerCache,
            ReachableStrategy reachableStrategy) {
        super(new RunnerProperties(StartupMode.ASYNC, 1));
        this.config = notNullOf(config, "config");
        this.loadBalancerCache = notNullOf(loadBalancerCache, "loadBalancerCache");
        this.reachableStrategy = notNullOf(reachableStrategy, "reachableStrategy");
    }

    @Override
    protected void onApplicationStarted(ApplicationArguments args, SafeScheduledTaskPoolExecutor worker) throws Exception {
        worker.scheduleWithFixedDelay(this, config.getPing().getInitialMs(), config.getPing().getDelayMs(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        loadBalancerCache.getAllServices().forEach((serviceId, instances) -> {
            safeMap(instances).forEach((instanceId, instance) -> {
                try {
                    doPing(instance);
                } catch (Exception e) {
                    log.warn(format("Failed to the request ping. serviceId=%s, instanceId=%s", serviceId, instanceId), e);
                }
            });
        });
    }

    @Override
    public void register(List<ServiceInstance> instances) {
        safeList(instances).stream()
                .forEach(i -> loadBalancerCache.putServiceInstance(new ServiceInstanceStatus().withInstance(i)));
    }

    @Override
    public int connect(ServerWebExchange exchange, ServiceInstance instance) {
        exchange.getAttributes().put(KEY_COST_TIME, currentTimeMillis());
        ServiceInstanceStatus status = loadBalancerCache.getServiceInstance(instance.getServiceId(),
                LoadBalancerStats.getInstanceId(instance), true);
        try {
            return status.getStats().getConnections().addAndGet(1);
        } finally {
            // update
            loadBalancerCache.putServiceInstance(status);
        }
    }

    @Override
    public int disconnect(ServerWebExchange exchange, ServiceInstance instance) {
        ServiceInstanceStatus status = loadBalancerCache.getServiceInstance(instance.getServiceId(),
                LoadBalancerStats.getInstanceId(instance), true);
        try {
            Stats stats = status.getStats();
            long beginTime = exchange.getRequiredAttribute(KEY_COST_TIME);
            save(status, new PassivePing((currentTimeMillis() - beginTime)));
            return stats.getConnections().addAndGet(-1);
        } finally {
            // update
            loadBalancerCache.putServiceInstance(status);
        }
    }

    @Override
    public List<ServiceInstanceStatus> getReachableInstances(String serviceId) {
        return safeMap(loadBalancerCache.getService(serviceId, true)).values()
                .stream()
                .filter(i -> LoadBalancerStats.Stats.isAlive(config, i.getStats()))
                .collect(toList());
    }

    @Override
    public List<ServiceInstanceStatus> getAllInstances(String serviceId) {
        return safeMap(loadBalancerCache.getService(serviceId, true)).values().stream().collect(toList());
    }

    protected Disposable doPing(ServiceInstanceStatus status) {
        /**
         * Notice: A timeout must be set when pinging an instance group to
         * prevent the group from being blocked for too long. Problems with
         * netflix ribbon implementation see:
         * {@link com.netflix.loadbalancer.BaseLoadBalancer.SerialPingStrategy}
         * and {@link com.netflix.loadbalancer.PingUrl#isAlive}
         * see:https://stackoverflow.com/questions/61843235/reactor-netty-not-getting-an-httpserver-response-when-the-httpclient-subscribes
         * see:https://github.com/reactor/reactor-netty/issues/151
         */
        Duration timeout = Duration.ofMillis(config.getPing().getTimeoutMs());
        if (!isBlank(config.getPing().getExpectBody())) {
            return HttpClient.create()
                    .wiretap(config.getPing().isDebug())
                    .get()
                    .uri(buildUri(status))
                    .responseConnection((res, connection) -> Mono.just(res))
                    .timeout(timeout,
                            Mono.fromRunnable(() -> save(status, new ActivePing(currentTimeMillis(), true, null, null))))
                    .doFinally(signal -> {
                        if (signal != SignalType.ON_COMPLETE) {
                            // Failed to request ping.
                            if (signal == SignalType.ON_ERROR || signal == SignalType.CANCEL) {
                                save(status, new ActivePing(currentTimeMillis(), false, null, null));
                            }
                        }
                    })
                    // main thread non-blocking.
                    .subscribe(response -> {
                        save(status, new ActivePing(currentTimeMillis(), false, response.status(), null));
                    }, ex -> {
                        save(status, new ActivePing(currentTimeMillis(), false, null, null));
                    }, () -> {
                        log.debug("Ping completion for service instance status: {}", status);
                    });
        }

        return HttpClient.create()
                .wiretap(config.getPing().isDebug())
                .get()
                .uri(buildUri(status))
                .responseContent()
                .aggregate()
                .asString()
                .timeout(timeout, Mono.fromRunnable(() -> save(status, new ActivePing(currentTimeMillis(), true, null, null))))
                .doFinally(signal -> {
                    if (signal != SignalType.ON_COMPLETE) {
                        // Failed to request ping.
                        if (signal == SignalType.ON_ERROR || signal == SignalType.CANCEL) {
                            save(status, new ActivePing(currentTimeMillis(), false, null, null));
                        }
                    }
                })
                // main thread non-blocking.
                .subscribe(response -> {
                    save(status, new ActivePing(currentTimeMillis(), false, null, response));
                }, ex -> {
                    save(status, new ActivePing(currentTimeMillis(), false, null, null));
                }, () -> {
                    log.debug("Ping completion for service instance status: {}", status);
                });
    }

    protected URI buildUri(ServiceInstanceStatus status) {
        ServiceInstance instance = status.getInstance();
        return URI.create(
                instance.getScheme().concat("://").concat(instance.getHost()).concat(":").concat(instance.getPort() + "").concat(
                        config.getPing().getPath()));
    }

    protected synchronized void save(ServiceInstanceStatus status, ActivePing activePing) {
        Stats stats = status.getStats();
        Deque<ActivePing> queue = stats.getActivePings();
        if (queue.size() > config.getPing().getReceiveQueue()) {
            queue.poll();
        }
        queue.offer(activePing);
        reachableStrategy.updateStatus(config, status);
    }

    protected synchronized void save(ServiceInstanceStatus status, PassivePing passivePing) {
        Stats stats = status.getStats();
        Deque<PassivePing> queue = stats.getPassivePings();
        if (queue.size() > config.getPing().getReceiveQueue()) {
            queue.poll();
        }
        queue.offer(passivePing);
        stats.setLatestCostTime(queue.peekLast().getCostTime());
        stats.setOldestCostTime(queue.peekLast().getCostTime());
        stats.setMaxCostTime(queue.stream().mapToDouble(p -> p.getCostTime()).max().getAsDouble());
        stats.setMinCostTime(queue.stream().mapToDouble(p -> p.getCostTime()).min().getAsDouble());
        stats.setAvgCostTime(queue.stream().mapToDouble(p -> p.getCostTime()).average().getAsDouble());
    }

}
