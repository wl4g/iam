package com.wl4g.iam.gateway.loadbalance.rule;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.infra.common.log.SmartLogger;

import lombok.AllArgsConstructor;

/**
 * Grayscale routing based on client request version number
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
@AllArgsConstructor
public class VersionGrayLoadBalancerRule implements GrayLoadBalancerRule {

    private final SmartLogger log = getLogger(getClass());
    private final LoadBalancerProperties loadBalancerConfig;
    private final DiscoveryClient discoveryClient;

    @Override
    public ServiceInstance choose(String serviceId, ServerHttpRequest request) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        // There is no instance in the registry throwing an exception.
        if (isEmpty(instances)) {
            log.warn("No found instance available for {}", serviceId);
            throw new NotFoundException("No found instance available for " + serviceId);
        }

        // Gets the request version, if not, return an available instance
        // randomly.
        String reqVersion = request.getHeaders().getFirst(loadBalancerConfig.getVersionGrayLoadBalanceRequestHeader());
        if (loadBalancerConfig.isFallbackToGetFromQueryParam()) {
            reqVersion = request.getQueryParams().getFirst(loadBalancerConfig.getVersionGrayLoadBalanceRequestHeader());
        }
        if (isBlank(reqVersion)) {
            return instances.get(RandomUtils.nextInt(0, instances.size()));
        }

        // Get a list of eligible candidate instances.
        List<ServiceInstance> candidates = findCandidateInstances(instances, reqVersion);

        // TODO by configured policies choose
        // Choose by default random.
        return candidates.get(RandomUtils.nextInt(0, candidates.size()));
    }

    private List<ServiceInstance> findCandidateInstances(List<ServiceInstance> instances, String reqVersion) {
        // Traverse the meta-data of the instance, and return this instance if
        // there is a match.
        safeList(instances).stream()
                .filter(i -> equalsIgnoreCase(
                        safeMap(i.getMetadata()).get(loadBalancerConfig.getVersionGrayLoadBalanceMetadataKey()), reqVersion))
                .collect(toList());
        log.debug("Choosed candidate gray loadbalance reqVersion: {} -> {}", reqVersion, instances);
        return null;
    }

}