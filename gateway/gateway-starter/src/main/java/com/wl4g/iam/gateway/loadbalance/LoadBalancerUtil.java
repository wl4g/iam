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
package com.wl4g.iam.gateway.loadbalance;

import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.util.Objects.isNull;

import java.lang.reflect.Field;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;

import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.Stats;

/**
 * {@link LoadBalancerUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-17 v3.0.0
 * @since v3.0.0
 */
public abstract class LoadBalancerUtil {

    public static boolean isAlive(CanaryLoadBalancerFilterFactory.Config config, Stats stats) {
        return isNull(stats.getAlive()) ? config.getChoose().isNullPingToReachable() : stats.getAlive();
    }

    public static String getInstanceId(ServiceInstance instance) {
        if (instance instanceof DelegatingServiceInstance) {
            ServiceInstance _instance = getField(DELEGATE_FIELD, (DelegatingServiceInstance) instance, true);
            return _instance.getInstanceId();
        }
        return instance.getInstanceId();
    }

    public static final Field DELEGATE_FIELD = findField(DelegatingServiceInstance.class, "delegate", ServiceInstance.class);

}
