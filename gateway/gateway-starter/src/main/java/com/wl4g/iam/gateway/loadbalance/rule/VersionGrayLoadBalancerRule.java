package com.wl4g.iam.gateway.loadbalance.rule;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Grayscale routing based on client request version number
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
@Slf4j
@AllArgsConstructor
public class VersionGrayLoadBalancerRule implements GrayLoadBalancerRule {

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
        if (isBlank(reqVersion)) {
            return instances.get(RandomUtils.nextInt(0, instances.size()));
        }

        // Traverse the meta-data of the instance, and return this instance if
        // there is a match.
        for (ServiceInstance instance : instances) {
            Map<String, String> metadata = instance.getMetadata();
            String targetVersion = metadata.get(loadBalancerConfig.getVersionGrayLoadBalanceMetadataKey());
            if (reqVersion.equalsIgnoreCase(targetVersion)) {
                log.debug("gray requst match success :{} {}", reqVersion, instance);
                return instance;
            }
        }
        return instances.get(RandomUtils.nextInt(0, instances.size()));
    }

}