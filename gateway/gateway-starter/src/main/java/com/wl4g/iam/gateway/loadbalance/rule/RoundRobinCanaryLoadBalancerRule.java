package com.wl4g.iam.gateway.loadbalance.rule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;

/**
 * Round-Robin Grayscale Load Balancer rule based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 * @see {@link com.netflix.loadbalancer.RoundRobinRule}
 */
public class RoundRobinCanaryLoadBalancerRule extends AbstractCanaryLoadBalancerRule {

    private final AtomicInteger position = new AtomicInteger(0);

    public RoundRobinCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    protected ServiceInstance doChooseServiceInstance(
            List<ServiceInstance> availableInstances,
            List<ServiceInstance> candidateInstances) {

        // TODO: enforce order?
        int pos = Math.abs(this.position.incrementAndGet());

        return candidateInstances.get(pos % candidateInstances.size());
    }

}