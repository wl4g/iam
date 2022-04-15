package com.wl4g.iam.gateway.loadbalance.rule;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;

/**
 * Grayscale load balancer rule for weight-based least response time.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 * @see {@link com.netflix.loadbalancer.WeightedResponseTimeRule}
 */
public class WeightLeastTimeCanaryLoadBalancerRule extends RoundRobinCanaryLoadBalancerRule {

    public WeightLeastTimeCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    public CanaryLoadBalancerKind kind() {
        return CanaryLoadBalancerKind.WLT;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        // TODO

        return super.doChooseInstance(stats, serviceId, candidateInstances);
    }

}