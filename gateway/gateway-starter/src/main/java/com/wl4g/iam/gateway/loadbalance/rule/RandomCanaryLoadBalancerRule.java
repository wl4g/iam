package com.wl4g.iam.gateway.loadbalance.rule;

import static java.util.concurrent.ThreadLocalRandom.current;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;

/**
 * Random Grayscale Load Balancer rule based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public class RandomCanaryLoadBalancerRule extends AbstractCanaryLoadBalancerRule {

    public RandomCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    protected ServiceInstance doChooseServiceInstance(
            List<ServiceInstance> availableInstances,
            List<ServiceInstance> candidateInstances) {
        return candidateInstances.get(current().nextInt(0, candidateInstances.size()));
    }

}