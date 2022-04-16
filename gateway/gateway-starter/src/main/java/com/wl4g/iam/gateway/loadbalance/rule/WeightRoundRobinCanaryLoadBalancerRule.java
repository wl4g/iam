package com.wl4g.iam.gateway.loadbalance.rule;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;

/**
 * Grayscale load balancer rule for weight-based round-Robin.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 */
public class WeightRoundRobinCanaryLoadBalancerRule extends RoundRobinCanaryLoadBalancerRule {

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.WRR;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        // TODO

        return super.doChooseInstance(exchange, stats, serviceId, candidateInstances);
    }

}