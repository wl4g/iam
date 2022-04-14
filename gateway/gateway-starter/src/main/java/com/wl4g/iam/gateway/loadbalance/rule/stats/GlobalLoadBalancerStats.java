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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.cloud.client.ServiceInstance;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.infra.common.task.RunnerProperties;
import com.wl4g.infra.common.task.SafeScheduledTaskPoolExecutor;
import com.wl4g.infra.core.task.ApplicationTaskRunner;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.netty.http.client.HttpClient;

/**
 * {@link GlobalLoadBalancerStats}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-13 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class GlobalLoadBalancerStats extends ApplicationTaskRunner<RunnerProperties> implements LoadBalancerStats {

    private final Map<String, Map<String, ServiceInstanceInfo>> globalServices = new ConcurrentHashMap<>(32);
    private final LoadBalancerProperties loadBalancerConfig;

    public GlobalLoadBalancerStats(LoadBalancerProperties loadBalancerConfig) {
        this.loadBalancerConfig = notNullOf(loadBalancerConfig, "loadBalancerConfig");
    }

    @Override
    protected void onApplicationStarted(ApplicationArguments args, SafeScheduledTaskPoolExecutor worker) throws Exception {
        worker.scheduleWithFixedDelay(this, loadBalancerConfig.getStats().getInitialDelaySeconds(),
                loadBalancerConfig.getStats().getDelaySeconds(), TimeUnit.SECONDS);
    }

    @Override
    public void register(ServiceInstanceInfo instance) {
        getOrCreateInstance(instance.getInstance().getServiceId(), instance.getInstance().getInstanceId());
    }

    @Override
    public int connect(ServiceInstance instance, int ammount) {
        ServiceInstanceInfo info = getOrCreateInstance(instance.getServiceId(), instance.getInstanceId());
        return info.getStats().getConnections().addAndGet(ammount);
    }

    @Override
    public int disconnect(ServiceInstance instance, int ammount) {
        ServiceInstanceInfo info = getOrCreateInstance(instance.getServiceId(), instance.getInstanceId());
        return info.getStats().getConnections().addAndGet(-ammount);
    }

    public List<ServiceInstanceInfo> getReachableInstances(String serviceId) {
        return getOrCreateService(serviceId).values()
                .stream()
                .map(i -> calculateStatus(i))
                .filter(i -> i.getStats().getAlive())
                .collect(toList());
    }

    public List<ServiceInstanceInfo> getAllInstances(String serviceId) {
        return getOrCreateService(serviceId).values().stream().collect(toList());
    }

    private synchronized ServiceInstanceInfo calculateStatus(ServiceInstanceInfo instance) {
        // Calculate health statistics status.
        Stats stats = instance.getStats();
        Queue<PingRecord> queue = stats.getPingRecords();
        for (int i = 0, lastAvailableSize = queue.size() / 4; i < lastAvailableSize; i++) {
            PingRecord ping = queue.peek();
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
                log.warn("LoadBalancer server [{}/{}] status changed to {}", instance.getInstance().getServiceId(),
                        instance.getInstance().getInstanceId(), (stats.getAlive() ? "ALIVE" : "DEAD"));
            }
        }
        return instance;
    }

    private ServiceInstanceInfo getOrCreateInstance(String serviceId, String instanceId) {
        Map<String, ServiceInstanceInfo> service = getOrCreateService(serviceId);
        ServiceInstanceInfo info = service.get(instanceId);
        if (isNull(info)) {
            synchronized (instanceId) {
                info = service.get(instanceId);
                if (isNull(info)) {
                    service.put(instanceId, info = new ServiceInstanceInfo());
                }
            }
        }
        return info;
    }

    private Map<String, ServiceInstanceInfo> getOrCreateService(String serviceId) {
        Map<String, ServiceInstanceInfo> instances = globalServices.get(serviceId);
        if (isNull(instances)) {
            synchronized (serviceId) {
                instances = globalServices.get(serviceId);
                if (isNull(instances)) {
                    globalServices.put(serviceId, instances = new ConcurrentHashMap<>(16));
                }
            }
        }
        return instances;
    }

    @Override
    public void run() {
        globalServices.forEach((serviceId, instances) -> {
            safeMap(instances).forEach((instanceId, instance) -> {
                try {
                    doPing(instance);
                } catch (Exception e) {
                    log.warn(format("Failed to the request ping. serviceId=%s, instanceId=%s", serviceId, instanceId), e);
                }
            });
        });
    }

    private void doPing(ServiceInstanceInfo instance) {
        /**
         * Notice: A timeout must be set when pinging an instance group to
         * prevent the group from being blocked for too long. Problems with
         * netflix ribbon implementation see:
         * {@link com.netflix.loadbalancer.BaseLoadBalancer.SerialPingStrategy}
         * and {@link com.netflix.loadbalancer.PingUrl#isAlive}
         */
        Duration responseTimeout = Duration.ofMillis(30_000);
        HttpClient.create()
                .wiretap(true)
                .get()
                .uri(buildUri(instance))
                .responseContent()
                .asString()
                .collectList()
                .doOnSuccess(body -> {
                    String responseBody = body.stream().collect(joining());
                    save(instance, new PingRecord(currentTimeMillis(), false, null, responseBody));
                })
                .timeout(responseTimeout,
                        Mono.fromRunnable(() -> save(instance, new PingRecord(currentTimeMillis(), true, null, null))))
                .doFinally(signal -> {
                    if (signal != SignalType.ON_COMPLETE) {
                        // Failed to request ping.
                        if (signal == SignalType.ON_ERROR || signal == SignalType.CANCEL) {
                            save(instance, new PingRecord(currentTimeMillis(), false, null, null));
                        }
                    }
                })
                // TODO use non-blocking ping request.
                .block(responseTimeout);
    }

    private URI buildUri(ServiceInstanceInfo instance) {
        return URI.create(instance.getInstance().getScheme() + "://" + instance.getInstance().getHost() + ":"
                + instance.getInstance().getPort() + instance.getInstance());
    }

    private void save(ServiceInstanceInfo instance, PingRecord pingRecord) {
        Queue<PingRecord> queue = instance.getStats().getPingRecords();
        if (queue.size() > 16) {
            queue.poll();
        }
        queue.offer(pingRecord);
    }

}
