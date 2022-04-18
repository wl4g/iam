package com.wl4g.iam.gateway.loadbalance.chooser;

import static com.wl4g.iam.gateway.loadbalance.config.CanaryLoadbalanceAutoConfiguration.BEAN_CANARY_LB_REQUEST_MATCHER;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.CanaryLoadBalancerFilterFactory;
import com.wl4g.iam.gateway.loadbalance.LoadBalancerUtil;
import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadBalancerProperties;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats;
import com.wl4g.iam.gateway.loadbalance.stats.LoadBalancerStats.InstanceStatus;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.core.web.matcher.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.Getter;

/**
 * Abstract Grayscale Load Balancer chooser based on random.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-03 v3.0.0
 * @since v3.0.0
 */
@Getter
public abstract class AbstractCanaryLoadBalancerChooser implements CanaryLoadBalancerChooser {

    protected final SmartLogger log = getLogger(getClass());
    protected @Autowired CanaryLoadBalancerProperties loadBalancerConfig;
    protected @Autowired LoadBalancerStats loadBalancerStats;
    protected @Autowired DiscoveryClient discoveryClient;
    protected @Resource(name = BEAN_CANARY_LB_REQUEST_MATCHER) SpelRequestMatcher requestMatcher;
    protected @Autowired IamGatewayMetricsFacade metricsFacade;

    @Override
    public ServiceInstance choose(CanaryLoadBalancerFilterFactory.Config config, ServerWebExchange exchange, String serviceId) {
        List<ServiceInstance> allInstances = discoveryClient.getInstances(serviceId);

        // There is no instance in the registry throwing an exception.
        if (isEmpty(allInstances)) {
            log.warn("No found instance available for {}", serviceId);
            addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_EMPTY_INSTANCES_TOTAL, serviceId);
            throw new NotFoundException(format("No found instance available for %s", serviceId));
        }

        // According to the configuration expression, match whether the current
        // request satisfies the load condition for executing the canary.
        List<ServiceInstance> candidateInstances = null;
        List<MatchHttpRequestRule> rules = requestMatcher.find(new ReactiveRequestExtractor(exchange.getRequest()),
                getLoadBalancerConfig().getSelectExpression());
        if (isEmpty(rules)) {
            log.warn("The request did not match the canary load balancer instance.");
            addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_MISSING_TOTAL, serviceId);
            if (config.getChoose().isFallbackAllToCandidates()) {
                candidateInstances = allInstances;
                addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_FALLBACK_TOTAL, serviceId);
            } else {
                return null;
            }
        } else {
            addCounterMetrics(config, exchange, MetricsName.CANARY_LB_CHOOSE_TOTAL, serviceId);
            // Gets a list of eligible candidate instances.
            candidateInstances = findCandidateInstances(allInstances, rules.stream().map(r -> r.getName()).collect(toList()));
        }

        return doChooseInstance(config, exchange, loadBalancerStats, serviceId, candidateInstances);
    }

    public List<ServiceInstance> findCandidateInstances(List<ServiceInstance> instances, List<String> matchedRuleNames) {
        // Traverse the meta-data of the instance, and return this instance if
        // there is a match.
        List<ServiceInstance> candidates = safeList(instances).stream()
                .filter(i -> safeMap(i.getMetadata()).entrySet()
                        .stream()
                        .filter(e -> startsWith(e.getKey(), getLoadBalancerConfig().getCanaryDiscoveryServiceLabelPrefix()))
                        .anyMatch(e -> matchedRuleNames.stream().anyMatch(rn -> eqIgnCase(e.getValue(), rn))))
                .collect(toList());

        log.debug("Choosen canary loadbalancer candidate instances: {} -> {}", matchedRuleNames, instances);
        return candidates;
    }

    protected abstract ServiceInstance doChooseInstance(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            LoadBalancerStats stats,
            String serviceId,
            List<ServiceInstance> candidateInstances);

    protected List<InstanceStatus> getAvailableInstances(
            List<InstanceStatus> reachableInstances,
            List<ServiceInstance> candidateInstances) {

        return safeList(reachableInstances).stream()
                .filter(i -> safeList(candidateInstances).stream()
                        .anyMatch(c -> StringUtils.equals(i.getInstance().getInstanceId(), LoadBalancerUtil.getInstanceId(c))))
                .collect(toList());
    }

    protected void addCounterMetrics(
            CanaryLoadBalancerFilterFactory.Config config,
            ServerWebExchange exchange,
            String metricsName,
            String serviceId) {
        metricsFacade.counter(exchange, metricsName, 1, MetricsTag.SERVICE_ID, serviceId, MetricsTag.LB, kind().name());
    }

}