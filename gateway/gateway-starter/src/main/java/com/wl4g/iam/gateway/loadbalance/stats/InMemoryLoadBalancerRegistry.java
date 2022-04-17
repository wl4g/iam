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

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.iam.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.RouteServiceStatus;

/**
 * {@link InMemoryLoadBalancerRegistry}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-15 v3.0.0
 * @since v3.0.0
 */
public class InMemoryLoadBalancerRegistry implements LoadBalancerRegistry {

    private final Map<String, RouteServiceStatus> registerRouteServices = new ConcurrentHashMap<>(32);

    @Override
    public void register(
            @NotBlank String routeId,
            @NotNull CanaryLoadBalancerFilterFactory.Config config,
            @NotNull InstanceStatus instance) {
        hasTextOf(routeId, "routeId");
        notNullOf(config, "config");
        notNullOf(instance, "instance");
        RouteServiceStatus routeService = getRouteService(routeId);
        routeService.setRouteId(routeId);
        routeService.setConfig(config);
        routeService.getInstances().put(LoadBalancerUtil.getInstanceId(instance.getInstance()), instance);
        registerRouteServices.put(routeId, routeService);
    }

    @Override
    public void register(@NotBlank String routeId, @NotNull RouteServiceStatus routeService) {
        hasTextOf(routeId, "routeId");
        notNullOf(routeService, "routeService");
        registerRouteServices.put(routeId, routeService);
    }

    @Override
    public @NotNull Map<String, RouteServiceStatus> getAllRouteServices() {
        return registerRouteServices;
    }

    @Override
    public @NotNull RouteServiceStatus getRouteService(@NotBlank String routeId) {
        hasTextOf(routeId, "routeId");
        RouteServiceStatus routeService = registerRouteServices.get(routeId);
        if (isNull(routeService)) {
            synchronized (this) {
                routeService = registerRouteServices.get(routeId);
                if (isNull(routeService)) {
                    registerRouteServices.put(routeId, routeService = new RouteServiceStatus());
                }
            }
        }
        return routeService;
    }

}
