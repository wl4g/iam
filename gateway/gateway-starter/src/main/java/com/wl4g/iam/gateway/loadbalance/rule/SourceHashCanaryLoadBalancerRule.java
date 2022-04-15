package com.wl4g.iam.gateway.loadbalance.rule;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.net.InetSocketAddress;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;

/**
 * Grayscale load balancer rule for based source address hashing. </br>
 * That is, the request from the same ip is sent to the same server in the
 * backend, if the backend server is working normally and not overloaded. This
 * can solve the problem of session sharing, but there is a problem here. Many
 * enterprises, communities, and schools share an IP, which will lead to uneven
 * distribution of requests.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 */
public class SourceHashCanaryLoadBalancerRule extends AbstractCanaryLoadBalancerRule {

    public SourceHashCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        super(loadBalancerConfig, discoveryClient);
    }

    @Override
    public LoadBalancerAlgorithm kind() {
        return LoadBalancerAlgorithm.SH;
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

            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (nonNull(remoteAddress)) {
                int hash = remoteAddress.getHostString().hashCode();
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
        }

        if (count >= loadBalancerConfig.getMaxChooseTries()) {
            log.warn("No available alive servers after {} tries from load balancer stats: {}", count, stats);
        }
        return null;
    }

}