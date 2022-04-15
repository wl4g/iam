package com.wl4g.iam.gateway.loadbalance.rule;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Comparator;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;

/**
 * Grayscale load balancer rule for based least connections. </br>
 * This algorithm will decide who to distribute the request to according to the
 * number of connections of the back-end RS. For example, if the number of RS1
 * connections is less than the number of RS2 connections, the request will be
 * sent to RS1 first. The problem here is that the session cannot be maintained,
 * that is, session sharing.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 */
public class LeastConnCanaryLoadBalancerRule extends AbstractCanaryLoadBalancerRule {

    public LeastConnCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.LC;
    }

    @Override
    protected ServiceInstance doChooseInstance(
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        int count = 0;
        ServiceInstanceStatus chosenInstance = null;
        while (chosenInstance == null && count++ < 10) {
            List<ServiceInstanceStatus> allInstances = stats.getAllInstances(serviceId);
            List<ServiceInstanceStatus> reachableInstances = stats.getReachableInstances(serviceId);
            List<ServiceInstanceStatus> availableInstances = getAvailableInstances(reachableInstances, candidateInstances);

            int allCount = allInstances.size();
            int avaCount = availableInstances.size();

            if ((avaCount == 0) || (allCount == 0)) {
                log.warn("No up servers available from load balancer: {}", stats);
                return null;
            }

            chosenInstance = availableInstances.stream().min(DEFAULT).orElse(null);
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

    public static final Comparator<ServiceInstanceStatus> DEFAULT = (o1, o2) -> o1.getStats().getConnections().get()
            - o2.getStats().getConnections().get();

}