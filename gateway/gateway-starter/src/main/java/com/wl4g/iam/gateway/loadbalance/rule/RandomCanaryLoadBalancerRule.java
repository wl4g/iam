package com.wl4g.iam.gateway.loadbalance.rule;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.ThreadLocalRandom.current;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;

/**
 * Random Grayscale Load Balancer rule based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 */
public class RandomCanaryLoadBalancerRule extends AbstractCanaryLoadBalancerRule {

    public RandomCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.R;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        // return
        // candidateInstances.get(current().nextInt(0,candidateInstances.size()));

        int count = 0;
        ServiceInstanceStatus chosenInstance = null;
        while (isNull(chosenInstance) && count++ < 10) {
            List<ServiceInstanceStatus> allInstances = stats.getAllInstances(serviceId);
            List<ServiceInstanceStatus> reachableInstances = stats.getReachableInstances(serviceId);
            List<ServiceInstanceStatus> availableInstances = getAvailableInstances(reachableInstances, candidateInstances);

            int allCount = allInstances.size();
            int avaCount = availableInstances.size();

            if ((avaCount == 0) || (allCount == 0)) {
                log.warn("No up servers available from load balancer: {}", stats);
                return null;
            }

            int nextInstanceIndex = current().nextInt(0, availableInstances.size());
            chosenInstance = availableInstances.get(nextInstanceIndex);

            if (isNull(chosenInstance)) {
                // Give up the opportunity for short-term CPU to give other
                // threads execution, just like the sleep() method does not
                // release the lock.
                Thread.yield();
                continue;
            }

            if (nonNull(chosenInstance.getStats().getAlive()) && chosenInstance.getStats().getAlive()) {
                return chosenInstance.getInstance();
            }

            // Next.
            chosenInstance = null;
        }

        if (count >= loadBalancerConfig.getMaxChooseTries()) {
            log.warn("No available alive servers after {} tries from load balancer: {}", count, stats);
        }
        return null;
    }

}