package com.wl4g.iam.gateway.loadbalance.rule;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.wl4g.iam.gateway.loadbalance.config.LoadBalancerProperties;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.core.web.matcher.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

/**
 * Abstract Grayscale Load Balancer rule based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public abstract class AbstractCanaryLoadBalancerRule implements CanaryLoadBalancerRule {

    protected final SmartLogger log = getLogger(getClass());
    protected final LoadBalancerProperties loadBalancerConfig;
    protected final DiscoveryClient discoveryClient;
    protected final SpelRequestMatcher requestMatcher;

    public AbstractCanaryLoadBalancerRule(LoadBalancerProperties loadBalancerConfig, DiscoveryClient discoveryClient) {
        this.loadBalancerConfig = notNullOf(loadBalancerConfig, "loadBalancerConfig");
        this.discoveryClient = notNullOf(discoveryClient, "discoveryClient");
        this.requestMatcher = new SpelRequestMatcher(loadBalancerConfig.getMatchRuleDefinitions());
    }

    @Override
    public ServiceInstance choose(String serviceId, ServerHttpRequest request) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        // There is no instance in the registry throwing an exception.
        if (isEmpty(instances)) {
            log.warn("No found instance available for {}", serviceId);
            throw new NotFoundException("No found instance available for " + serviceId);
        }

        // According to the configuration expression, match whether the current
        // request satisfies the load condition for executing the canary.
        List<MatchHttpRequestRule> matchesRules = requestMatcher.find(new ReactiveRequestExtractor(request),
                loadBalancerConfig.getMatchExpression());
        if (isEmpty(matchesRules)) {
            return instances.get(RandomUtils.nextInt(0, instances.size()));
        }

        // Get a list of eligible candidate instances.
        List<ServiceInstance> candidates = findCandidateInstances(instances,
                matchesRules.stream().map(r -> r.getName()).collect(toList()));

        // TODO
        // int count = 0;
        // ServiceInstance chosenInstance = null;
        // while (chosenInstance == null && count++ < 10) {
        // List<Server> reachableServers = lb.getReachableServers();
        // List<Server> allServers = lb.getAllServers();
        // int upCount = reachableServers.size();
        // int serverCount = allServers.size();
        //
        // if ((upCount == 0) || (serverCount == 0)) {
        // log.warn("No up servers available from load balancer: " + lb);
        // return null;
        // }
        //
        // int nextServerIndex = incrementAndGetModulo(serverCount);
        // chosenInstance = allServers.get(nextServerIndex);
        //
        // if (chosenInstance == null) {
        // // Give up the opportunity for short-term CPU to give other
        // // threads execution, just like the sleep() method does not
        // // release the lock.
        // Thread.yield();
        // continue;
        // }
        //
        // // if (server.isAlive() && (server.isReadyToServe())) {
        // // return (chosenInstance);
        // // }
        //
        // // Next.
        // chosenInstance = null;
        // }
        //
        // if (count >= 10) {
        // log.warn("No available alive servers after 10 tries from load
        // balancer: " + lb);
        // }

        return doChooseServiceInstance(instances, candidates);
    }

    protected List<ServiceInstance> findCandidateInstances(List<ServiceInstance> instances, List<String> ruleNames) {
        // Traverse the meta-data of the instance, and return this instance if
        // there is a match.
        List<ServiceInstance> candidates = safeList(instances).stream()
                .filter(i -> ruleNames.stream()
                        .anyMatch(rn -> equalsIgnoreCase(
                                safeMap(i.getMetadata()).get(loadBalancerConfig.getCanaryDiscoveryServiceLabelKey()), rn)))
                .collect(toList());

        log.debug("Choosen canary loadbalancer candidates: {} -> {}", ruleNames, instances);
        return candidates;
    }

    protected abstract ServiceInstance doChooseServiceInstance(
            List<ServiceInstance> availableInstances,
            List<ServiceInstance> candidateInstances);

}