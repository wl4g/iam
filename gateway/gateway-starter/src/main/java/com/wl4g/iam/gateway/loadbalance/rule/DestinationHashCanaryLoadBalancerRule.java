package com.wl4g.iam.gateway.loadbalance.rule;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;

/**
 * Grayscale load balancer rule for based destination service hashing. </br>
 * Simply put, the same type of requests are allocated to the same backend
 * server, for example, requests ending in .jgp, .png, etc. are forwarded to the
 * same node. This algorithm is not for real load balancing, but for the
 * classification management of resources. This scheduling algorithm is mainly
 * used in systems using cache nodes to improve the cache hit rate.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 */
public class DestinationHashCanaryLoadBalancerRule extends RoundRobinCanaryLoadBalancerRule {

    public DestinationHashCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.DH;
    }

    @Override
    protected ServiceInstance doChooseInstance( 
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances) {

        int count = 0;
        ServiceInstanceStatus chosenInstance = null;
        while (isNull(chosenInstance) && count++ < loadBalancerConfig.getMaxChooseTries()) {
            List<ServiceInstanceStatus> allInstances = stats.getAllInstances(serviceId);
            List<ServiceInstanceStatus> reachableInstances = stats.getReachableInstances(serviceId);
            List<ServiceInstanceStatus> availableInstances = getAvailableInstances(reachableInstances, candidateInstances);

            int allCount = allInstances.size();
            int avaCount = availableInstances.size();

            if ((avaCount == 0) || (allCount == 0)) {
                log.warn("No up servers available from load balancer stats: {}", stats);
                return null;
            }

            String path = exchange.getRequest().getURI().getPath();
            int hash = path.hashCode();
            // Simply put, the same type of requests are allocated to the same
            // backend server, for example, requests ending in .jgp, .png, etc.
            // are forwarded to the same node. This algorithm is not for real
            // load balancing, but for the classification management of
            // resources. This scheduling algorithm is mainly used in systems
            // that use cache nodes to improve the cache hit rate.
            String ext = StringUtils.getFilenameExtension(path);
            if (!isBlank(ext)) {
                hash = ext.hashCode();
            }
            int nextInstanceIndex = avaCount % hash;
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
            log.warn("No available alive servers after {} tries from load balancer stats: {}", count, stats);
        }
        return null;
    }

}