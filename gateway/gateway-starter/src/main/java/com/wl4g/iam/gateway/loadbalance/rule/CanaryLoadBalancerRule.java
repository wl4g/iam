package com.wl4g.iam.gateway.loadbalance.rule;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * {@link CanaryLoadBalancerRule}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public interface CanaryLoadBalancerRule {
    ServiceInstance choose(String serviceId, ServerHttpRequest request);
}
