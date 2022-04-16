package com.wl4g.iam.gateway.loadbalance.rule;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.rule.stats.LoadBalancerStats.ServiceInstanceStatus;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.core.web.matcher.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

/**
 * Abstract Grayscale Load Balancer rule based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 */
public abstract class AbstractCanaryLoadBalancerRule implements CanaryLoadBalancerRule {

    protected final SmartLogger log = getLogger(getClass());
    protected final CanaryLoadBalancerProperties loadBalancerConfig;
    protected final DiscoveryClient discoveryClient;
    protected final SpelRequestMatcher requestMatcher;

    public AbstractCanaryLoadBalancerRule(CanaryLoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        this.loadBalancerConfig = notNullOf(loadBalancerConfig, "loadBalancerConfig");
        this.discoveryClient = notNullOf(discoveryClient, "discoveryClient");
        this.requestMatcher = new SpelRequestMatcher(loadBalancerConfig.getMatchRuleDefinitions());
    }

    @Override
    public ServiceInstance choose(ServerWebExchange exchange, LoadBalancerStats stats, String serviceId) {
        List<ServiceInstance> allInstances = discoveryClient.getInstances(serviceId);

        // There is no instance in the registry throwing an exception.
        if (isEmpty(allInstances)) {
            log.warn("No found instance available for {}", serviceId);
            throw new NotFoundException("No found instance available for " + serviceId);
        }

        // Register all instances.
        stats.register(allInstances);

        // According to the configuration expression, match whether the current
        // request satisfies the load condition for executing the canary.
        List<ServiceInstance> candidateInstances = null;
        List<MatchHttpRequestRule> rules = requestMatcher.find(new ReactiveRequestExtractor(exchange.getRequest()),
                loadBalancerConfig.getSelectExpression());
        if (isEmpty(rules)) {
            log.warn("The request did not match the canary load balancer instance.");
            if (loadBalancerConfig.isFallbackAllToCandidates()) {
                candidateInstances = allInstances;
            } else {
                return null;
            }
        } else {
            // Gets a list of eligible candidate instances.
            candidateInstances = findCandidateInstances(allInstances, rules.stream().map(r -> r.getName()).collect(toList()));
        }

        return doChooseInstance(exchange, stats, serviceId, candidateInstances);
    }

    public List<ServiceInstance> findCandidateInstances(List<ServiceInstance> instances, List<String> matchedRuleNames) {
        // Traverse the meta-data of the instance, and return this instance if
        // there is a match.
        List<ServiceInstance> candidates = safeList(instances).stream()
                .filter(i -> safeMap(i.getMetadata()).entrySet()
                        .stream()
                        .filter(e -> startsWith(e.getKey(), loadBalancerConfig.getCanaryDiscoveryServiceLabelPrefix()))
                        .anyMatch(e -> matchedRuleNames.stream().anyMatch(rn -> eqIgnCase(e.getValue(), rn))))
                .collect(toList());

        log.debug("Choosen canary loadbalancer candidate instances: {} -> {}", matchedRuleNames, instances);
        return candidates;
    }

    protected abstract ServiceInstance doChooseInstance(
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances);

    protected List<ServiceInstanceStatus> getAvailableInstances(
            List<ServiceInstanceStatus> reachableInstances,
            List<ServiceInstance> candidateInstances) {

        return safeList(reachableInstances).stream()
                .filter(i -> safeList(candidateInstances).stream()
                        .anyMatch(c -> StringUtils.equals(i.getInstance().getInstanceId(), c.getInstanceId())))
                .collect(toList());
    }

}