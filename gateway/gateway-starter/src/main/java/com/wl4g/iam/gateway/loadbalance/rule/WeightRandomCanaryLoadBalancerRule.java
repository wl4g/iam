package com.wl4g.iam.gateway.loadbalance.rule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;

/**
 * Weight Round-Robin Grayscale Load Balancer rule based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 * @see {@link com.netflix.loadbalancer.WeightedResponseTimeRule}
 */
public class WeightRandomCanaryLoadBalancerRule extends AbstractCanaryLoadBalancerRule {

    private final AtomicInteger position = new AtomicInteger(0);

    public WeightRandomCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    public CanaryLoadBalancerKind kind() {
        return CanaryLoadBalancerKind.WR;
    }

    @Override
    protected ServiceInstance doChooseServiceInstance(
            List<ServiceInstance> availableInstances,
            List<ServiceInstance> candidateInstances) {

        // TODO
        int pos = Math.abs(position.incrementAndGet());

        return candidateInstances.get(pos % candidateInstances.size());
    }

}