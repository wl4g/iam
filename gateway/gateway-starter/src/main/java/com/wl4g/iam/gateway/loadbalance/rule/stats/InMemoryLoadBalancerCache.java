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

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;

/**
 * {@link InMemoryLoadBalancerCache}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-15 v3.0.0
 * @since v3.0.0
 */
public class InMemoryLoadBalancerCache implements LoadBalancerCache {

    private final Map<String, Map<String, ServiceInstanceStatus>> registerServices = new ConcurrentHashMap<>(32);

    @Override
    public Map<String, Map<String, ServiceInstanceStatus>> getAllServices() {
        return registerServices;
    }

    @Override
    public Map<String, ServiceInstanceStatus> getService(String serviceId, boolean orCreate) {
        hasTextOf(serviceId, "serviceId");
        // Gets or create
        Map<String, ServiceInstanceStatus> instances = registerServices.get(serviceId);
        if (orCreate && isNull(instances)) {
            synchronized (serviceId) {
                instances = registerServices.get(serviceId);
                if (isNull(instances)) {
                    registerServices.put(serviceId, instances = new ConcurrentHashMap<>(16));
                }
            }
        }
        return instances;
    }

    @Override
    public void putService(String serviceId, Map<String, ServiceInstanceStatus> instances) {
        hasTextOf(serviceId, "serviceId");
        notNullOf(instances, "instances");
        registerServices.put(serviceId, instances);
    }

    @Override
    public ServiceInstanceStatus getServiceInstance(String serviceId, String instanceId, boolean orCreate) {
        hasTextOf(serviceId, "serviceId");
        hasTextOf(instanceId, "instanceId");
        // Gets or create
        Map<String, ServiceInstanceStatus> service = getService(serviceId, orCreate);
        ServiceInstanceStatus status = service.get(instanceId);
        if (orCreate && isNull(status)) {
            synchronized (instanceId) {
                status = service.get(instanceId);
                if (isNull(status)) {
                    service.put(instanceId, status = new ServiceInstanceStatus());
                }
            }
        }
        return status;
    }

    @Override
    public void putServiceInstance(ServiceInstanceStatus instance) {
        notNullOf(instance, "instance");
        notNullOf(instance.getInstance(), "instances");
        hasTextOf(instance.getInstance().getServiceId(), "instance.serviceId");
        Map<String, ServiceInstanceStatus> service = getService(instance.getInstance().getServiceId(), true);
        service.put(instance.getInstance().getInstanceId(), instance);
    }

}
