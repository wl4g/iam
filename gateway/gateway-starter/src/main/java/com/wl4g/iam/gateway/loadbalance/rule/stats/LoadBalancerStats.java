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

import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.collect.Queues;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

/**
 * {@link LoadBalancerStats}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-13 v3.0.0
 * @since v3.0.0
 */
public interface LoadBalancerStats {

    void register(List<ServiceInstanceStatus> instances);

    int connect(ServerWebExchange exchange, ServiceInstance instance);

    int disconnect(ServerWebExchange exchange, ServiceInstance instance);

    List<ServiceInstanceStatus> getReachableInstances(String serviceId);

    List<ServiceInstanceStatus> getAllInstances(String serviceId);

    @With
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInstanceStatus {
        private ServiceInstance instance;
        private Stats stats = new Stats();
    }

    @With
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private AtomicInteger connections = new AtomicInteger(0);
        private Deque<ActivePing> activePings = Queues.newArrayDeque();
        private Deque<PassivePing> passivePings = Queues.newArrayDeque();
        private Boolean alive;
        private double latestCostTime;
        private double oldestCostTime;
        private double maxCostTime;
        private double minCostTime;
        private double avgCostTime;
    }

    /**
     * Active ping result.
     */
    @With
    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class ActivePing {
        private long timestamp;
        private boolean isTimeout;
        private HttpResponseStatus status;
        private String responseBody;
    }

    /**
     * Passive ping result.
     */
    @With
    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class PassivePing {
        private long costTime;
        // private HttpResponseStatus status;
    }

    public static final String KEY_COST_TIME = LoadBalancerStats.class.getName().concat(".costTime");
}
